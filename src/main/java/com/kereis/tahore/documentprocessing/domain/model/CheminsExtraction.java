package com.kereis.tahore.documentprocessing.domain.model;

/**
 * Noms des champs extraits, tels que retenus au contrat de champs.
 *
 * <p>Les regles metier passent par ces constantes et n'ecrivent jamais un nom en
 * litteral : si un champ est renomme au classeur, seule cette classe change et
 * le compilateur designe chaque point d'usage.
 *
 * <p>Genere depuis {@code contracts/delos-contrat-champs.json}. Ne pas editer.
 */
public final class CheminsExtraction {

    private CheminsExtraction() {}

    // D09 — BULLETIN_ADHESION
    /** string (enum) — obligatoire : {@code typeProduit} */
    public static final String TYPE_PRODUIT = "typeProduit";
    /** string (enum) — obligatoire : {@code civilite} */
    public static final String CIVILITE = "civilite";
    /** string — obligatoire : {@code prenom} */
    public static final String PRENOM = "prenom";
    /** string — obligatoire : {@code nom} */
    public static final String NOM = "nom";
    /** string — obligatoire : {@code dateNaissance} */
    public static final String DATE_NAISSANCE = "dateNaissance";
    /** string — obligatoire (classeur : commune) : {@code communeNaissance} */
    public static final String COMMUNE_NAISSANCE = "communeNaissance";
    /** string — obligatoire : {@code departement} */
    public static final String DEPARTEMENT = "departement";
    /** string — obligatoire (classeur : pays) : {@code paysNaissance} */
    public static final String PAYS_NAISSANCE = "paysNaissance";
    /** string — obligatoire : {@code adresse} */
    public static final String ADRESSE = "adresse";
    /** string — obligatoire : {@code pays} */
    public static final String PAYS = "pays";
    /** string — obligatoire : {@code codePostal} */
    public static final String CODE_POSTAL = "codePostal";
    /** string — obligatoire : {@code commune} */
    public static final String COMMUNE = "commune";
    /** string — obligatoire : {@code telephone} */
    public static final String TELEPHONE = "telephone";
    /** string — obligatoire : {@code email} */
    public static final String EMAIL = "email";
    /** string — obligatoire : {@code nationalite} */
    public static final String NATIONALITE = "nationalite";
    /** string — obligatoire : {@code profession} */
    public static final String PROFESSION = "profession";
    /** string (enum) — obligatoire : {@code categorieSocioProfessionnelle} */
    public static final String CATEGORIE_SOCIO_PROFESSIONNELLE = "categorieSocioProfessionnelle";
    /** boolean : {@code sansActiviteRemuneree} */
    public static final String SANS_ACTIVITE_REMUNEREE = "sansActiviteRemuneree";
    /** number — obligatoire : {@code netFiscalN1} */
    public static final String NET_FISCAL_N1 = "netFiscalN1";
    /** number — obligatoire : {@code netFiscalN2} */
    public static final String NET_FISCAL_N2 = "netFiscalN2";
    /** boolean — obligatoire : {@code fumeur} */
    public static final String FUMEUR = "fumeur";
    /** string (enum) — obligatoire : {@code codeFormule} */
    public static final String CODE_FORMULE = "codeFormule";
    /** number — obligatoire : {@code capitalDeces} */
    public static final String CAPITAL_DECES = "capitalDeces";
    /** string (enum) — obligatoire : {@code modaliteVersement} */
    public static final String MODALITE_VERSEMENT = "modaliteVersement";
    /** integer : {@code dureeRenteAnnees} */
    public static final String DUREE_RENTE_ANNEES = "dureeRenteAnnees";
    /** string — obligatoire : {@code dateSignature} */
    public static final String DATE_SIGNATURE = "dateSignature";
    /** boolean : {@code presenceParaphes} */
    public static final String PRESENCE_PARAPHES = "presenceParaphes";
    /** boolean : {@code presentSignatureHabilite} */
    public static final String PRESENT_SIGNATURE_HABILITE = "presentSignatureHabilite";
    /** boolean : {@code presentSignatureAssure} */
    public static final String PRESENT_SIGNATURE_ASSURE = "presentSignatureAssure";
    /** string (enum) — obligatoire : {@code typeClauseDeces} */
    public static final String TYPE_CLAUSE_DECES = "typeClauseDeces";
    /** string (enum) : {@code beneficiaireCivilite} */
    public static final String BENEFICIAIRE_CIVILITE = "beneficiaireCivilite";
    /** string : {@code beneficiaireNom} */
    public static final String BENEFICIAIRE_NOM = "beneficiaireNom";
    /** string : {@code beneficiaireNomNaissance} */
    public static final String BENEFICIAIRE_NOM_NAISSANCE = "beneficiaireNomNaissance";
    /** string : {@code beneficiairePrenom} */
    public static final String BENEFICIAIRE_PRENOM = "beneficiairePrenom";
    /** object : {@code beneficiaireAdresse} */
    public static final String BENEFICIAIRE_ADRESSE = "beneficiaireAdresse";
    /** string (classeur : dateNaissance) : {@code beneficiaireDateNaissance} */
    public static final String BENEFICIAIRE_DATE_NAISSANCE = "beneficiaireDateNaissance";
    /** string : {@code lieuNaissance} */
    public static final String LIEU_NAISSANCE = "lieuNaissance";
    /** number : {@code beneficiairePartPourcentage} */
    public static final String BENEFICIAIRE_PART_POURCENTAGE = "beneficiairePartPourcentage";
    /** boolean : {@code identiqueAssure} */
    public static final String IDENTIQUE_ASSURE = "identiqueAssure";
    /** string (enum) — obligatoire : {@code periodiciteCotisation} */
    public static final String PERIODICITE_COTISATION = "periodiciteCotisation";
    /** boolean : {@code decesDegressif} */
    public static final String DECES_DEGRESSIF = "decesDegressif";
    /** boolean : {@code doublementDecesAccidentel} */
    public static final String DOUBLEMENT_DECES_ACCIDENTEL = "doublementDecesAccidentel";
    /** boolean : {@code fraisProfessionnelsItt} */
    public static final String FRAIS_PROFESSIONNELS_ITT = "fraisProfessionnelsItt";
    /** string (enum) — obligatoire : {@code remiseCollaborateur} */
    public static final String REMISE_COLLABORATEUR = "remiseCollaborateur";
    /** boolean — obligatoire : {@code numeroUidPresent} */
    public static final String NUMERO_UID_PRESENT = "numeroUidPresent";
    /** string — obligatoire : {@code codeAgence} */
    public static final String CODE_AGENCE = "codeAgence";
    /** string — obligatoire : {@code emailConseiller} */
    public static final String EMAIL_CONSEILLER = "emailConseiller";
    /** string — obligatoire : {@code nomConseiller} */
    public static final String NOM_CONSEILLER = "nomConseiller";
    /** string — obligatoire : {@code prenomConseiller} */
    public static final String PRENOM_CONSEILLER = "prenomConseiller";
    /** boolean : {@code risqueSejour} */
    public static final String RISQUE_SEJOUR = "risqueSejour";
    /** string (enum) — obligatoire : {@code conventionMadelin} */
    public static final String CONVENTION_MADELIN = "conventionMadelin";
    /** string (enum) — obligatoire : {@code segmentation} */
    public static final String SEGMENTATION = "segmentation";

    // D10 — RECUEIL_CONSENTEMENT
    /** boolean — obligatoire : {@code presentRecueil} */
    public static final String PRESENT_RECUEIL = "presentRecueil";
    /** boolean — obligatoire : {@code consentementDonneesSante} */
    public static final String CONSENTEMENT_DONNEES_SANTE = "consentementDonneesSante";
    /** boolean — obligatoire : {@code signatureAssure} */
    public static final String SIGNATURE_ASSURE = "signatureAssure";

    // QSS — QUESTIONNAIRE_SANTE_SIMPLIFIE
    /** string (enum) — obligatoire : {@code type} */
    public static final String TYPE = "type";
    /** string (enum) — obligatoire : {@code emetteur} */
    public static final String EMETTEUR = "emetteur";
    /** boolean — obligatoire : {@code manuscrit} */
    public static final String MANUSCRIT = "manuscrit";
    /** boolean — obligatoire : {@code recueilConsentementJoint} */
    public static final String RECUEIL_CONSENTEMENT_JOINT = "recueilConsentementJoint";
    /** boolean — obligatoire : {@code present} */
    public static final String PRESENT = "present";
    /** boolean : {@code paraphesToutesPages} */
    public static final String PARAPHES_TOUTES_PAGES = "paraphesToutesPages";
    /** boolean — obligatoire : {@code allReponsesNegative} */
    public static final String ALL_REPONSES_NEGATIVE = "allReponsesNegative";

    // D13 — MANDAT_SEPA
    /** string — obligatoire : {@code iban} */
    public static final String IBAN = "iban";
    /** string : {@code titulaire} */
    public static final String TITULAIRE = "titulaire";
    /** object — obligatoire : {@code adresseTitulaire} */
    public static final String ADRESSE_TITULAIRE = "adresseTitulaire";
    /** string — obligatoire : {@code bic} */
    public static final String BIC = "bic";

    // D14 — RIB
    /** string (enum) — obligatoire : {@code typeCompte} */
    public static final String TYPE_COMPTE = "typeCompte";
}
