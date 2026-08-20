package com.kereis.tahore.documentprocessing.domain.controle;

import static com.kereis.tahore.documentprocessing.domain.model.CheminsExtraction.ALL_REPONSES_NEGATIVE;

import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.TypeDocument;
import java.util.Optional;

/**
 * RG-2.3.1 — recevabilite du questionnaire de sante simplifie.
 *
 * <p>Un QSS dont une reponse est « oui » rend le dossier non recevable et declenche la
 * demande d'un questionnaire complet. Delos evalue la condition et renvoie
 * {@code allReponsesNegative} : vrai si l'assure a pu repondre « non » partout.
 *
 * <p>Ce controle ne lit aucune donnee de sante. Il lit un booleen de synthese, ce qui est
 * exactement ce dont le cloisonnement des donnees de sante a besoin : le detail des
 * reponses ne traverse jamais le module.
 */
public final class ControleRecevabiliteQss {

    public static final String REGLE = "RG-2.3.1";

    private ControleRecevabiliteQss() {}

    public static Verdict verifier(DocumentEntrant document, double seuil) {
        if (document.type() != TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE) {
            return Verdict.NON_APPLICABLE;
        }
        Optional<Boolean> reponse = document.donneesExtraites()
                .flatMap(d -> d.champ(ALL_REPONSES_NEGATIVE))
                .filter(c -> c.fiable(seuil))
                .map(c -> c.vrai());
        if (reponse.isEmpty()) {
            // Condition absente ou peu sure : le module ne tranche pas a la place du
            // gestionnaire sur une donnee de sante.
            return Verdict.A_VERIFIER;
        }
        return reponse.orElseThrow() ? Verdict.RECEVABLE : Verdict.NON_RECEVABLE_QS_COMPLET_ATTENDU;
    }

    public enum Verdict {
        /** Toutes les reponses sont negatives : le dossier suit le chemin nominal. */
        RECEVABLE,
        /** Au moins une reponse positive : un questionnaire complet est demande. */
        NON_RECEVABLE_QS_COMPLET_ATTENDU,
        /** Condition absente ou sous le seuil : revue manuelle. */
        A_VERIFIER,
        /** Le document n'est pas un QSS. */
        NON_APPLICABLE
    }
}
