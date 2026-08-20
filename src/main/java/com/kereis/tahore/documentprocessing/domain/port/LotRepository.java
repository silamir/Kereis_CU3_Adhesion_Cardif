package com.kereis.tahore.documentprocessing.domain.port;

import com.kereis.tahore.documentprocessing.domain.model.Lot;
import java.util.List;

/**
 * Acces aux lots en attente de traitement.
 *
 * <p>Nomme par le besoin du domaine, pas par le fournisseur : un
 * {@code DmsPort} ferait traverser la forme du tiers et annulerait le benefice
 * de la frontiere. Regle verifiee par {@code ArchitectureHexagonaleTest}.
 */
public interface LotRepository {

    List<Lot> lotsEnAttente();
}
