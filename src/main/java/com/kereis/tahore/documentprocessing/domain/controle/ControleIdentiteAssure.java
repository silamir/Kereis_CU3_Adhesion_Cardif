package com.kereis.tahore.documentprocessing.domain.controle;

import static com.kereis.tahore.documentprocessing.domain.model.CheminsExtraction.ASSURE_DATE_NAISSANCE;
import static com.kereis.tahore.documentprocessing.domain.model.CheminsExtraction.ASSURE_NOM;
import static com.kereis.tahore.documentprocessing.domain.model.CheminsExtraction.ASSURE_PRENOM;

import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.DonneesExtraites;
import com.kereis.tahore.documentprocessing.domain.model.TypeDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gabarit d'un controle metier, a decliner pour les 80 assertions des 14 fiches.
 *
 * <p>Trois proprietes rendent ces controles ecrivables et testables aujourd'hui, avant
 * que les points ouverts ne soient tranches.
 *
 * <ol>
 *   <li>Ils ne dependent que du domaine : ni Spring, ni forme du DMS, ni client HTTP. Un
 *       test est une construction d'objet et une assertion.
 *   <li>Ils lisent les donnees par {@link
 *       com.kereis.tahore.documentprocessing.domain.model.CheminsExtraction}, jamais par
 *       un chemin en litteral. Un renommage cote Delos ne touche qu'une classe.
 *   <li>Ils retournent un verdict explicite plutot que de lever une exception : un lot
 *       incomplet n'est pas une erreur technique, c'est un resultat metier.
 * </ol>
 */
public final class ControleIdentiteAssure {

    /** Identifiant de la regle au referentiel des fiches de controle. */
    public static final String REGLE = "RG-2.1.1";

    private ControleIdentiteAssure() {}

    public static Verdict verifier(DocumentEntrant document) {
        if (document.type() != TypeDocument.BULLETIN_ADHESION) {
            return Verdict.nonApplicable(REGLE);
        }
        Optional<DonneesExtraites> donnees = document.donneesExtraites();
        if (donnees.isEmpty()) {
            return Verdict.enEchec(REGLE, List.of("aucune donnee extraite"));
        }

        List<String> manquants = new ArrayList<>();
        for (String chemin : List.of(ASSURE_NOM, ASSURE_PRENOM, ASSURE_DATE_NAISSANCE)) {
            if (donnees.orElseThrow().champ(chemin).isEmpty()) {
                manquants.add(chemin);
            }
        }
        return manquants.isEmpty() ? Verdict.conforme(REGLE) : Verdict.enEchec(REGLE, manquants);
    }

    /**
     * Resultat d'un controle.
     *
     * @param regle identifiant de la regle appliquee
     * @param etat conforme, en echec, ou non applicable au document soumis
     * @param motifs chemins ou libelles expliquant un echec, vide sinon
     */
    public record Verdict(String regle, Etat etat, List<String> motifs) {

        public enum Etat {
            CONFORME,
            EN_ECHEC,
            NON_APPLICABLE
        }

        static Verdict conforme(String regle) {
            return new Verdict(regle, Etat.CONFORME, List.of());
        }

        static Verdict enEchec(String regle, List<String> motifs) {
            return new Verdict(regle, Etat.EN_ECHEC, List.copyOf(motifs));
        }

        static Verdict nonApplicable(String regle) {
            return new Verdict(regle, Etat.NON_APPLICABLE, List.of());
        }
    }
}
