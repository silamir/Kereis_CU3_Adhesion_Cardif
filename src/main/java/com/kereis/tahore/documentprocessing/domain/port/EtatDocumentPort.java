package com.kereis.tahore.documentprocessing.domain.port;

import com.kereis.tahore.documentprocessing.domain.model.Lot;

/** Changement d'etat de traitement d'un lot ou d'un document. */
public interface EtatDocumentPort {

    /** Renvoie le lot au gestionnaire, sans autre effet de bord. */
    void renvoyerAuGestionnaire(Lot lot, String motif);
}
