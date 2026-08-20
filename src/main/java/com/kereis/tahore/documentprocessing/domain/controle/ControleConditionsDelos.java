package com.kereis.tahore.documentprocessing.domain.controle;

import com.kereis.tahore.documentprocessing.domain.model.ContratChamps;
import com.kereis.tahore.documentprocessing.domain.model.DocumentEntrant;
import com.kereis.tahore.documentprocessing.domain.model.DonneesExtraites;
import java.util.List;
import java.util.Optional;

/**
 * Verifie les conditions que Delos evalue lui-meme.
 *
 * <p>Pour les champs classes « a verifier / controle » au contrat, Delos ne renvoie pas la
 * valeur mais un <b>booleen</b> : la condition decrite par le libelle du classeur est
 * remplie, ou non. Ce controle les parcourt tous, sans en connaitre le detail : la liste
 * vient du contrat, donc du classeur.
 *
 * <p>Trois issues sont distinguees, et elles n'appellent pas la meme suite. Une condition
 * <b>non remplie</b> est un fait metier — le document est en defaut. Une condition
 * <b>absente</b> est une lacune d'extraction. Une condition <b>peu sure</b> est une
 * reponse que Delos donne sans y croire : elle se fait verifier, elle ne se tranche pas.
 */
public final class ControleConditionsDelos {

    public static final String REGLE = "RG-2.1.7";

    private ControleConditionsDelos() {}

    public static Resultat verifier(DocumentEntrant document, double seuil) {
        List<String> attendues = ContratChamps.conditions(document.type());
        Optional<DonneesExtraites> donnees = document.donneesExtraites();
        if (donnees.isEmpty()) {
            return new Resultat(document.identifiant(), List.of(), attendues, List.of());
        }
        DonneesExtraites extraites = donnees.orElseThrow();

        List<String> nonRemplies = attendues.stream()
                .filter(nom -> extraites.champ(nom).filter(c -> !c.vrai()).isPresent())
                .toList();
        List<String> absentes = attendues.stream()
                .filter(nom -> extraites.champ(nom).isEmpty())
                .toList();
        List<String> peuSures = attendues.stream()
                .filter(nom -> extraites.champ(nom).filter(c -> !c.fiable(seuil)).isPresent())
                .toList();
        return new Resultat(document.identifiant(), nonRemplies, absentes, peuSures);
    }

    /**
     * @param conditionsNonRemplies Delos a repondu non : le document est en defaut
     * @param conditionsAbsentes Delos n'a rien renvoye pour cette condition
     * @param conditionsPeuSures Delos a repondu, mais sous le seuil de confiance
     */
    public record Resultat(
            String document,
            List<String> conditionsNonRemplies,
            List<String> conditionsAbsentes,
            List<String> conditionsPeuSures) {

        public Resultat {
            conditionsNonRemplies = List.copyOf(conditionsNonRemplies);
            conditionsAbsentes = List.copyOf(conditionsAbsentes);
            conditionsPeuSures = List.copyOf(conditionsPeuSures);
        }

        public boolean toutesRempliesEtSures() {
            return conditionsNonRemplies.isEmpty()
                    && conditionsAbsentes.isEmpty()
                    && conditionsPeuSures.isEmpty();
        }
    }
}
