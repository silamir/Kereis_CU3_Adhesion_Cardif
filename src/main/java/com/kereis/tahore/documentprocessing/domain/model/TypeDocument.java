package com.kereis.tahore.documentprocessing.domain.model;

/**
 * Types de documents du perimetre CU#3.
 *
 * <p>Les libelles correspondent au champ {@code documentType} du contrat
 * d'extraction Delos. Le rapprochement avec {@code indexation.nature} du DMS
 * reste a etablir : cote Tahore, la nature 1 vaut « bulletin d'adhesion ».
 */
public enum TypeDocument {
    BULLETIN_ADHESION,
    RECUEIL_CONSENTEMENT,
    QUESTIONNAIRE_SANTE_SIMPLIFIE,
    MANDAT_SEPA,
    RIB;
}
