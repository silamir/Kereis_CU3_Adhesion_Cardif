package com.kereis.tahore.documentprocessing.domain.model;

import java.util.Map;
import java.util.Optional;

/**
 * Donnees issues de l'extraction Delos, portees par le DMS dans
 * {@code metadata}.
 *
 * <p>La convention de nommage des cles de {@code metadata} reste a obtenir
 * cote Kereis : elle est donc isolee dans l'adaptateur DMS et n'apparait pas
 * dans cette signature.
 */
public record DonneesExtraites(
        double confianceGlobale, boolean manuscrit, Map<String, Object> champs) {

    public Optional<Object> champ(String chemin) {
        return Optional.ofNullable(champs.get(chemin));
    }
}
