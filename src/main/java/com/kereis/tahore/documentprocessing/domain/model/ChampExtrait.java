package com.kereis.tahore.documentprocessing.domain.model;

import java.util.Optional;

/**
 * Un champ extrait par Delos : sa valeur, et la confiance que le moteur lui accorde.
 *
 * <p>La confiance est portee <b>par champ</b> et non par document. C'est ce qui permet de
 * dire « la date de naissance est sure mais le telephone est douteux » : un score unique
 * par document masquerait le second.
 *
 * @param valeur valeur lue, du type declare au contrat de champs
 * @param confiance de 0 a 1, telle que fournie par le moteur d'extraction
 */
public record ChampExtrait(Object valeur, double confiance) {

    /** Vrai si la confiance atteint le seuil au-dela duquel la valeur est exploitable. */
    public boolean fiable(double seuil) {
        return confiance >= seuil;
    }

    public Optional<String> texte() {
        return valeur == null ? Optional.empty() : Optional.of(valeur.toString());
    }

    public boolean vrai() {
        return Boolean.TRUE.equals(valeur);
    }

    public Optional<Double> nombre() {
        return valeur instanceof Number n ? Optional.of(n.doubleValue()) : Optional.empty();
    }
}
