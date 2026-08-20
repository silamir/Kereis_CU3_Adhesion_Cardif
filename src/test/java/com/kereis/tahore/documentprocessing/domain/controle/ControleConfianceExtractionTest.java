package com.kereis.tahore.documentprocessing.domain.controle;

import static org.assertj.core.api.Assertions.assertThat;

import com.kereis.tahore.documentprocessing.domain.model.ChampExtrait;
import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.DonneesExtraites;
import com.kereis.tahore.documentprocessing.domain.model.TypeDocument;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ControleConfianceExtractionTest {

    private static final double SEUIL = 0.90d;

    private static DocumentEntrant avec(Map<String, ChampExtrait> champs) {
        return new DocumentEntrant(
                "doc-1", TypeDocument.BULLETIN_ADHESION, "REC-1", 1,
                Instant.parse("2026-08-20T08:00:00Z"), false, Optional.empty(),
                Optional.of(new DonneesExtraites(champs)));
    }

    @Test
    @DisplayName("RG-1.2.3 — tous les champs au-dessus du seuil : aucune revue manuelle")
    void tous_fiables() {
        var r = ControleConfianceExtraction.verifier(avec(Map.of(
                "nom", new ChampExtrait("Dupont", 0.97d),
                "dateNaissance", new ChampExtrait("1985-03-12", 0.93d))), SEUIL);

        assertThat(r.champsAVerifier()).isEmpty();
        assertThat(r.exigeRevueManuelle()).isFalse();
    }

    @Test
    @DisplayName("RG-1.2.3 — les champs sous le seuil sont nommes, du moins sur au plus sur")
    void champs_sous_seuil_ordonnes() {
        var r = ControleConfianceExtraction.verifier(avec(Map.of(
                "nom", new ChampExtrait("Dupont", 0.97d),
                "email", new ChampExtrait("x@y.invalid", 0.73d),
                "telephone", new ChampExtrait("0388000000", 0.70d),
                "adresse", new ChampExtrait("12 rue des Lilas", 0.77d))), SEUIL);

        assertThat(r.champsAVerifier()).containsExactly("telephone", "email", "adresse");
        assertThat(r.exigeRevueManuelle()).isTrue();
    }

    @Test
    @DisplayName("RG-1.2.3 — un document sans extraction exige une revue manuelle")
    void extraction_absente() {
        DocumentEntrant sans = new DocumentEntrant(
                "doc-2", TypeDocument.RIB, "REC-1", 5,
                Instant.parse("2026-08-20T08:00:00Z"), false, Optional.empty(), Optional.empty());

        var r = ControleConfianceExtraction.verifier(sans, SEUIL);

        assertThat(r.extractionPresente()).isFalse();
        assertThat(r.exigeRevueManuelle()).isTrue();
    }

    @Test
    @DisplayName("Le seuil est un parametre : abaisse a 0,70, plus aucun champ n'est a verifier")
    void seuil_parametrable() {
        var champs = Map.of(
                "email", new ChampExtrait("x@y.invalid", 0.73d),
                "telephone", new ChampExtrait("0388000000", 0.70d));

        assertThat(ControleConfianceExtraction.verifier(avec(champs), 0.90d).champsAVerifier())
                .hasSize(2);
        assertThat(ControleConfianceExtraction.verifier(avec(champs), 0.70d).champsAVerifier())
                .isEmpty();
    }
}
