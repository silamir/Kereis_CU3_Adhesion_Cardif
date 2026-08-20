package com.kereis.tahore.documentprocessing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verrouille la transcription du contrat de champs.
 *
 * <p>Le classeur est la source de verite. Ces tests echouent si la transcription s'en
 * ecarte, et servent aussi de constat sur ce que le classeur laisse ouvert : ils
 * deviendront verts le jour ou le metier tranchera, pas avant.
 */
class ContratChampsTest {

    @Test
    @DisplayName("Les cinq documents du perimetre sont transcrits, avec leur nombre de champs")
    void perimetre_transcrit() {
        Map<TypeDocument, Long> parDocument = ContratChamps.tous().stream()
                .collect(Collectors.groupingBy(
                        ContratChamps.Champ::document, Collectors.counting()));

        assertThat(parDocument)
                .containsEntry(TypeDocument.BULLETIN_ADHESION, 52L)
                .containsEntry(TypeDocument.RECUEIL_CONSENTEMENT, 7L)
                .containsEntry(TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE, 8L)
                .containsEntry(TypeDocument.MANDAT_SEPA, 7L)
                .containsEntry(TypeDocument.RIB, 5L);
        assertThat(ContratChamps.tous()).hasSize(79);
    }

    @Test
    @DisplayName("Aucun nom de champ n'est en collision a l'interieur d'un document")
    void noms_uniques_par_document() {
        for (TypeDocument document : TypeDocument.values()) {
            List<String> noms = ContratChamps.du(document).stream()
                    .filter(ContratChamps.Champ::extractible)
                    .map(ContratChamps.Champ::variable)
                    .toList();

            assertThat(noms)
                    .as("noms de champ de %s : une collision ferait ecraser un champ par un "
                            + "autre dans metadata", document)
                    .doesNotHaveDuplicates();
        }
    }

    @Test
    @DisplayName("Les champs obligatoires se lisent dans le contrat, sans liste tenue a la main")
    void champs_obligatoires_lus_du_contrat() {
        assertThat(ContratChamps.obligatoires(TypeDocument.BULLETIN_ADHESION)).hasSize(34);
        assertThat(ContratChamps.obligatoires(TypeDocument.RECUEIL_CONSENTEMENT)).hasSize(7);
        assertThat(ContratChamps.obligatoires(TypeDocument.RIB)).hasSize(4);
    }

    @Test
    @DisplayName("Constat : deux enumerations du classeur restent a obtenir")
    void enumerations_a_obtenir() {
        List<String> aObtenir = ContratChamps.enumerationsAObtenir().stream()
                .map(ContratChamps.Champ::enumeration)
                .distinct()
                .toList();

        // TYPE_PRODUIT porte le controle d'eligibilite au perimetre CU#3 : tant que ses
        // valeurs manquent, ce controle n'est pas ecrivable.
        assertThat(aObtenir).containsExactlyInAnyOrder("TYPE_PRODUIT", "CSP");
    }

    @Test
    @DisplayName("Constat : le classeur classe la totalite du recueil et du QSS en controle")
    void repartition_extraction_controle() {
        Function<TypeDocument, Long> extraits =
                d -> ContratChamps.du(d).stream()
                        .filter(c -> c.role() == ContratChamps.Role.EXTRACTION)
                        .count();

        // Point ouvert : si Delos n'extrait rien de ces deux documents, la recevabilite
        // automatique n'est pas atteignable. Voir docs/ecarts-specifications-api.md.
        assertThat(extraits.apply(TypeDocument.RECUEIL_CONSENTEMENT)).isZero();
        assertThat(extraits.apply(TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE)).isZero();
        assertThat(extraits.apply(TypeDocument.BULLETIN_ADHESION)).isEqualTo(43L);
        assertThat(extraits.apply(TypeDocument.RIB)).isEqualTo(5L);
    }

    @Test
    @DisplayName("La cohérence RIB / mandat SEPA est marquee non extractible par le classeur")
    void champ_explicitement_non_extractible() {
        assertThat(ContratChamps.du(TypeDocument.MANDAT_SEPA))
                .anySatisfy(champ -> assertThat(champ.extractible()).isFalse());
        assertThat(ContratChamps.aExtraire(TypeDocument.MANDAT_SEPA))
                .doesNotContain("\u2014 ne pas extraire \u2014");
    }
}
