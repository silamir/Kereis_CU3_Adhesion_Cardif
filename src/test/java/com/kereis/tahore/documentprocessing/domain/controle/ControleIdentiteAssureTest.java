package com.kereis.tahore.documentprocessing.domain.controle;

import static org.assertj.core.api.Assertions.assertThat;

import com.kereis.tahore.documentprocessing.domain.controle.ControleIdentiteAssure.Verdict;
import com.kereis.tahore.documentprocessing.domain.model.CheminsExtraction;
import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.DonneesExtraites;
import com.kereis.tahore.documentprocessing.domain.model.TypeDocument;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Montre qu'un controle metier se teste sans contexte applicatif : aucune annotation
 * Spring, aucun demarrage, aucun reseau.
 */
class ControleIdentiteAssureTest {

    private static DocumentEntrant bulletin(Map<String, Object> champs) {
        return new DocumentEntrant(
                "doc-1",
                TypeDocument.BULLETIN_ADHESION,
                "REC-1",
                1,
                Instant.parse("2026-08-20T08:00:00Z"),
                false,
                Optional.empty(),
                Optional.of(new DonneesExtraites(0.94d, false, champs)));
    }

    @Test
    @DisplayName("RG-2.1.1 — un bulletin portant nom, prenom et date de naissance est conforme")
    void identite_complete() {
        Verdict v = ControleIdentiteAssure.verifier(bulletin(Map.of(
                CheminsExtraction.ASSURE_NOM, "MARTIN",
                CheminsExtraction.ASSURE_PRENOM, "Camille",
                CheminsExtraction.ASSURE_DATE_NAISSANCE, "1985-04-17")));

        assertThat(v.etat()).isEqualTo(Verdict.Etat.CONFORME);
        assertThat(v.motifs()).isEmpty();
    }

    @Test
    @DisplayName("RG-2.1.1 — la date de naissance absente met le controle en echec, et le motif la nomme")
    void date_de_naissance_absente() {
        Verdict v = ControleIdentiteAssure.verifier(bulletin(Map.of(
                CheminsExtraction.ASSURE_NOM, "MARTIN",
                CheminsExtraction.ASSURE_PRENOM, "Camille")));

        assertThat(v.etat()).isEqualTo(Verdict.Etat.EN_ECHEC);
        assertThat(v.motifs()).containsExactly(CheminsExtraction.ASSURE_DATE_NAISSANCE);
    }

    @Test
    @DisplayName("RG-2.1.1 — le controle ne s'applique pas a un RIB")
    void non_applicable_hors_bulletin() {
        DocumentEntrant rib = new DocumentEntrant(
                "doc-2", TypeDocument.RIB, "REC-1", 5,
                Instant.parse("2026-08-20T08:00:00Z"), false,
                Optional.empty(), Optional.empty());

        assertThat(ControleIdentiteAssure.verifier(rib).etat())
                .isEqualTo(Verdict.Etat.NON_APPLICABLE);
    }
}
