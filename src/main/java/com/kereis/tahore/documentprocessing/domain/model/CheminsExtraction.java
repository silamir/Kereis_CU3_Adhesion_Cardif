package com.kereis.tahore.documentprocessing.domain.model;

/**
 * Noms des champs attendus dans {@code metadata}.
 *
 * <p>Le commentaire de chaque constante indique ce que Delos renvoie : une valeur
 * brute, ou un booleen de condition. Dans le second cas, le libelle du classeur dit
 * ce que le booleen affirme.
 *
 * <p>Genere depuis {@code contracts/delos-contrat-champs.json}. Ne pas editer.
 */
public final class CheminsExtraction {

    private CheminsExtraction() {}

    // D09 — BULLETIN_ADHESION
    /** string (enum) — obligatoire */
    public static final String TYPE_PRODUIT = "typeProduit";
    /** string (enum) — obligatoire */
    public static final String CIVILITE = "civilite";
    /** string — obligatoire */
    public static final String PRENOM = "prenom";
    /** string — obligatoire */
    public static final String NOM = "nom";
    /** string — obligatoire */
    public static final String DATE_NAISSANCE = "dateNaissance";
    /** string — obligatoire (classeur : commune) */
    public static final String COMMUNE_NAISSANCE = "communeNaissance";
    /** string — obligatoire */
    public static final String DEPARTEMENT = "departement";
    /** string — obligatoire (classeur : pays) */
    public static final String PAYS_NAISSANCE = "paysNaissance";
    /** string — obligatoire */
    public static final String ADRESSE = "adresse";
    /** string — obligatoire */
    public static final String PAYS = "pays";
    /** string — obligatoire */
    public static final String CODE_POSTAL = "codePostal";
    /** string — obligatoire */
    public static final String COMMUNE = "commune";
    /** string — obligatoire */
    public static final String TELEPHONE = "telephone";
    /** string — obligatoire */
    public static final String EMAIL = "email";
    /** string — obligatoire */
    public static final String NATIONALITE = "nationalite";
    /** string — obligatoire */
    public static final String PROFESSION = "profession";
    /** string (enum) — obligatoire */
    public static final String CATEGORIE_SOCIO_PROFESSIONNELLE = "categorieSocioProfessionnelle";
    /** booleen — Sans activité professionnelle rémunérée (O/N) */
    public static final String SANS_ACTIVITE_REMUNEREE = "sansActiviteRemuneree";
    /** number — obligatoire */
    public static final String NET_FISCAL_N1 = "netFiscalN1";
    /** number — obligatoire */
    public static final String NET_FISCAL_N2 = "netFiscalN2";
    /** boolean — obligatoire */
    public static final String FUMEUR = "fumeur";
    /** string (enum) — obligatoire */
    public static final String CODE_FORMULE = "codeFormule";
    /** number — obligatoire */
    public static final String CAPITAL_DECES = "capitalDeces";
    /** string (enum) — obligatoire */
    public static final String MODALITE_VERSEMENT = "modaliteVersement";
    /** integer */
    public static final String DUREE_RENTE_ANNEES = "dureeRenteAnnees";
    /** string — obligatoire */
    public static final String DATE_SIGNATURE = "dateSignature";
    /** booleen — Paraphes de l'adhésion / assuré (présent O/N) */
    public static final String PRESENCE_PARAPHES = "presenceParaphes";
    /** booleen — Signature de la personne habilitée si applicable */
    public static final String PRESENT_SIGNATURE_HABILITE = "presentSignatureHabilite";
    /** booleen — Signature de l'assuré (présent O/N) */
    public static final String PRESENT_SIGNATURE_ASSURE = "presentSignatureAssure";
    /** string (enum) — obligatoire */
    public static final String TYPE_CLAUSE_DECES = "typeClauseDeces";
    /** string (enum) */
    public static final String BENEFICIAIRE_CIVILITE = "beneficiaireCivilite";
    /** string */
    public static final String BENEFICIAIRE_NOM = "beneficiaireNom";
    /** string */
    public static final String BENEFICIAIRE_NOM_NAISSANCE = "beneficiaireNomNaissance";
    /** string */
    public static final String BENEFICIAIRE_PRENOM = "beneficiairePrenom";
    /** object */
    public static final String BENEFICIAIRE_ADRESSE = "beneficiaireAdresse";
    /** string (classeur : dateNaissance) */
    public static final String BENEFICIAIRE_DATE_NAISSANCE = "beneficiaireDateNaissance";
    /** string */
    public static final String LIEU_NAISSANCE = "lieuNaissance";
    /** number */
    public static final String BENEFICIAIRE_PART_POURCENTAGE = "beneficiairePartPourcentage";
    /** booleen — Souscripteur = assuré (O/N) */
    public static final String IDENTIQUE_ASSURE = "identiqueAssure";
    /** string (enum) — obligatoire */
    public static final String PERIODICITE_COTISATION = "periodiciteCotisation";
    /** booleen — Option décès dégressif */
    public static final String DECES_DEGRESSIF = "decesDegressif";
    /** booleen — Option doublement décès accidentelle */
    public static final String DOUBLEMENT_DECES_ACCIDENTEL = "doublementDecesAccidentel";
    /** booleen — Option prise en charge des frais professionnels en cas d'ITT */
    public static final String FRAIS_PROFESSIONNELS_ITT = "fraisProfessionnelsItt";
    /** string (enum) — obligatoire */
    public static final String REMISE_COLLABORATEUR = "remiseCollaborateur";
    /** boolean — obligatoire */
    public static final String NUMERO_UID_PRESENT = "numeroUidPresent";
    /** string — obligatoire */
    public static final String CODE_AGENCE = "codeAgence";
    /** string — obligatoire */
    public static final String EMAIL_CONSEILLER = "emailConseiller";
    /** string — obligatoire */
    public static final String NOM_CONSEILLER = "nomConseiller";
    /** string — obligatoire */
    public static final String PRENOM_CONSEILLER = "prenomConseiller";
    /** booleen — Risque de séjour O/N */
    public static final String RISQUE_SEJOUR = "risqueSejour";
    /** string (enum) — obligatoire */
    public static final String CONVENTION_MADELIN = "conventionMadelin";
    /** string (enum) — obligatoire */
    public static final String SEGMENTATION = "segmentation";

    // D10 — RECUEIL_CONSENTEMENT
    /** booleen — Présence du recueil (O/N) — obligatoire */
    public static final String PRESENT_RECUEIL = "presentRecueil";
    /** booleen — Consentement données de santé (O/N) — obligatoire */
    public static final String CONSENTEMENT_DONNEES_SANTE = "consentementDonneesSante";
    /** booleen — Signature de l'assuré (O/N) — obligatoire */
    public static final String SIGNATURE_ASSURE = "signatureAssure";

    // QSS — QUESTIONNAIRE_SANTE_SIMPLIFIE
    /** booleen — Type (QSS simplifié / QS complet) — obligatoire */
    public static final String TYPE = "type";
    /** booleen — Émetteur du document (conseiller BNP / assuré) — obligatoire */
    public static final String EMETTEUR = "emetteur";
    /** booleen — Rempli à la main / scanné (O/N) — obligatoire */
    public static final String MANUSCRIT = "manuscrit";
    /** booleen — Recueil de consentement joint (O/N) — obligatoire */
    public static final String RECUEIL_CONSENTEMENT_JOINT = "recueilConsentementJoint";
    /** booleen — Date et signature et paraphes si on a 3 pages — obligatoire */
    public static final String PRESENT = "present";
    /** booleen — Date et signature et paraphes si on a 3 pages */
    public static final String PARAPHES_TOUTES_PAGES = "paraphesToutesPages";
    /** booleen — Je peux répondre "non" / je réponds oui — obligatoire */
    public static final String ALL_REPONSES_NEGATIVE = "allReponsesNegative";

    // D13 — MANDAT_SEPA
    /** string — obligatoire */
    public static final String IBAN = "iban";
    /** booleen — Titulaire du compte */
    public static final String TITULAIRE = "titulaire";
    /** booleen — Adresse postale — obligatoire */
    public static final String ADRESSE_TITULAIRE = "adresseTitulaire";
    /** booleen — BIC — obligatoire */
    public static final String BIC = "bic";

    // D14 — RIB
    /** string (enum) — obligatoire */
    public static final String TYPE_COMPTE = "typeCompte";
}
