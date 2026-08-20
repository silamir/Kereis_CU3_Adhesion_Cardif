package com.kereis.tahore.documentprocessing.domain.model;

/**
 * Chemins des champs extraits par Delos, tels que definis au contrat de champs.
 *
 * <p>Les regles metier passent par ces constantes et n'ecrivent jamais un chemin en
 * litteral. La convention de nommage cote Delos est encore en relecture : si une
 * variable est renommee, seule cette classe change, et le compilateur designe alors
 * chaque point d'usage. C'est ce qui rend les controles ecrivables des maintenant.
 *
 * <p>Genere depuis le contrat de champs. Ne pas editer a la main.
 */
public final class CheminsExtraction {

    private CheminsExtraction() {}

    // ---------------------------------------------------------------
    // D09 — BULLETIN_ADHESION
    // ---------------------------------------------------------------
    /** enumere : {@code contrat.typeProduit} */
    public static final String CONTRAT_TYPE_PRODUIT = "contrat.typeProduit";
    /** enumere : {@code assure.civilite} */
    public static final String ASSURE_CIVILITE = "assure.civilite";
    /** texte : {@code assure.prenom} */
    public static final String ASSURE_PRENOM = "assure.prenom";
    /** texte : {@code assure.nom} */
    public static final String ASSURE_NOM = "assure.nom";
    /** date AAAA-MM-JJ : {@code assure.dateNaissance} */
    public static final String ASSURE_DATE_NAISSANCE = "assure.dateNaissance";
    /** texte : {@code assure.lieuNaissance.commune} */
    public static final String ASSURE_LIEU_NAISSANCE_COMMUNE = "assure.lieuNaissance.commune";
    /** texte : {@code assure.lieuNaissance.departement} */
    public static final String ASSURE_LIEU_NAISSANCE_DEPARTEMENT = "assure.lieuNaissance.departement";
    /** texte : {@code assure.lieuNaissance.pays} */
    public static final String ASSURE_LIEU_NAISSANCE_PAYS = "assure.lieuNaissance.pays";
    /** texte : {@code assure.adresse.ligne1} */
    public static final String ASSURE_ADRESSE_LIGNE1 = "assure.adresse.ligne1";
    /** texte : {@code assure.adresse.pays} */
    public static final String ASSURE_ADRESSE_PAYS = "assure.adresse.pays";
    /** texte : {@code assure.adresse.codePostal} */
    public static final String ASSURE_ADRESSE_CODE_POSTAL = "assure.adresse.codePostal";
    /** texte : {@code assure.adresse.commune} */
    public static final String ASSURE_ADRESSE_COMMUNE = "assure.adresse.commune";
    /** texte : {@code assure.contact.telephone} */
    public static final String ASSURE_CONTACT_TELEPHONE = "assure.contact.telephone";
    /** texte : {@code assure.contact.email} */
    public static final String ASSURE_CONTACT_EMAIL = "assure.contact.email";
    /** texte : {@code assure.nationalite} */
    public static final String ASSURE_NATIONALITE = "assure.nationalite";
    /** texte : {@code assure.activite.profession} */
    public static final String ASSURE_ACTIVITE_PROFESSION = "assure.activite.profession";
    /** enumere : {@code assure.activite.categorieSocioProfessionnelle} */
    public static final String ASSURE_ACTIVITE_CATEGORIE_SOCIO_PROFESSIONNELLE = "assure.activite.categorieSocioProfessionnelle";
    /** booleen : {@code assure.activite.sansActiviteRemuneree} */
    public static final String ASSURE_ACTIVITE_SANS_ACTIVITE_REMUNEREE = "assure.activite.sansActiviteRemuneree";
    /** montant EUR : {@code assure.revenus.netFiscalN1} */
    public static final String ASSURE_REVENUS_NET_FISCAL_N1 = "assure.revenus.netFiscalN1";
    /** montant EUR : {@code assure.revenus.netFiscalN2} */
    public static final String ASSURE_REVENUS_NET_FISCAL_N2 = "assure.revenus.netFiscalN2";
    /** booleen : {@code assure.sante.fumeur} */
    public static final String ASSURE_SANTE_FUMEUR = "assure.sante.fumeur";
    /** enumere : {@code garanties.codeFormule} */
    public static final String GARANTIES_CODE_FORMULE = "garanties.codeFormule";
    /** montant EUR : {@code garanties.capitalDeces} */
    public static final String GARANTIES_CAPITAL_DECES = "garanties.capitalDeces";
    /** enumere : {@code garanties.modaliteVersement} */
    public static final String GARANTIES_MODALITE_VERSEMENT = "garanties.modaliteVersement";
    /** entier : {@code garanties.dureeRenteAnnees} */
    public static final String GARANTIES_DUREE_RENTE_ANNEES = "garanties.dureeRenteAnnees";
    /** date AAAA-MM-JJ : {@code signatures.dateSignature} */
    public static final String SIGNATURES_DATE_SIGNATURE = "signatures.dateSignature";
    /** booleen : {@code signatures.paraphesAssure.present} */
    public static final String SIGNATURES_PARAPHES_ASSURE_PRESENT = "signatures.paraphesAssure.present";
    /** booleen : {@code signatures.personneHabilitee.present} */
    public static final String SIGNATURES_PERSONNE_HABILITEE_PRESENT = "signatures.personneHabilitee.present";
    /** booleen : {@code signatures.assure.present} */
    public static final String SIGNATURES_ASSURE_PRESENT = "signatures.assure.present";
    /** enumere : {@code beneficiairesDeces.typeClause} */
    public static final String BENEFICIAIRES_DECES_TYPE_CLAUSE = "beneficiairesDeces.typeClause";
    /** enumere : {@code beneficiairesDeces.designes[].civilite} */
    public static final String BENEFICIAIRES_DECES_DESIGNES_CIVILITE = "beneficiairesDeces.designes[].civilite";
    /** texte : {@code beneficiairesDeces.designes[].nom} */
    public static final String BENEFICIAIRES_DECES_DESIGNES_NOM = "beneficiairesDeces.designes[].nom";
    /** texte : {@code beneficiairesDeces.designes[].nomNaissance} */
    public static final String BENEFICIAIRES_DECES_DESIGNES_NOM_NAISSANCE = "beneficiairesDeces.designes[].nomNaissance";
    /** texte : {@code beneficiairesDeces.designes[].prenom} */
    public static final String BENEFICIAIRES_DECES_DESIGNES_PRENOM = "beneficiairesDeces.designes[].prenom";
    /** objet : {@code beneficiairesDeces.designes[].adresse} */
    public static final String BENEFICIAIRES_DECES_DESIGNES_ADRESSE = "beneficiairesDeces.designes[].adresse";
    /** nombre : {@code beneficiairesDeces.designes[].partPourcentage} */
    public static final String BENEFICIAIRES_DECES_DESIGNES_PART_POURCENTAGE = "beneficiairesDeces.designes[].partPourcentage";
    /** booleen : {@code souscripteur.identiqueAssure} */
    public static final String SOUSCRIPTEUR_IDENTIQUE_ASSURE = "souscripteur.identiqueAssure";
    /** enumere : {@code reglement.periodicite} */
    public static final String REGLEMENT_PERIODICITE = "reglement.periodicite";
    /** booleen : {@code garanties.options.decesDegressif} */
    public static final String GARANTIES_OPTIONS_DECES_DEGRESSIF = "garanties.options.decesDegressif";
    /** booleen : {@code garanties.options.doublementDecesAccidentel} */
    public static final String GARANTIES_OPTIONS_DOUBLEMENT_DECES_ACCIDENTEL = "garanties.options.doublementDecesAccidentel";
    /** booleen : {@code garanties.options.fraisProfessionnelsItt} */
    public static final String GARANTIES_OPTIONS_FRAIS_PROFESSIONNELS_ITT = "garanties.options.fraisProfessionnelsItt";
    /** texte : {@code distribution.codeAgence} */
    public static final String DISTRIBUTION_CODE_AGENCE = "distribution.codeAgence";
    /** texte : {@code conseiller.email} */
    public static final String CONSEILLER_EMAIL = "conseiller.email";
    /** texte : {@code conseiller.nom} */
    public static final String CONSEILLER_NOM = "conseiller.nom";
    /** texte : {@code conseiller.prenom} */
    public static final String CONSEILLER_PRENOM = "conseiller.prenom";
    /** booleen : {@code assure.sante.risqueSejour} */
    public static final String ASSURE_SANTE_RISQUE_SEJOUR = "assure.sante.risqueSejour";
    /** enumere : {@code contrat.conventionMadelin} */
    public static final String CONTRAT_CONVENTION_MADELIN = "contrat.conventionMadelin";
    /** enumere : {@code contrat.segmentation} */
    public static final String CONTRAT_SEGMENTATION = "contrat.segmentation";

    // ---------------------------------------------------------------
    // D10 — RECUEIL_CONSENTEMENT
    // ---------------------------------------------------------------
    /** booleen : {@code recueil.present} */
    public static final String RECUEIL_PRESENT = "recueil.present";
    /** booleen : {@code consentement.donneesSante} */
    public static final String CONSENTEMENT_DONNEES_SANTE = "consentement.donneesSante";

    // ---------------------------------------------------------------
    // QSS — QUESTIONNAIRE_SANTE_SIMPLIFIE
    // ---------------------------------------------------------------
    /** enumere : {@code questionnaire.type} */
    public static final String QUESTIONNAIRE_TYPE = "questionnaire.type";
    /** enumere : {@code questionnaire.emetteur} */
    public static final String QUESTIONNAIRE_EMETTEUR = "questionnaire.emetteur";
    /** booleen : {@code questionnaire.manuscrit} */
    public static final String QUESTIONNAIRE_MANUSCRIT = "questionnaire.manuscrit";
    /** booleen : {@code questionnaire.recueilConsentementJoint} */
    public static final String QUESTIONNAIRE_RECUEIL_CONSENTEMENT_JOINT = "questionnaire.recueilConsentementJoint";
    /** booleen : {@code questionnaire.reponsePositivePresente} */
    public static final String QUESTIONNAIRE_REPONSE_POSITIVE_PRESENTE = "questionnaire.reponsePositivePresente";

    // ---------------------------------------------------------------
    // D13 — MANDAT_SEPA
    // ---------------------------------------------------------------
    /** IBAN : {@code banque.iban} */
    public static final String BANQUE_IBAN = "banque.iban";
    /** texte : {@code banque.titulaire} */
    public static final String BANQUE_TITULAIRE = "banque.titulaire";
    /** objet : {@code banque.adresseTitulaire} */
    public static final String BANQUE_ADRESSE_TITULAIRE = "banque.adresseTitulaire";
    /** BIC : {@code banque.bic} */
    public static final String BANQUE_BIC = "banque.bic";

    // ---------------------------------------------------------------
    // D14 — RIB
    // ---------------------------------------------------------------
    /** enumere : {@code banque.typeCompte} */
    public static final String BANQUE_TYPE_COMPTE = "banque.typeCompte";
}
