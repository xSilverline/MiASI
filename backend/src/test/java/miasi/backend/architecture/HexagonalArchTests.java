package miasi.backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "miasi.backend", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchTests {

  @ArchTest
  static final ArchRule domain_should_not_depend_on_adapters =
      noClasses()
          .that()
          .resideInAnyPackage("..domains..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..api..", "..database..", "..eventListners..");

  @ArchTest
  static final ArchRule api_should_not_be_used_by_domain =
      noClasses()
          .that()
          .resideInAnyPackage("..api..")
          .and()
          .resideOutsideOfPackage("..api.config..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..database..");

  @ArchTest
  static final ArchRule database_should_not_depend_on_api =
      noClasses()
          .that()
          .resideInAnyPackage("..database..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..api..");

  @ArchTest
  static final ArchRule database_should_not_be_imported_by_domain =
      noClasses()
          .that()
          .resideInAnyPackage("..domains..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..database..");

  @ArchTest
  static final ArchRule listeners_should_not_be_called_from_domain =
      noClasses()
          .that()
          .resideInAnyPackage("..domains..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..eventListners..");

  @ArchTest
  static final ArchRule domain_should_not_use_spring =
      noClasses()
          .that()
          .resideInAnyPackage("..domains..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..");

  @ArchTest
  static final ArchRule ports_should_be_interfaces =
      classes().that().resideInAnyPackage("..ports..", "..port.out..").should().beInterfaces();

  @ArchTest
  static final ArchRule output_port_implementations_are_in_infrastructure =
      classes()
          .that()
          .implement(
              com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage(
                  "..port.out.."))
          .should()
          .resideInAnyPackage("..infrastructure.out..", "..database..", "..eventListners..");

  @ArchTest
  static final ArchRule analysis_domain_should_not_depend_on_application_or_infrastructure =
      noClasses()
          .that()
          .resideInAnyPackage("..domains.analysis.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..domains.analysis.application..", "..domains.analysis.infrastructure..");

  @ArchTest
  static final ArchRule analysis_application_should_not_depend_on_infrastructure =
      noClasses()
          .that()
          .resideInAnyPackage("..domains.analysis.application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..domains.analysis.infrastructure..", "..api..", "..database..");
}
