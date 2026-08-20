package com.kereis.tahore.documentprocessing.infrastructure.dms;

import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.DonneesExtraites;
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
 * <p>C'est le seul endroit du module qui connait la forme du DMS. En
 * particulier, la convention de cles de {@code metadata} — encore inconnue
 * cote Kereis — est isolee ici : quand elle sera fixee, seule cette classe
 * changera.
 *
 * <p>La lecture se fait volontairement en {@code Map} et non via des DTO
 * generes : tant que la convention de {@code metadata} n'est pas arretee, une
 * structure typee serait a refaire. Les DTO generes par
 * {@code mvn -P generate-clients} prendront le relais pour le reste.
 */
@Repository
public class DmsLotRepository implements LotRepository {

    /** Cle sous laquelle l'extraction Delos est deposee dans metadata. */
    static final String CLE_EXTRACTION = "delos";

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
     *       {@code metadata} en revanche est une propriete, pas une sous-ressource : l'extraction
     *       Delos arrive donc sans rien demander.
     *   <li><b>Le filtrage se fait cote serveur.</b> Etat de traitement, absence de reference et
     *       domaine sont des parametres de requete : autant de documents qui ne traversent jamais
     *       le reseau. Filtrer cote client serait payer le transfert pour rien.
     *   <li><b>{@code limit} vaut 20 par defaut.</b> Un lot de cinq pieces passe, mais un scan
     *       large serait tronque silencieusement. La valeur est donc explicite et configurable.
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

    static DocumentEntrant versDomaine(Map<String, Object> doc) {
        DonneesExtraites extraction = extraction(doc);
        TypeDocument type = type(extraction, doc);
        if (type == null) {
            return null; // document hors perimetre CU#3
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
                Optional.ofNullable(extraction));
    }

    private static DonneesExtraites extraction(Map<String, Object> doc) {
        Map<String, Object> delos = sousObjet(sousObjet(doc, "metadata"), CLE_EXTRACTION);
        if (delos.isEmpty()) {
            return null;
        }
        Map<String, Object> meta = sousObjet(delos, "extraction");
        Object confiance = meta.get("globalConfidence");
        return new DonneesExtraites(
                confiance instanceof Number n ? n.doubleValue() : 0d,
                booleen(meta, "handwritten"),
                sousObjet(delos, "fields"));
    }

    private static TypeDocument type(DonneesExtraites extraction, Map<String, Object> doc) {
        // Le documentType de l'extraction fait foi. A defaut, indexation.nature,
        // dont le referentiel reste a obtenir cote DMS.
        Map<String, Object> delos = sousObjet(sousObjet(doc, "metadata"), CLE_EXTRACTION);
        String brut = texte(delos, "documentType");
        if (brut == null) {
            return null;
        }
        try {
            return TypeDocument.valueOf(brut);
        } catch (IllegalArgumentException horsPerimetre) {
            return null;
        }
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
