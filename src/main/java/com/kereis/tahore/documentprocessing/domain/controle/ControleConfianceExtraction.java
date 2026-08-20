package com.kereis.tahore.documentprocessing.domain.controle;

import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.DonneesExtraites;
import java.util.List;
import java.util.Optional;

/**
 * RG-1.2.3 — exploitabilite des donnees extraites.
 *
 * <p>Delos fournit une confiance par champ. Un champ sous le seuil n'est pas une erreur :
 * c'est une valeur a faire verifier. Le controle remonte donc la <b>liste des champs
 * concernes</b> plutot qu'un verdict binaire sur le document, pour que le gestionnaire
 * sache quoi regarder.
 */
public final class ControleConfianceExtraction {

    public static final String REGLE = "RG-1.2.3";

    private ControleConfianceExtraction() {}

    public static Resultat verifier(DocumentEntrant document, double seuil) {
        Optional<DonneesExtraites> donnees = document.donneesExtraites();
        if (donnees.isEmpty()) {
            return new Resultat(REGLE, document.identifiant(), List.of(), false);
        }
        List<String> aVerifier = donnees.orElseThrow().champsSousSeuil(seuil);
        return new Resultat(REGLE, document.identifiant(), aVerifier, true);
    }

    /**
     * @param champsAVerifier noms des champs sous le seuil, du moins sur au plus sur
     * @param extractionPresente faux si le document n'a aucune donnee extraite
     */
    public record Resultat(
            String regle, String document, List<String> champsAVerifier, boolean extractionPresente) {

        public Resultat {
            champsAVerifier = List.copyOf(champsAVerifier);
        }

        public boolean exigeRevueManuelle() {
            return !extractionPresente || !champsAVerifier.isEmpty();
        }
    }
}
