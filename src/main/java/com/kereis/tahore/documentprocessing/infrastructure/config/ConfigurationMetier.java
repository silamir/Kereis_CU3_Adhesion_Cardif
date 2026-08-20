package com.kereis.tahore.documentprocessing.infrastructure.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parametres metier, modifiables sans recompiler.
 *
 * @param etatsEcoutes etats DMS declenchant le traitement. Les specifications
 *     CU#3 citent « pending_automation », absent de l'enumeration reelle du
 *     DMS : valeur a trancher.
 */
@ConfigurationProperties(prefix = "cu3.scan")
public record ConfigurationMetier(
        List<String> etatsEcoutes, String etatRenvoiGestionnaire, int slaHeures) {}
