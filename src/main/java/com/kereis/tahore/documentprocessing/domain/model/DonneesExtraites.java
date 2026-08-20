package com.kereis.tahore.documentprocessing.domain.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Champs extraits d'un document par Delos, chacun avec sa confiance.
 *
 * <p>Le DMS les porte a plat dans {@code metadata}, un nom par champ extrait. Les noms
 * sont ceux du contrat de champs, exposes par {@link CheminsExtraction} : les regles
 * metier n'ecrivent jamais un nom en litteral.
 *
 * @param champs indexes par nom de champ du contrat
 */
public record DonneesExtraites(Map<String, ChampExtrait> champs) {

    public DonneesExtraites {
        champs = Map.copyOf(champs);
    }

    public Optional<ChampExtrait> champ(String nom) {
        return Optional.ofNullable(champs.get(nom));
    }

    /** Vrai si le champ est present <b>et</b> sa confiance atteint le seuil. */
    public boolean exploitable(String nom, double seuil) {
        return champ(nom).filter(c -> c.fiable(seuil)).isPresent();
    }

    /**
     * Champs dont la confiance est sous le seuil, tries du moins sur au plus sur.
     *
     * <p>C'est la liste que le controle de confiance remonte au gestionnaire : ce sont les
     * valeurs a verifier a l'oeil, pas le document entier a rejeter.
     */
    public List<String> champsSousSeuil(double seuil) {
        return champs.entrySet().stream()
                .filter(e -> !e.getValue().fiable(seuil))
                .sorted(Map.Entry.comparingByValue(
                        java.util.Comparator.comparingDouble(ChampExtrait::confiance)))
                .map(Map.Entry::getKey)
                .toList();
    }

    public Optional<Double> confianceMinimale() {
        return champs.values().stream().map(ChampExtrait::confiance).min(Double::compare);
    }
}
