package com.kereis.tahore.documentprocessing.infrastructure.dms;

import static org.assertj.core.api.Assertions.assertThat;

import com.kereis.tahore.documentprocessing.domain.model.TypeDocument;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.json.JsonParserFactory;

/**
 * Lecture des fixtures de simulation : verifie que la traduction
 * DMS -> domaine tient sur chaque scenario.
 */
class DmsLotRepositoryTest {

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> documents(String scenario) throws Exception {
        try (InputStream flux =
                DmsLotRepositoryTest.class.getResourceAsStream("/fixtures/dms/" + scenario + ".json")) {
            assertThat(flux).as("fixture %s presente", scenario).isNotNull();
            Map<String, Object> reponse =
                    JsonParserFactory.getJsonParser().parseMap(new String(flux.readAllBytes()));
            return (List<Map<String, Object>>) reponse.get("data");
        }
    }

    @Test
    @DisplayName("Un lot nominal expose les cinq pieces du perimetre CU#3")
    void lot_nominal() throws Exception {
        List<TypeDocument> types = documents("01-lot-nominal").stream()
                .map(DmsLotRepository::versDomaine)
                .filter(java.util.Objects::nonNull)
                .map(d -> d.type())
                .toList();

        assertThat(types)
                .containsExactlyInAnyOrder(
                        TypeDocument.BULLETIN_ADHESION,
                        TypeDocument.RECUEIL_CONSENTEMENT,
                        TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE,
                        TypeDocument.MANDAT_SEPA,
                        TypeDocument.RIB);
    }

    @Test
    @DisplayName("Le questionnaire de sante porte le marqueur de confidentialite medicale")
    void confidentialite_medicale() throws Exception {
        var qss = documents("01-lot-nominal").stream()
                .map(DmsLotRepository::versDomaine)
                .filter(java.util.Objects::nonNull)
                .filter(d -> d.type() == TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE)
                .findFirst()
                .orElseThrow();

        assertThat(qss.confidentialiteMedicale()).isTrue();
    }

    @Test
    @DisplayName("Un lot deja rattache a un dossier sort du perimetre d'ecoute")
    void lot_deja_rattache() throws Exception {
        var documents = documents("05-lot-deja-rattache").stream()
                .map(DmsLotRepository::versDomaine)
                .filter(java.util.Objects::nonNull)
                .toList();

        assertThat(documents).isNotEmpty().allMatch(d -> d.dejaRattache());
    }

    @Test
    @DisplayName("Une confiance globale sous le seuil est lisible sur le bulletin")
    void confiance_faible() throws Exception {
        var bulletin = documents("03-lot-confiance-faible").stream()
                .map(DmsLotRepository::versDomaine)
                .filter(java.util.Objects::nonNull)
                .filter(d -> d.type() == TypeDocument.BULLETIN_ADHESION)
                .findFirst()
                .orElseThrow();

        assertThat(bulletin.donneesExtraites()).isPresent();
        assertThat(bulletin.donneesExtraites().orElseThrow().confianceGlobale()).isLessThan(0.80d);
    }
}
