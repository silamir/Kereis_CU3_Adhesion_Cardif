package com.kereis.tahore.documentprocessing.infrastructure.dms;

import com.kereis.tahore.documentprocessing.domain.model.Lot;
import com.kereis.tahore.documentprocessing.domain.port.EtatDocumentPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Renvoi d'un lot au gestionnaire par changement d'etat de traitement.
 *
 * <p>Squelette : l'endpoint de mise a jour d'etat reste a confirmer, ainsi que
 * la valeur d'etat a poser. {@code PENDING_PROCESSING} existe bien dans
 * l'enumeration DMS ; {@code PENDING_AUTOMATION} des specifications, non.
 */
@Component
public class DmsEtatDocumentAdapter implements EtatDocumentPort {

    private static final Logger log = LoggerFactory.getLogger(DmsEtatDocumentAdapter.class);

    @Override
    public void renvoyerAuGestionnaire(Lot lot, String motif) {
        // Aucune donnee de sante ni donnee personnelle en journal : seuls
        // l'identifiant technique du lot et le motif.
        log.info("Renvoi du lot {} au gestionnaire, motif {}", lot.identifiant(), motif);
    }
}
