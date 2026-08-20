package com.kereis.tahore.documentprocessing.application;

import com.kereis.tahore.documentprocessing.domain.model.Lot;
import com.kereis.tahore.documentprocessing.domain.port.EtatDocumentPort;
import com.kereis.tahore.documentprocessing.domain.port.LotRepository;
import java.util.List;

/**
 * Point d'entree du module : scan periodique des lots en attente.
 *
 * <p>Squelette. Les 80 assertions de controle des 14 fiches viendront se
 * brancher ici, un test rouge a la fois.
 */
public class ScannerLotsEnAttente {

    private final LotRepository lots;
    private final EtatDocumentPort etats;

    public ScannerLotsEnAttente(LotRepository lots, EtatDocumentPort etats) {
        this.lots = lots;
        this.etats = etats;
    }

    public Resultat executer() {
        List<Lot> aTraiter = lots.lotsEnAttente();
        int renvoyes = 0;
        for (Lot lot : aTraiter) {
            if (lot.multiAdhesion()) {
                etats.renvoyerAuGestionnaire(lot, "MULTI_ADHESION");
                renvoyes++;
            }
        }
        return new Resultat(aTraiter.size(), renvoyes);
    }

    public record Resultat(int lotsExamines, int lotsRenvoyes) {}
}
