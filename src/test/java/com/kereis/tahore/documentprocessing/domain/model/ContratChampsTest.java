package com.kereis.tahore.documentprocessing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.kereis.tahore.documentprocessing.domain.model.ContratChamps.NatureRetour;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verrouille la transcription du contrat de champs.
 *
 * <p>Le classeur est la source de verite : ces tests echouent si la transcription s'en
 * ecarte. Certains sont des constats sur ce que le classeur laisse ouvert ou se contredit ;
 * ils passeront au vert le jour ou le metier tranchera, pas avant.
 */
class ContratChampsTest {

    @Test
    @DisplayName("Les cinq documents du perimetre sont transcrits avec leur nombre de champs")
    void perimetre_transcrit() {
        assertThat(ContratChamps.tous()).hasSize(79);
        assertThat(ContratChamps.du(TypeDocument.BULLETIN_ADHESION)).hasSize(52);
        assertThat(ContratChamps.du(TypeDocument.RECUEIL_CONSENTEMENT)).hasSize(7);
        assertThat(ContratChamps.du(TypeDocument.QUESTIONNAIRE_SANTE_SIMPLIFIE)).hasSize(8);
        assertThat(ContratChamps.du(TypeDocument.MANDAT_SEPA)).hasSize(7);
        assertThat(ContratChamps.du(TypeDocument.RIB)).hasSize(5);
    }

    @Test
    @DisplayName("Delos renvoie 49 valeurs brutes, 29 booleens de condition, et rien pour 1 champ")
    void nature_des_retours() {
        assertThat(ContratChamps.tous().stream().filter(c -> c.nature() == NatureRetour.VALEUR))
                .hasSize(49);
        assertThat(ContratChamps.tous().stream().filter(c -> c.nature() == NatureRetour.CONDITION))
                .hasSize(29);
        assertThat(ContratChamps.tous().stream()
                        .filter(c -> c.nature() == NatureRetour.NON_RENVOYE))
                .hasSize(1);
    }

    @Test
    @DisplayName("Un champ de condition attend un booleen, quel que soit son type declare")
    void type_effectif_des_conditions() {
        ContratChamps.Champ dateSignature = ContratChamps
                .champ(TypeDocument.MANDAT_SEPA, "dateSignature")
                .orElseThrow();

        assertThat(dateSignature.nature()).isEqualTo(NatureRetour.CONDITION);
        assertThat(dateSignature.typeDeclare()).isNotEqualTo("boolean");
        assertThat(dateSignature.typeEffectif()).isEqualTo("boolean");
    }

    @Test
    @DisplayName("Le RIB n'est fait que de valeurs brutes, le recueil que de conditions")
    void repartition_par_document() {
        assertThat(ContratChamps.conditions(TypeDocument.RIB)).isEmpty();
        assertThat(ContratChamps.valeurs(TypeDocument.RIB)).hasSize(5);
        assertThat(ContratChamps.valeurs(TypeDocument.RECUEIL_CONSENTEMENT)).isEmpty();
        assertThat(ContratChamps.conditions(TypeDocument.RECUEIL_CONSENTEMENT)).hasSize(7);
    }

    @Test
    @DisplayName("Aucun nom de champ n'est en collision a l'interieur d'un document")
    void noms_uniques_par_document() {
        for (TypeDocument document : TypeDocument.values()) {
            assertThat(ContratChamps.attendus(document))
                    .as("une collision ferait ecraser un champ par un autre dans metadata : %s",
                            document)
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
    @DisplayName("Constat : trois champs sont renommes faute d'unicite au classeur")
    void renommages_traces() {
        assertThat(ContratChamps.renommes())
                .extracting(ContratChamps.Champ::variable)
                .containsExactlyInAnyOrder(
                        "communeNaissance", "paysNaissance", "beneficiaireDateNaissance");
    }

    @Test
    @DisplayName("Constat : deux enumerations du classeur restent a obtenir")
    void enumerations_a_obtenir() {
        List<String> aObtenir = ContratChamps.enumerationsAObtenir().stream()
                .map(ContratChamps.Champ::enumeration)
                .distinct()
                .toList();

        // TYPE_PRODUIT porte le controle d'eligibilite au perimetre CU#3 : sans ses valeurs,
        // ce controle n'est pas ecrivable.
        assertThat(aObtenir).containsExactlyInAnyOrder("TYPE_PRODUIT", "CSP");
    }

    @Test
    @DisplayName("Constat : deux champs du QSS sont annonces booleens tout en portant une enumeration")
    void contradictions_du_classeur() {
        // Un booleen ne peut pas porter QSS_SIMPLIFIE ni CONSEILLER_BNP. Or RG-2.2.5 a besoin
        // de l'emetteur reel pour statuer. Voir docs/ecarts-specifications-api.md.
        assertThat(ContratChamps.contradictions())
                .extracting(ContratChamps.Champ::variable)
                .containsExactlyInAnyOrder("type", "emetteur");
    }
}
