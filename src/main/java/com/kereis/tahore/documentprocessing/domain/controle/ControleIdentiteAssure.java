package com.kereis.tahore.documentprocessing.domain.controle;

import static com.kereis.tahore.documentprocessing.domain.model.CheminsExtraction.DATE_NAISSANCE;
import static com.kereis.tahore.documentprocessing.domain.model.CheminsExtraction.NOM;
import static com.kereis.tahore.documentprocessing.domain.model.CheminsExtraction.PRENOM;

import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.DonneesExtraites;
import com.kereis.tahore.documentprocessing.domain.model.TypeDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RG-2.1.1 — identite de l'assure sur le bulletin d'adhesion.
 *
 * <p>Gabarit des 80 assertions des 14 fiches. Un champ compte comme presente s'il existe
 * <b>et</b> si sa confiance atteint le seuil : une valeur lue a 0,42 n'est pas une valeur
 * sur laquelle on engage un dossier.
 */
public final class ControleIdentiteAssure {

    public static final String REGLE = "RG-2.1.1";
    private static final List<String> REQUIS = List.of(NOM, PRENOM, DATE_NAISSANCE);

    private ControleIdentiteAssure() {}

    public static Verdict verifier(DocumentEntrant document, double seuil) {
        if (document.type() != TypeDocument.BULLETIN_ADHESION) {
            return Verdict.nonApplicable(REGLE);
        }
        Optional<DonneesExtraites> donnees = document.donneesExtraites();
        if (donnees.isEmpty()) {
            return Verdict.enEchec(REGLE, List.of("aucune donnee extraite"));
        }
        List<String> motifs = new ArrayList<>();
        for (String champ : REQUIS) {
            if (donnees.orElseThrow().champ(champ).isEmpty()) {
                motifs.add(champ + " : absent");
            } else if (!donnees.orElseThrow().exploitable(champ, seuil)) {
                motifs.add(champ + " : confiance insuffisante");
            }
        }
        return motifs.isEmpty() ? Verdict.conforme(REGLE) : Verdict.enEchec(REGLE, motifs);
    }

    public record Verdict(String regle, Etat etat, List<String> motifs) {

        public enum Etat {
            CONFORME,
            EN_ECHEC,
            NON_APPLICABLE
        }

        static Verdict conforme(String regle) {
            return new Verdict(regle, Etat.CONFORME, List.of());
        }

        static Verdict enEchec(String regle, List<String> motifs) {
            return new Verdict(regle, Etat.EN_ECHEC, List.copyOf(motifs));
        }

        static Verdict nonApplicable(String regle) {
            return new Verdict(regle, Etat.NON_APPLICABLE, List.of());
        }
    }
}
