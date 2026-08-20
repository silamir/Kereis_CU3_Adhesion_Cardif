package com.kereis.tahore.documentprocessing.domain.model;

/**
 * Noms des champs extraits par Delos, tels que valides au contrat de champs.
 *
 * <p>Ce sont les noms de la colonne « Nom de la variable » du contrat, donc ceux que
 * le metier a relus. Les champs arrivent a plat dans {@code metadata}, un nom par
 * champ extrait.
 *
 * <p>Les regles metier passent par ces constantes et n'ecrivent jamais un nom en
 * litteral : si Delos renomme un champ, seule cette classe change et le compilateur
 * designe chaque point d'usage.
 *
 * <p>Genere depuis le contrat de champs. Ne pas editer a la main.
 */
public final class CheminsExtraction {

    private CheminsExtraction() {}

    // D09 — BULLETIN_ADHESION
    /** string (enum) : {@code typeProduit} */
    public static final String TYPE_PRODUIT = "typeProduit";
    /** string (enum) : {@code civilite} */
    public static final String CIVILITE = "civilite";
    /** string : {@code prenom} */
    public static final String PRENOM = "prenom";
    /** string : {@code nom} */
    public static final String NOM = "nom";
    /** string : {@code dateNaissance} */
    public static final String DATE_NAISSANCE = "dateNaissance";
    /** string : {@code commune} */
    public static final String COMMUNE = "commune";
    /** string : {@code departement} */
    public static final String DEPARTEMENT = "departement";
    /** string : {@code pays} */
    public static final String PAYS = "pays";
    /** string : {@code adresse} */
    public static final String ADRESSE = "adresse";
    /** string : {@code codePostal} */
    public static final String CODE_POSTAL = "codePostal";
    /** string : {@code telephone} */
    public static final String TELEPHONE = "telephone";
    /** string : {@code email} */
    public static final String EMAIL = "email";
    /** string : {@code nationalite} */
    public static final String NATIONALITE = "nationalite";
    /** string : {@code profession} */
    public static final String PROFESSION = "profession";
    /** string (enum) : {@code categorieSocioProfessionnelle} */
    public static final String CATEGORIE_SOCIO_PROFESSIONNELLE = "categorieSocioProfessionnelle";
    /** boolean : {@code sansActiviteRemuneree} */
    public static final String SANS_ACTIVITE_REMUNEREE = "sansActiviteRemuneree";
    /** number : {@code netFiscalN1} */
    public static final String NET_FISCAL_N1 = "netFiscalN1";
    /** number : {@code netFiscalN2} */
    public static final String NET_FISCAL_N2 = "netFiscalN2";
    /** boolean : {@code fumeur} */
    public static final String FUMEUR = "fumeur";
    /** string (enum) : {@code codeFormule} */
    public static final String CODE_FORMULE = "codeFormule";
    /** number : {@code capitalDeces} */
    public static final String CAPITAL_DECES = "capitalDeces";
    /** string (enum) : {@code modaliteVersement} */
    public static final String MODALITE_VERSEMENT = "modaliteVersement";
    /** integer : {@code dureeRenteAnnees} */
    public static final String DUREE_RENTE_ANNEES = "dureeRenteAnnees";
    /** string : {@code dateSignature} */
    public static final String DATE_SIGNATURE = "dateSignature";
    /** boolean : {@code presenceParaphes} */
    public static final String PRESENCE_PARAPHES = "presenceParaphes";
    /** boolean : {@code presentSignatureHabilite} */
    public static final String PRESENT_SIGNATURE_HABILITE = "presentSignatureHabilite";
    /** boolean : {@code presentSignatureAssure} */
    public static final String PRESENT_SIGNATURE_ASSURE = "presentSignatureAssure";
    /** string (enum) : {@code typeClauseDeces} */
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
    /** string : {@code lieuNaissance} */
    public static final String LIEU_NAISSANCE = "lieuNaissance";
    /** number : {@code beneficiairePartPourcentage} */
    public static final String BENEFICIAIRE_PART_POURCENTAGE = "beneficiairePartPourcentage";
    /** boolean : {@code identiqueAssure} */
    public static final String IDENTIQUE_ASSURE = "identiqueAssure";
    /** string (enum) : {@code periodiciteCotisation} */
    public static final String PERIODICITE_COTISATION = "periodiciteCotisation";
    /** boolean : {@code decesDegressif} */
    public static final String DECES_DEGRESSIF = "decesDegressif";
    /** boolean : {@code doublementDecesAccidentel} */
    public static final String DOUBLEMENT_DECES_ACCIDENTEL = "doublementDecesAccidentel";
    /** boolean : {@code fraisProfessionnelsItt} */
    public static final String FRAIS_PROFESSIONNELS_ITT = "fraisProfessionnelsItt";
    /** string (enum) : {@code remiseCollaborateur} */
    public static final String REMISE_COLLABORATEUR = "remiseCollaborateur";
    /** boolean : {@code numeroUidPresent} */
    public static final String NUMERO_UID_PRESENT = "numeroUidPresent";
    /** string : {@code codeAgence} */
    public static final String CODE_AGENCE = "codeAgence";
    /** string : {@code emailConseiller} */
    public static final String EMAIL_CONSEILLER = "emailConseiller";
    /** string : {@code nomConseiller} */
    public static final String NOM_CONSEILLER = "nomConseiller";
    /** string : {@code prenomConseiller} */
    public static final String PRENOM_CONSEILLER = "prenomConseiller";
    /** boolean : {@code risqueSejour} */
    public static final String RISQUE_SEJOUR = "risqueSejour";
    /** string (enum) : {@code conventionMadelin} */
    public static final String CONVENTION_MADELIN = "conventionMadelin";
    /** string (enum) : {@code segmentation} */
    public static final String SEGMENTATION = "segmentation";

    // D10 — RECUEIL_CONSENTEMENT
    /** boolean : {@code presentRecueil} */
    public static final String PRESENT_RECUEIL = "presentRecueil";
    /** boolean : {@code consentementDonneesSante} */
    public static final String CONSENTEMENT_DONNEES_SANTE = "consentementDonneesSante";
    /** boolean : {@code signatureAssure} */
    public static final String SIGNATURE_ASSURE = "signatureAssure";

    // QSS — QUESTIONNAIRE_SANTE_SIMPLIFIE
    /** string (enum) : {@code type} */
    public static final String TYPE = "type";
    /** string (enum) : {@code emetteur} */
    public static final String EMETTEUR = "emetteur";
    /** boolean : {@code manuscrit} */
    public static final String MANUSCRIT = "manuscrit";
    /** boolean : {@code recueilConsentementJoint} */
    public static final String RECUEIL_CONSENTEMENT_JOINT = "recueilConsentementJoint";
    /** boolean : {@code present} */
    public static final String PRESENT = "present";
    /** boolean : {@code paraphesToutesPages} */
    public static final String PARAPHES_TOUTES_PAGES = "paraphesToutesPages";
    /** boolean : {@code allReponsesNegative} */
    public static final String ALL_REPONSES_NEGATIVE = "allReponsesNegative";

    // D13 — MANDAT_SEPA
    /** string : {@code iban} */
    public static final String IBAN = "iban";
    /** string : {@code titulaire} */
    public static final String TITULAIRE = "titulaire";
    /** — : {@code — ne pas extraire —} */
    public static final String — NE PAS EXTRAIRE — = "— ne pas extraire —";
    /** object : {@code adresseTitulaire} */
    public static final String ADRESSE_TITULAIRE = "adresseTitulaire";
    /** string : {@code bic} */
    public static final String BIC = "bic";

    // D14 — RIB
    /** string (enum) : {@code typeCompte} */
    public static final String TYPE_COMPTE = "typeCompte";
}
