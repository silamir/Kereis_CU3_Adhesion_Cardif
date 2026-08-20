package com.kereis.tahore.documentprocessing.infrastructure.dms;

import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.Lot;
import com.kereis.tahore.documentprocessing.domain.model.TypeDocument;
import com.kereis.tahore.documentprocessing.domain.port.LotRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

/**
 * Adaptateur DMS : traduit la reponse de
 * {@code GET /private/secure/input/documents} en lots du domaine.
 *
 * <p>C'est le seul endroit du module qui connaisse la forme du DMS. La lecture se fait en
 * {@code Map} plutot que via des DTO generes : tant que la convention de cles de
 * {@code metadata} n'est pas arretee, une structure typee serait a refaire.
 */
@Repository
public class DmsLotRepository implements LotRepository {

    private static final ParameterizedTypeReference<Map<String, Object>> REPONSE =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;
    private final DmsProprietes proprietes;

    public DmsLotRepository(RestClient.Builder builder, DmsProprietes proprietes) {
        this.client = builder.baseUrl(proprietes.baseUrl()).build();
        this.proprietes = proprietes;
    }

    /**
     * Interroge le DMS et regroupe les documents en lots.
     *
     * <p>Trois points non evidents du contrat DMS sont a respecter ici.
     *
     * <ol>
     *   <li><b>{@code extension=reception} est obligatoire.</b> {@code reception} est une
     *       sous-ressource, et le service n'en renvoie aucune par defaut. Sans ce parametre,
     *       {@code reception.id} est absent et le regroupement en lots ne peut pas se faire.
     *       {@code metadata} en revanche est une propriete : les champs extraits arrivent sans
     *       rien demander.
     *   <li><b>Le filtrage se fait cote serveur.</b> Etat de traitement et absence de reference
     *       sont des parametres de requete : autant de documents qui ne traversent jamais le
     *       reseau.
     *   <li><b>{@code limit} vaut 20 par defaut.</b> Un scan large serait tronque
     *       silencieusement. La valeur est donc explicite et configurable.
     * </ol>
     */
    @Override
    public List<Lot> lotsEnAttente() {
        Map<String, Object> reponse = client.get()
                .uri(uri -> {
                    uri.path("/private/secure/input/documents")
                            .queryParam("extension", "reception")
                            .queryParam("limit", proprietes.limite());
                    proprietes.etatsEcoutes()
                            .forEach(etat -> uri.queryParam("processingStateIdList", etat));
                    if (proprietes.seulementNonRattaches()) {
                        // Second critere du verrou anti-doublon, applique par le serveur.
                        uri.queryParam("withoutReference", true);
                    }
                    return uri.build();
                })
                .retrieve()
                .body(REPONSE);
        return regrouperParLot(documents(reponse));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> documents(Map<String, Object> reponse) {
        if (reponse == null) {
            return List.of();
        }
        Object data = reponse.get("data");
        return data instanceof List<?> liste ? (List<Map<String, Object>>) liste : List.of();
    }

    private static List<Lot> regrouperParLot(List<Map<String, Object>> brut) {
        Map<String, List<DocumentEntrant>> parLot = new LinkedHashMap<>();
        for (Map<String, Object> doc : brut) {
            DocumentEntrant converti = versDomaine(doc);
            if (converti != null) {
                parLot.computeIfAbsent(converti.identifiantLot(), c -> new ArrayList<>())
                        .add(converti);
            }
        }
        List<Lot> lots = new ArrayList<>();
        parLot.forEach((identifiant, documents) -> lots.add(new Lot(identifiant, documents)));
        return lots;
    }

    /** Renvoie {@code null} si le document sort du perimetre CU#3. */
    static DocumentEntrant versDomaine(Map<String, Object> doc) {
        TypeDocument type = type(doc);
        if (type == null) {
            return null;
        }
        return new DocumentEntrant(
                texte(doc, "id"),
                type,
                texte(sousObjet(doc, "reception"), "id"),
                entier(doc, "receptionOrder"),
                horodatage(sousObjet(doc, "reception"), "createdAt"),
                booleen(sousObjet(doc, "permissions"), "medicalConfidentiality"),
                Optional.ofNullable(texte(sousObjet(doc, "binding"), "reference"))
                        .filter(r -> !r.isBlank()),
                ExtractionDms.lire(sousObjet(doc, "metadata")));
    }

    /**
     * Le type vient de {@code indexation.nature.id}, referentiel du DMS. Repli sur le
     * libelle de la nature puis sur {@code description} tant que le referentiel complet
     * n'est pas obtenu.
     */
    private static TypeDocument type(Map<String, Object> doc) {
        Map<String, Object> nature = sousObjet(sousObjet(doc, "indexation"), "nature");
        Object id = nature.get("id");
        if (id instanceof Number n) {
            Optional<TypeDocument> parNature = TypeDocument.parNature(n.intValue());
            if (parNature.isPresent()) {
                return parNature.orElseThrow();
            }
        }
        return TypeDocument.parLibelle(texte(nature, "name"))
                .or(() -> TypeDocument.parLibelle(texte(doc, "description")))
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> sousObjet(Map<String, Object> parent, String cle) {
        if (parent == null) {
            return Map.of();
        }
        Object valeur = parent.get(cle);
        return valeur instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private static String texte(Map<String, Object> parent, String cle) {
        Object v = parent == null ? null : parent.get(cle);
        return v == null ? null : v.toString();
    }

    private static int entier(Map<String, Object> parent, String cle) {
        Object v = parent.get(cle);
        return v instanceof Number n ? n.intValue() : 0;
    }

    private static boolean booleen(Map<String, Object> parent, String cle) {
        return Boolean.TRUE.equals(parent.get(cle));
    }

    private static Instant horodatage(Map<String, Object> parent, String cle) {
        String v = texte(parent, cle);
        try {
            return v == null ? Instant.EPOCH : Instant.parse(v);
        } catch (RuntimeException illisible) {
            return Instant.EPOCH;
        }
    }
}
