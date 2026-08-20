package com.kereis.tahore.documentprocessing.domain.model;

import java.time.Instant;
import java.util.Optional;

/**
 * Un document entrant tel que le domaine en a besoin.
 *
 * <p>Volontairement plus pauvre que la reponse du DMS : le domaine ne connait
 * que ce dont les controles ont besoin. La traduction est faite par
 * l'adaptateur, jamais ici.
 */
public record DocumentEntrant(
        String identifiant,
        TypeDocument type,
        String identifiantLot,
        int rangDansLot,
        Instant recuLe,
        boolean confidentialiteMedicale,
        Optional<String> numeroDossier,
        Optional<DonneesExtraites> donneesExtraites) {

    /** Un document deja rattache a un dossier sort du perimetre d'ecoute. */
    public boolean dejaRattache() {
        return numeroDossier.filter(n -> !n.isBlank()).isPresent();
    }
}
