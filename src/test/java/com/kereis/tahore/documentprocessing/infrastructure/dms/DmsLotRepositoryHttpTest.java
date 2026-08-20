package com.kereis.tahore.documentprocessing.infrastructure.dms;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.Lot;
import com.kereis.tahore.documentprocessing.domain.model.TypeDocument;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

/**
 * Appel DMS simule de bout en bout : le DMS est remplace par WireMock, qui renvoie une
 * fixture de reponse 200 portant les extractions Delos dans {@code metadata}.
 *
 * <p>Ce test couvre ce qu'un test lisant la fixture depuis le classpath ne peut pas
 * couvrir : la requete reellement emise. Trois pieges du contrat DMS ne se voient que
 * la — l'extension obligatoire, la limite explicite, et les filtres serveur.
 *
 * <p>La fixture servie est celle des tests unitaires et de la stack locale : un seul
 * jeu de donnees pour les trois usages.
 */
class DmsLotRepositoryHttpTest {

    private static WireMockServer dms;
    private DmsLotRepository depot;

    @BeforeAll
    static void demarrerDms() {
        dms = new WireMockServer(options().dynamicPort());
        dms.start();
    }

    @AfterAll
    static void arreterDms() {
        dms.stop();
    }

    @BeforeEach
    void reinitialiser() {
        dms.resetAll();
        DmsProprietes proprietes =
                new DmsProprietes("http://localhost:" + dms.port(), List.of("READY"), true, 200);
        depot = new DmsLotRepository(RestClient.builder(), proprietes);
    }

    private static String fixture(String scenario) throws Exception {
        try (InputStream flux = DmsLotRepositoryHttpTest.class.getResourceAsStream(
                "/fixtures/dms/" + scenario + ".json")) {
            assertThat(flux).as("fixture %s presente", scenario).isNotNull();
            return new String(flux.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void dmsRepond(String scenario) throws Exception {
        dms.stubFor(get(urlPathEqualTo("/private/secure/input/documents"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(fixture(scenario))));
    }

    @Test
    @DisplayName("Un lot nominal remonte en un lot de cinq documents, types lus dans metadata.delos")
    void lot_nominal_complet() throws Exception {
        dmsRepond("01-lot-nominal");

        List<Lot> lots = depot.lotsEnAttente();

        assertThat(lots).hasSize(1);
        Lot lot = lots.getFirst();
        assertThat(lot.identifiant()).isEqualTo("REC-2026-0001");
        assertThat(lot.documents()).hasSize(5);
        assertThat(lot.typesPresents())
                .containsExactlyInAnyOrder(
                        TypeDocument.BULLETIN_ADHESION,
                        TypeDocument.RECUEIL_CONSENTEMENT,
                        TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE,
                        TypeDocument.MANDAT_SEPA,
                        TypeDocument.RIB);
    }

    @Test
    @DisplayName("La requete porte extension=reception, sans quoi l'identifiant de lot serait absent")
    void requete_demande_la_sous_ressource_reception() throws Exception {
        dmsRepond("01-lot-nominal");

        depot.lotsEnAttente();

        dms.verify(getRequestedFor(urlPathEqualTo("/private/secure/input/documents"))
                .withQueryParam("extension", equalTo("reception")));
    }

    @Test
    @DisplayName("La requete porte une limite explicite et les filtres serveur du perimetre")
    void requete_porte_limite_et_filtres() throws Exception {
        dmsRepond("01-lot-nominal");

        depot.lotsEnAttente();

        dms.verify(getRequestedFor(urlPathEqualTo("/private/secure/input/documents"))
                .withQueryParam("limit", equalTo("200"))
                .withQueryParam("processingStateIdList", equalTo("READY"))
                .withQueryParam("withoutReference", equalTo("true")));
    }

    @Test
    @DisplayName("Sans filtre de rattachement, withoutReference n'est pas envoye")
    void filtre_de_rattachement_desactivable() throws Exception {
        dmsRepond("01-lot-nominal");
        depot = new DmsLotRepository(
                RestClient.builder(),
                new DmsProprietes("http://localhost:" + dms.port(), List.of("READY"), false, 50));

        depot.lotsEnAttente();

        dms.verify(getRequestedFor(urlPathEqualTo("/private/secure/input/documents"))
                .withQueryParam("withoutReference", absent())
                .withQueryParam("limit", equalTo("50")));
    }

    @Test
    @DisplayName("Les donnees extraites sont lues sous metadata.delos, avec leur confiance")
    void extraction_lue_depuis_metadata() throws Exception {
        dmsRepond("03-lot-confiance-faible");

        DocumentEntrant bulletin = depot.lotsEnAttente().getFirst().documents().stream()
                .filter(d -> d.type() == TypeDocument.BULLETIN_ADHESION)
                .findFirst()
                .orElseThrow();

        assertThat(bulletin.donneesExtraites()).isPresent();
        assertThat(bulletin.donneesExtraites().orElseThrow().confianceGlobale()).isEqualTo(0.41d);
        assertThat(bulletin.donneesExtraites().orElseThrow().champ("assure.nom")).isPresent();
    }

    @Test
    @DisplayName("Le questionnaire de sante remonte son marqueur de confidentialite medicale")
    void confidentialite_medicale_portee_par_le_document() throws Exception {
        dmsRepond("01-lot-nominal");

        DocumentEntrant qss = depot.lotsEnAttente().getFirst().documents().stream()
                .filter(d -> d.type() == TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE)
                .findFirst()
                .orElseThrow();

        assertThat(qss.confidentialiteMedicale()).isTrue();
    }

    @Test
    @DisplayName("Deux bulletins dans un meme lot sont detectes comme multi-adhesion")
    void multi_adhesion_detectee() throws Exception {
        dmsRepond("04-lot-multi-adhesion");

        Lot lot = depot.lotsEnAttente().getFirst();

        assertThat(lot.documents()).hasSize(6);
        assertThat(lot.multiAdhesion()).isTrue();
    }

    @Test
    @DisplayName("Un lot deja rattache a un dossier est reconnu comme hors perimetre")
    void lot_deja_rattache_reconnu() throws Exception {
        dmsRepond("05-lot-deja-rattache");

        assertThat(depot.lotsEnAttente().getFirst().documents())
                .isNotEmpty()
                .allMatch(DocumentEntrant::dejaRattache);
    }

    @Test
    @DisplayName("Une reponse vide ne produit aucun lot, sans lever d'exception")
    void reponse_vide() {
        dms.stubFor(get(urlPathEqualTo("/private/secure/input/documents"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"count\":0,\"totalCount\":0,\"data\":[]}")));

        assertThat(depot.lotsEnAttente()).isEmpty();
    }
}
