package com.kereis.tahore.documentprocessing.domain.model;

import java.util.Optional;

/**
 * Types de documents du perimetre CU#3.
 *
 * <p>Le rattachement se fait sur {@code indexation.nature.id} du DMS, qui est le
 * referentiel faisant foi. Une seule valeur est attestee a ce jour : la nature 1 vaut
 * « bulletin d'adhesion », d'apres la specification Tahore des actes. Les quatre autres
 * sont <b>provisoires</b> et a confirmer aupres de l'equipe DMS.
 */
public enum TypeDocument {
    BULLETIN_ADHESION(1),
    RECUEIL_CONSENTEMENT(2),
    QUESTIONNAIRE_SANTE_SIMPLIFIE(3),
    MANDAT_SEPA(4),
    RIB(5);

    private final int natureId;

    TypeDocument(int natureId) {
        this.natureId = natureId;
    }

    public int natureId() {
        return natureId;
    }

    /** Vide si la nature n'appartient pas au perimetre CU#3. */
    public static Optional<TypeDocument> parNature(int natureId) {
        for (TypeDocument t : values()) {
            if (t.natureId == natureId) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    /** Repli sur le libelle, quand la nature n'est pas exploitable. */
    public static Optional<TypeDocument> parLibelle(String libelle) {
        if (libelle == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(libelle.trim()));
        } catch (IllegalArgumentException horsPerimetre) {
            return Optional.empty();
        }
    }
}
