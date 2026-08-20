package com.kereis.tahore.documentprocessing.infrastructure.dms;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parametres d'appel du DMS.
 *
 * @param etatsEcoutes valeurs de {@code processingStateIdList}. Les specifications CU#3 citent
 *     « pending_automation », absent de l'enumeration reelle du DMS : valeur a trancher.
 * @param seulementNonRattaches ajoute {@code withoutReference} a la requete, second critere du
 *     verrou anti-doublon, applique par le serveur.
 * @param limite valeur de {@code limit}. Le DMS applique 20 par defaut, ce qui tronquerait un scan
 *     large sans le signaler.
 */
@ConfigurationProperties(prefix = "cu3.dms")
public record DmsProprietes(
        String baseUrl, List<String> etatsEcoutes, boolean seulementNonRattaches, int limite) {}
