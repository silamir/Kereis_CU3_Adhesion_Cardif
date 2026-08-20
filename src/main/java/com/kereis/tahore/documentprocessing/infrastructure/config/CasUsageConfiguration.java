package com.kereis.tahore.documentprocessing.infrastructure.config;

import com.kereis.tahore.documentprocessing.application.ScannerLotsEnAttente;
import com.kereis.tahore.documentprocessing.domain.port.EtatDocumentPort;
import com.kereis.tahore.documentprocessing.domain.port.LotRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Cablage des cas d'usage : le domaine reste ignorant de Spring. */
@Configuration
@EnableConfigurationProperties(ConfigurationMetier.class)
public class CasUsageConfiguration {

    @Bean
    ScannerLotsEnAttente scannerLotsEnAttente(LotRepository lots, EtatDocumentPort etats) {
        return new ScannerLotsEnAttente(lots, etats);
    }
}
