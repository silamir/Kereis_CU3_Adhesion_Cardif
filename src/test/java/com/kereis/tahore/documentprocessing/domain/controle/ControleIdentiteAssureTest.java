package com.kereis.tahore.documentprocessing.domain.controle;

import static org.assertj.core.api.Assertions.assertThat;

import com.kereis.tahore.documentprocessing.domain.controle.ControleIdentiteAssure.Verdict;
import com.kereis.tahore.documentprocessing.domain.model.ChampExtrait;
import com.kereis.tahore.documentprocessing.domain.model.CheminsExtraction;
import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.DonneesExtraites;
import com.kereis.tahore.documentprocessing.domain.model.TypeDocument;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Un controle metier se teste sans contexte Spring, sans reseau, sans forme du DMS. */
class ControleIdentiteAssureTest {

    private static final double SEUIL = 0.90d;

    private static DocumentEntrant bulletin(Map<String, ChampExtrait> champs) {
        return new DocumentEntrant(
                "doc-1", TypeDocument.BULLETIN_ADHESION, "REC-1", 1,
                Instant.parse("2026-08-20T08:00:00Z"), false, Optional.empty(),
                Optional.of(new DonneesExtraites(champs)));
    }

    @Test
    @DisplayName("RG-2.1.1 — nom, prenom et date de naissance surs : conforme")
    void identite_complete_et_sure() {
        Verdict v = ControleIdentiteAssure.verifier(bulletin(Map.of(
                CheminsExtraction.NOM, new ChampExtrait("Dupont", 0.97d),
                CheminsExtraction.PRENOM, new ChampExtrait("Camille", 0.94d),
                CheminsExtraction.DATE_NAISSANCE, new ChampExtrait("1985-03-12", 0.93d))), SEUIL);

        assertThat(v.etat()).isEqualTo(Verdict.Etat.CONFORME);
    }

    @Test
    @DisplayName("RG-2.1.1 — un champ absent est distingue d'un champ peu sur")
    void absent_et_peu_sur_sont_distingues() {
        Verdict v = ControleIdentiteAssure.verifier(bulletin(Map.of(
                CheminsExtraction.NOM, new ChampExtrait("Dupont", 0.97d),
                CheminsExtraction.PRENOM, new ChampExtrait("Camille", 0.42d))), SEUIL);

        assertThat(v.etat()).isEqualTo(Verdict.Etat.EN_ECHEC);
        assertThat(v.motifs())
                .containsExactly(
                        CheminsExtraction.PRENOM + " : confiance insuffisante",
                        CheminsExtraction.DATE_NAISSANCE + " : absent");
    }

    @Test
    @DisplayName("RG-2.1.1 — une valeur presente mais lue a 0,42 ne suffit pas")
    void valeur_presente_mais_peu_sure() {
        Verdict v = ControleIdentiteAssure.verifier(bulletin(Map.of(
                CheminsExtraction.NOM, new ChampExtrait("Dupont", 0.42d),
                CheminsExtraction.PRENOM, new ChampExtrait("Camille", 0.94d),
                CheminsExtraction.DATE_NAISSANCE, new ChampExtrait("1985-03-12", 0.93d))), SEUIL);

        assertThat(v.etat()).isEqualTo(Verdict.Etat.EN_ECHEC);
        assertThat(v.motifs()).containsExactly(CheminsExtraction.NOM + " : confiance insuffisante");
    }

    @Test
    @DisplayName("RG-2.1.1 — le controle ne s'applique pas a un RIB")
    void non_applicable_hors_bulletin() {
        DocumentEntrant rib = new DocumentEntrant(
                "doc-2", TypeDocument.RIB, "REC-1", 5,
                Instant.parse("2026-08-20T08:00:00Z"), false, Optional.empty(), Optional.empty());

        assertThat(ControleIdentiteAssure.verifier(rib, SEUIL).etat())
                .isEqualTo(Verdict.Etat.NON_APPLICABLE);
    }
}
