package com.kereis.tahore.documentprocessing;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Traduit en tests les principes non negociables de la constitution.
 *
 * <p>Un echec ici n'est pas un detail de style : c'est la frontiere hexagonale
 * qui cede. Le corriger tot coute une minute, tard coute un refactoring.
 */
@AnalyzeClasses(
        packages = "com.kereis.tahore.documentprocessing",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureHexagonaleTest {

    @ArchTest
    static final ArchRule le_domaine_ignore_spring = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..", "tools.jackson..", "com.fasterxml..")
            .because("le domaine doit etre testable sans demarrer de contexte applicatif");

    @ArchTest
    static final ArchRule le_domaine_ignore_l_infrastructure = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .because("la dependance va de l'infrastructure vers le domaine, jamais l'inverse");

    @ArchTest
    static final ArchRule l_application_ignore_l_infrastructure = noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .because("les cas d'usage s'expriment sur les ports, pas sur les adaptateurs");

    @ArchTest
    static final ArchRule les_ports_sont_des_interfaces = classes()
            .that()
            .resideInAPackage("..domain.port..")
            .should()
            .beInterfaces()
            .because("un port est un contrat, pas une implementation");

    /**
     * Anti-pattern explicite de la constitution : un port nomme d'apres un
     * systeme fait traverser la forme du tiers et annule le benefice de la
     * frontiere. Les ports se nomment par le besoin du domaine.
     */
    @ArchTest
    static final ArchRule aucun_port_nomme_d_apres_un_fournisseur = noClasses()
            .that()
            .resideInAPackage("..domain.port..")
            .should()
            .haveSimpleNameContaining("Dms")
            .orShould()
            .haveSimpleNameContaining("Tahore")
            .orShould()
            .haveSimpleNameContaining("Delos")
            .because("un port se nomme par la capacite metier, pas par le fournisseur");
}
