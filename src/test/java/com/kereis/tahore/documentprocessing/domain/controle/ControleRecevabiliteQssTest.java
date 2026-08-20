package com.kereis.tahore.documentprocessing.domain.controle;

import static com.kereis.tahore.documentprocessing.domain.model.CheminsExtraction.ALL_REPONSES_NEGATIVE;
import static org.assertj.core.api.Assertions.assertThat;

import com.kereis.tahore.documentprocessing.domain.controle.ControleRecevabiliteQss.Verdict;
import com.kereis.tahore.documentprocessing.domain.model.ChampExtrait;
import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.DonneesExtraites;
import com.kereis.tahore.documentprocessing.domain.model.TypeDocument;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ControleRecevabiliteQssTest {

    private static final double SEUIL = 0.90d;

    private static DocumentEntrant qss(Map<String, ChampExtrait> champs) {
        return new DocumentEntrant(
                "doc-qss", TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE, "REC-1", 3,
                Instant.parse("2026-08-20T08:00:00Z"), true, Optional.empty(),
                champs == null ? Optional.empty() : Optional.of(new DonneesExtraites(champs)));
    }

    @Test
    @DisplayName("RG-2.3.1 — toutes les reponses negatives : le dossier est recevable")
    void toutes_reponses_negatives() {
        assertThat(ControleRecevabiliteQss.verifier(
                        qss(Map.of(ALL_REPONSES_NEGATIVE, new ChampExtrait(true, 0.97d))), SEUIL))
                .isEqualTo(Verdict.RECEVABLE);
    }

    @Test
    @DisplayName("RG-2.3.1 — une reponse positive declenche la demande d'un questionnaire complet")
    void une_reponse_positive() {
        assertThat(ControleRecevabiliteQss.verifier(
                        qss(Map.of(ALL_REPONSES_NEGATIVE, new ChampExtrait(false, 0.97d))), SEUIL))
                .isEqualTo(Verdict.NON_RECEVABLE_QS_COMPLET_ATTENDU);
    }

    @Test
    @DisplayName("RG-2.3.1 — une condition peu sure ne se tranche pas : revue manuelle")
    void condition_peu_sure() {
        assertThat(ControleRecevabiliteQss.verifier(
                        qss(Map.of(ALL_REPONSES_NEGATIVE, new ChampExtrait(true, 0.55d))), SEUIL))
                .isEqualTo(Verdict.A_VERIFIER);
    }

    @Test
    @DisplayName("RG-2.3.1 — condition absente : revue manuelle, pas de decision par defaut")
    void condition_absente() {
        assertThat(ControleRecevabiliteQss.verifier(qss(Map.of()), SEUIL))
                .isEqualTo(Verdict.A_VERIFIER);
        assertThat(ControleRecevabiliteQss.verifier(qss(null), SEUIL))
                .isEqualTo(Verdict.A_VERIFIER);
    }

    @Test
    @DisplayName("RG-2.3.1 — le controle ne s'applique pas a un RIB")
    void non_applicable() {
        DocumentEntrant rib = new DocumentEntrant(
                "doc-rib", TypeDocument.RIB, "REC-1", 5,
                Instant.parse("2026-08-20T08:00:00Z"), false, Optional.empty(), Optional.empty());

        assertThat(ControleRecevabiliteQss.verifier(rib, SEUIL)).isEqualTo(Verdict.NON_APPLICABLE);
    }
}
