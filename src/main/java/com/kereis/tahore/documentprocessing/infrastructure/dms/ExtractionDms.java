package com.kereis.tahore.documentprocessing.infrastructure.dms;

import com.kereis.tahore.documentprocessing.domain.model.ChampExtrait;
import com.kereis.tahore.documentprocessing.domain.model.DonneesExtraites;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Traduit l'objet {@code metadata} du DMS en donnees du domaine.
 *
 * <p>Seul endroit du module qui connaisse la forme reelle de {@code metadata}. Delos y
 * depose un champ par cle, sous la forme <code>{value, confidence}</code>. Quand un champ
 * est lui-meme un groupe — {@code beneficiaireAdresse} porte adresse, code postal et
 * commune —, il est aplati en {@code beneficiaireAdresse.codePostal} : le domaine ne
 * manipule que des noms de champ, jamais une arborescence.
 *
 * <p>La convention de cles n'est pas encore arretee cote Kereis. Quand elle le sera,
 * cette classe changera seule.
 */
final class ExtractionDms {

    private ExtractionDms() {}

    static Optional<DonneesExtraites> lire(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Optional.empty();
        }
        Map<String, ChampExtrait> champs = new LinkedHashMap<>();
        aplatir(metadata, "", champs);
        return champs.isEmpty() ? Optional.empty() : Optional.of(new DonneesExtraites(champs));
    }

    @SuppressWarnings("unchecked")
    private static void aplatir(Map<String, Object> source, String prefixe,
            Map<String, ChampExtrait> cible) {
        for (Map.Entry<String, Object> entree : source.entrySet()) {
            String nom = prefixe.isEmpty() ? entree.getKey() : prefixe + "." + entree.getKey();
            if (!(entree.getValue() instanceof Map<?, ?> brut)) {
                continue; // une valeur nue sans confiance n'est pas un champ extrait
            }
            Map<String, Object> objet = (Map<String, Object>) brut;
            if (objet.containsKey("confidence")) {
                cible.put(nom, new ChampExtrait(objet.get("value"), confiance(objet)));
            } else {
                aplatir(objet, nom, cible); // groupe de champs
            }
        }
    }

    private static double confiance(Map<String, Object> objet) {
        Object c = objet.get("confidence");
        return c instanceof Number n ? n.doubleValue() : 0d;
    }
}
