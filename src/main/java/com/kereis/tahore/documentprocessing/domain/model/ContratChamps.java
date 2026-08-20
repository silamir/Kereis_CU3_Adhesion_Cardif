package com.kereis.tahore.documentprocessing.domain.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrat de champs, transcrit depuis la source de verite.
 *
 * <p>Le classeur distingue deux natures de retour, et la distinction porte sur le
 * <b>type</b> de ce que Delos renvoie :
 *
 * <ul>
 *   <li><b>« A extraire »</b> — Delos renvoie la valeur brute, du type declare.
 *   <li><b>« A verifier / controle »</b> — Delos renvoie un <b>booleen</b> indiquant que
 *       la condition est remplie. Le libelle du classeur dit laquelle.
 * </ul>
 *
 * <p>Une seule ligne n'est pas renvoyee du tout, celle marquee « ne pas extraire » : la
 * coherence RIB / mandat SEPA est calculee par le module.
 *
 * <p>Genere depuis {@code contracts/delos-contrat-champs.json}. Ne pas editer a la main.
 */
public final class ContratChamps {

    private ContratChamps() {}

    /** Ce que Delos renvoie pour un champ. */
    public enum NatureRetour {
        /** La valeur brute, du type declare au contrat. */
        VALEUR,
        /** Un booleen : la condition decrite par le libelle est remplie, ou non. */
        CONDITION,
        /** Rien : le module calcule lui-meme. */
        NON_RENVOYE
    }

    /**
     * @param variable nom retenu pour le champ
     * @param variableClasseur nom au classeur, non nul seulement si un renommage a eu lieu
     * @param document type de document porteur
     * @param nature ce que Delos renvoie : valeur, condition, ou rien
     * @param obligatoire colonne « Obligatoire »
     * @param typeDeclare type declare au contrat, pertinent pour une VALEUR
     * @param enumeration nom de l'enumeration, {@code null} si le champ n'est pas enumere
     * @param valeursEnumeration valeurs connues ; vide si elles restent a obtenir
     * @param libelle libelle au dictionnaire, cite tel quel. Pour une CONDITION, c'est lui
     *     qui dit ce que le booleen affirme.
     */
    public record Champ(
            String variable,
            String variableClasseur,
            TypeDocument document,
            NatureRetour nature,
            boolean obligatoire,
            String typeDeclare,
            String enumeration,
            List<String> valeursEnumeration,
            String libelle) {

        public Champ {
            valeursEnumeration = List.copyOf(valeursEnumeration);
        }

        /** Type effectivement attendu dans {@code metadata}. */
        public String typeEffectif() {
            return nature == NatureRetour.CONDITION ? "boolean" : typeDeclare;
        }

        public boolean renvoyeParDelos() {
            return nature != NatureRetour.NON_RENVOYE;
        }

        public boolean enumerationAObtenir() {
            return enumeration != null && valeursEnumeration.isEmpty();
        }

        public boolean renomme() {
            return variableClasseur != null;
        }

        /**
         * Vrai si le classeur se contredit : un champ annonce comme condition, donc
         * booleen, ne peut pas porter de valeur enumeree.
         */
        public boolean contradictionEnumerationCondition() {
            return nature == NatureRetour.CONDITION && enumeration != null;
        }
    }

    private static final List<Champ> TOUS = List.of(
            new Champ(
                    "typeProduit",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string (enum)",
                    "TYPE_PRODUIT",
                    List.of(),
                    "Type (de bulletin / produit)"),
            new Champ(
                    "civilite",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string (enum)",
                    "CIVILITE",
                    List.of("M", "MME", "MLLE"),
                    "Civilité"),
            new Champ(
                    "prenom",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Prénom"),
            new Champ(
                    "nom",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Nom"),
            new Champ(
                    "dateNaissance",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Date de naissance"),
            new Champ(
                    "communeNaissance",
                    "commune",
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Lieu de naissance"),
            new Champ(
                    "departement",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Département de naissance"),
            new Champ(
                    "paysNaissance",
                    "pays",
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Pays de naissance"),
            new Champ(
                    "adresse",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Numéro de voie / adresse"),
            new Champ(
                    "pays",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Pays (adresse)"),
            new Champ(
                    "codePostal",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Code postal"),
            new Champ(
                    "commune",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Localité"),
            new Champ(
                    "telephone",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Numéro de téléphone"),
            new Champ(
                    "email",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Email"),
            new Champ(
                    "nationalite",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Nationalité"),
            new Champ(
                    "profession",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Profession exacte"),
            new Champ(
                    "categorieSocioProfessionnelle",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string (enum)",
                    "CSP",
                    List.of(),
                    "Catégorie socio-professionnelle (CSP)"),
            new Champ(
                    "sansActiviteRemuneree",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.CONDITION,
                    false,
                    "boolean",
                    null,
                    List.of(),
                    "Sans activité professionnelle rémunérée (O/N)"),
            new Champ(
                    "netFiscalN1",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "number",
                    null,
                    List.of(),
                    "Derniers revenus annuels nets fiscaux déclarés (N-1)"),
            new Champ(
                    "netFiscalN2",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "number",
                    null,
                    List.of(),
                    "Précédents revenus annuels nets fiscaux déclarés (N-2)"),
            new Champ(
                    "fumeur",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "boolean",
                    null,
                    List.of(),
                    "Fumeur (O/N)"),
            new Champ(
                    "codeFormule",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string (enum)",
                    "FORMULE_GARANTIE",
                    List.of("FORMULE_1", "FORMULE_2", "FORMULE_3", "FORMULE_4", "FORMULE_5"),
                    "Formule de garantie choisie"),
            new Champ(
                    "capitalDeces",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "number",
                    null,
                    List.of(),
                    "Capital décès (montant assuré)"),
            new Champ(
                    "modaliteVersement",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string (enum)",
                    "MODALITE_VERSEMENT",
                    List.of("CAPITAL", "RENTE_FORFAITAIRE"),
                    "Modalités de versement (capital / rente forfaitaire)"),
            new Champ(
                    "dureeRenteAnnees",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    false,
                    "integer",
                    null,
                    List.of(),
                    "Durée de la rente (si rente)"),
            new Champ(
                    "dateSignature",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Date de signature"),
            new Champ(
                    "presenceParaphes",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.CONDITION,
                    false,
                    "boolean",
                    null,
                    List.of(),
                    "Paraphes de l'adhésion / assuré (présent O/N)"),
            new Champ(
                    "presentSignatureHabilite",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.CONDITION,
                    false,
                    "boolean",
                    null,
                    List.of(),
                    "Signature de la personne habilitée si applicable"),
            new Champ(
                    "presentSignatureAssure",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.CONDITION,
                    false,
                    "boolean",
                    null,
                    List.of(),
                    "Signature de l'assuré (présent O/N)"),
            new Champ(
                    "typeClauseDeces",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string (enum)",
                    "TYPE_CLAUSE_BENEFICIAIRE",
                    List.of("DESIGNES", "CLAUSE_LIBRE", "CLAUSE_STANDARD"),
                    "Bénéficiaires décès - type (désignés / clause libre / clause standard)"),
            new Champ(
                    "beneficiaireCivilite",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    false,
                    "string (enum)",
                    "CIVILITE",
                    List.of("M", "MME", "MLLE"),
                    "Bénéficiaire désigné - civilité"),
            new Champ(
                    "beneficiaireNom",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    false,
                    "string",
                    null,
                    List.of(),
                    "Bénéficiaire désigné - nom"),
            new Champ(
                    "beneficiaireNomNaissance",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    false,
                    "string",
                    null,
                    List.of(),
                    "Bénéficiaire désigné - nom de naissance"),
            new Champ(
                    "beneficiairePrenom",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    false,
                    "string",
                    null,
                    List.of(),
                    "Bénéficiaire désigné - prénom"),
            new Champ(
                    "beneficiaireAdresse",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    false,
                    "object",
                    null,
                    List.of(),
                    "Bénéficiaire désigné - adresse"),
            new Champ(
                    "beneficiaireDateNaissance",
                    "dateNaissance",
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    false,
                    "string",
                    null,
                    List.of(),
                    "Bénéficiaire désigné - date et lieu de naissance"),
            new Champ(
                    "lieuNaissance",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    false,
                    "string",
                    null,
                    List.of(),
                    "Bénéficiaire désigné - date et lieu de naissance"),
            new Champ(
                    "beneficiairePartPourcentage",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    false,
                    "number",
                    null,
                    List.of(),
                    "Bénéficiaire désigné - part attribuée (%)"),
            new Champ(
                    "identiqueAssure",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.CONDITION,
                    false,
                    "boolean",
                    null,
                    List.of(),
                    "Souscripteur = assuré (O/N)"),
            new Champ(
                    "periodiciteCotisation",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string (enum)",
                    "PERIODICITE",
                    List.of("MENSUELLE", "TRIMESTRIELLE", "SEMESTRIELLE", "ANNUELLE"),
                    "Débiteur - périodicité des cotisations"),
            new Champ(
                    "decesDegressif",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.CONDITION,
                    false,
                    "boolean",
                    null,
                    List.of(),
                    "Option décès dégressif"),
            new Champ(
                    "doublementDecesAccidentel",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.CONDITION,
                    false,
                    "boolean",
                    null,
                    List.of(),
                    "Option doublement décès accidentelle"),
            new Champ(
                    "fraisProfessionnelsItt",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.CONDITION,
                    false,
                    "boolean",
                    null,
                    List.of(),
                    "Option prise en charge des frais professionnels en cas d'ITT"),
            new Champ(
                    "remiseCollaborateur",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string (enum)",
                    "REMISE_COLLABORATEUR",
                    List.of("AUCUNE", "REMISE_15"),
                    "Remise collaborateur qui de 15% / présence du numéro UID"),
            new Champ(
                    "numeroUidPresent",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "boolean",
                    null,
                    List.of(),
                    "Remise collaborateur qui de 15% / présence du numéro UID"),
            new Champ(
                    "codeAgence",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Code agence"),
            new Champ(
                    "emailConseiller",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Email du conseiller"),
            new Champ(
                    "nomConseiller",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Nom du conseiller"),
            new Champ(
                    "prenomConseiller",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Prénom du conseiller"),
            new Champ(
                    "risqueSejour",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.CONDITION,
                    false,
                    "boolean",
                    null,
                    List.of(),
                    "Risque de séjour O/N"),
            new Champ(
                    "conventionMadelin",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string (enum)",
                    "CONVENTION_MADELIN",
                    List.of("CONVENTION_2289_NON_MADELIN", "CONVENTION_2290_MADELIN"),
                    "Convention Madelin (2289/2290)"),
            new Champ(
                    "segmentation",
                    null,
                    TypeDocument.BULLETIN_ADHESION,
                    NatureRetour.VALEUR,
                    true,
                    "string (enum)",
                    "SEGMENTATION",
                    List.of("STANDARD", "VIP"),
                    "Segmentation (standard / VIP)"),
            new Champ(
                    "presentRecueil",
                    null,
                    TypeDocument.RECUEIL_CONSENTEMENT,
                    NatureRetour.CONDITION,
                    true,
                    "boolean",
                    null,
                    List.of(),
                    "Présence du recueil (O/N)"),
            new Champ(
                    "consentementDonneesSante",
                    null,
                    TypeDocument.RECUEIL_CONSENTEMENT,
                    NatureRetour.CONDITION,
                    true,
                    "boolean",
                    null,
                    List.of(),
                    "Consentement données de santé (O/N)"),
            new Champ(
                    "signatureAssure",
                    null,
                    TypeDocument.RECUEIL_CONSENTEMENT,
                    NatureRetour.CONDITION,
                    true,
                    "boolean",
                    null,
                    List.of(),
                    "Signature de l'assuré (O/N)"),
            new Champ(
                    "dateSignature",
                    null,
                    TypeDocument.RECUEIL_CONSENTEMENT,
                    NatureRetour.CONDITION,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Date de signature"),
            new Champ(
                    "nom",
                    null,
                    TypeDocument.RECUEIL_CONSENTEMENT,
                    NatureRetour.CONDITION,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Nom , prenom et date de naissance si meme sur le BA"),
            new Champ(
                    "prenom",
                    null,
                    TypeDocument.RECUEIL_CONSENTEMENT,
                    NatureRetour.CONDITION,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Nom , prenom et date de naissance si meme sur le BA"),
            new Champ(
                    "dateNaissance",
                    null,
                    TypeDocument.RECUEIL_CONSENTEMENT,
                    NatureRetour.CONDITION,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Nom , prenom et date de naissance si meme sur le BA"),
            new Champ(
                    "type",
                    null,
                    TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE,
                    NatureRetour.CONDITION,
                    true,
                    "string (enum)",
                    "TYPE_QUESTIONNAIRE",
                    List.of("QSS_SIMPLIFIE", "QS_COMPLET"),
                    "Type (QSS simplifié / QS complet)"),
            new Champ(
                    "emetteur",
                    null,
                    TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE,
                    NatureRetour.CONDITION,
                    true,
                    "string (enum)",
                    "EMETTEUR_QUESTIONNAIRE",
                    List.of("CONSEILLER_BNP", "ASSURE"),
                    "Émetteur du document (conseiller BNP / assuré)"),
            new Champ(
                    "manuscrit",
                    null,
                    TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE,
                    NatureRetour.CONDITION,
                    true,
                    "boolean",
                    null,
                    List.of(),
                    "Rempli à la main / scanné (O/N)"),
            new Champ(
                    "recueilConsentementJoint",
                    null,
                    TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE,
                    NatureRetour.CONDITION,
                    true,
                    "boolean",
                    null,
                    List.of(),
                    "Recueil de consentement joint (O/N)"),
            new Champ(
                    "dateSignature",
                    null,
                    TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE,
                    NatureRetour.CONDITION,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Date et signature et paraphes si on a 3 pages"),
            new Champ(
                    "present",
                    null,
                    TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE,
                    NatureRetour.CONDITION,
                    true,
                    "boolean",
                    null,
                    List.of(),
                    "Date et signature et paraphes si on a 3 pages"),
            new Champ(
                    "paraphesToutesPages",
                    null,
                    TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE,
                    NatureRetour.CONDITION,
                    false,
                    "boolean",
                    null,
                    List.of(),
                    "Date et signature et paraphes si on a 3 pages"),
            new Champ(
                    "allReponsesNegative",
                    null,
                    TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE,
                    NatureRetour.CONDITION,
                    true,
                    "boolean",
                    null,
                    List.of(),
                    "Je peux répondre \"non\" / je réponds oui"),
            new Champ(
                    "iban",
                    null,
                    TypeDocument.MANDAT_SEPA,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "IBAN"),
            new Champ(
                    "titulaire",
                    null,
                    TypeDocument.MANDAT_SEPA,
                    NatureRetour.CONDITION,
                    false,
                    "string",
                    null,
                    List.of(),
                    "Titulaire du compte"),
            new Champ(
                    "dateSignature",
                    null,
                    TypeDocument.MANDAT_SEPA,
                    NatureRetour.CONDITION,
                    false,
                    "string",
                    null,
                    List.of(),
                    "Date et signature"),
            new Champ(
                    "present",
                    null,
                    TypeDocument.MANDAT_SEPA,
                    NatureRetour.CONDITION,
                    false,
                    "boolean",
                    null,
                    List.of(),
                    "Date et signature"),
            new Champ(
                    "— ne pas extraire —",
                    null,
                    TypeDocument.MANDAT_SEPA,
                    NatureRetour.NON_RENVOYE,
                    false,
                    "—",
                    null,
                    List.of(),
                    "Cohérence RIB / mandat SEPA (O/N)"),
            new Champ(
                    "adresseTitulaire",
                    null,
                    TypeDocument.MANDAT_SEPA,
                    NatureRetour.CONDITION,
                    true,
                    "object",
                    null,
                    List.of(),
                    "Adresse postale"),
            new Champ(
                    "bic",
                    null,
                    TypeDocument.MANDAT_SEPA,
                    NatureRetour.CONDITION,
                    true,
                    "string",
                    null,
                    List.of(),
                    "BIC"),
            new Champ(
                    "iban",
                    null,
                    TypeDocument.RIB,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "IBAN"),
            new Champ(
                    "bic",
                    null,
                    TypeDocument.RIB,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "BIC"),
            new Champ(
                    "titulaire",
                    null,
                    TypeDocument.RIB,
                    NatureRetour.VALEUR,
                    true,
                    "string",
                    null,
                    List.of(),
                    "Titulaire du compte"),
            new Champ(
                    "typeCompte",
                    null,
                    TypeDocument.RIB,
                    NatureRetour.VALEUR,
                    true,
                    "string (enum)",
                    "TYPE_COMPTE",
                    List.of("PERSONNEL", "PROFESSIONNEL"),
                    "Type de compte (personnel / professionnel)"),
            new Champ(
                    "adresseTitulaire",
                    null,
                    TypeDocument.RIB,
                    NatureRetour.VALEUR,
                    false,
                    "object",
                    null,
                    List.of(),
                    "Adresse ( pas très important )")
            );

    private static final Map<TypeDocument, List<Champ>> PAR_DOCUMENT =
            TOUS.stream().collect(Collectors.groupingBy(Champ::document));

    public static List<Champ> tous() {
        return TOUS;
    }

    public static List<Champ> du(TypeDocument document) {
        return PAR_DOCUMENT.getOrDefault(document, List.of());
    }

    public static Optional<Champ> champ(TypeDocument document, String variable) {
        return du(document).stream().filter(c -> c.variable().equals(variable)).findFirst();
    }

    /**
     * Champs obligatoires d'un document.
     *
     * <p>Repond au point ouvert « liste exhaustive des champs obligatoires du bulletin » :
     * la liste ne se tient plus a la main, elle se lit dans le classeur.
     */
    public static List<String> obligatoires(TypeDocument document) {
        return du(document).stream().filter(Champ::obligatoire).map(Champ::variable).toList();
    }

    /** Champs attendus dans {@code metadata}, valeurs et conditions confondues. */
    public static List<String> attendus(TypeDocument document) {
        return du(document).stream()
                .filter(Champ::renvoyeParDelos)
                .map(Champ::variable)
                .toList();
    }

    /** Champs dont Delos renvoie un booleen de condition. */
    public static List<String> conditions(TypeDocument document) {
        return du(document).stream()
                .filter(c -> c.nature() == NatureRetour.CONDITION)
                .map(Champ::variable)
                .toList();
    }

    /** Champs dont Delos renvoie la valeur brute. */
    public static List<String> valeurs(TypeDocument document) {
        return du(document).stream()
                .filter(c -> c.nature() == NatureRetour.VALEUR)
                .map(Champ::variable)
                .toList();
    }

    public static List<Champ> enumerationsAObtenir() {
        return TOUS.stream().filter(Champ::enumerationAObtenir).toList();
    }

    /** Champs renommes faute d'unicite au classeur. A valider par le metier. */
    public static List<Champ> renommes() {
        return TOUS.stream().filter(Champ::renomme).toList();
    }

    /**
     * Contradictions du classeur : champ annonce comme condition, donc booleen, mais
     * portant une enumeration. Un booleen ne peut pas porter {@code QSS_SIMPLIFIE}.
     */
    public static List<Champ> contradictions() {
        return TOUS.stream().filter(Champ::contradictionEnumerationCondition).toList();
    }
}
