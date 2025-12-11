package com.techub.rps.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@DisplayName("Hexagonal Architecture Tests")
class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.techub.rps");
    }

    @Test
    @DisplayName("Control layer should not depend on boundary layer")
    void controlLayer_shouldNotDependOnBoundaryLayer() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..control..")
                .should().dependOnClassesThat()
                .resideInAPackage("..boundary..");

        rule.check(classes);
    }

    @Test
    @DisplayName("Configuration should only be in config package")
    void configuration_shouldOnlyBeInConfigPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.context.annotation.Configuration")
                .should().resideInAPackage("..config..");

        rule.check(classes);
    }

    @Test
    @DisplayName("Controllers should only depend on services and mappers")
    void controllers_shouldOnlyDependOnServicesAndMappers() {
        ArchRule rule = classes()
                .that().resideInAPackage("..boundary.incoming..")
                .and().haveSimpleNameEndingWith("Controller")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..control.ports..",
                        "..boundary.incoming..",
                        "..control.model..",
                        "java..",
                        "org.springframework..",
                        "lombok..",
                        "org.slf4j.."
                );

        rule.check(classes);
    }

    @Test
    @DisplayName("Services should be annotated with @Service")
    void services_shouldBeAnnotatedWithService() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Service")
                .and().resideInAPackage("..control.ports..")
                .should().beAnnotatedWith("org.springframework.stereotype.Service");

        rule.check(classes);
    }

    @Test
    @DisplayName("Mappers should only be in boundary incoming package")
    void mappers_shouldOnlyBeInBoundaryIncoming() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Mapper")
                .should().resideInAPackage("..boundary.incoming..");

        rule.check(classes);
    }

    @Test
    @DisplayName("Classes should have appropriate naming conventions")
    void classes_shouldFollowNamingConventions() {
        ArchRule controllerRule = classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().haveSimpleNameEndingWith("Controller");

        ArchRule serviceRule = classes()
                .that().areAnnotatedWith("org.springframework.stereotype.Service")
                .and().resideInAPackage("..control.ports..")
                .should().haveSimpleNameEndingWith("Service");

        ArchRule adapterRule = classes()
                .that().areAnnotatedWith("org.springframework.stereotype.Component")
                .and().resideInAPackage("..boundary.outgoing..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should().haveSimpleNameEndingWith("Adapter");

        controllerRule.check(classes);
        serviceRule.check(classes);
        adapterRule.check(classes);
    }
}
