package miasi.backend.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


@AnalyzeClasses(packages = "miasi.backend", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchTests {

  @ArchTest
  static final ArchRule domain_should_not_depend_on_adapters =
      noClasses()
          .that()
          .resideInAnyPackage(
              "..domains.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..api..",
              "..database..",
              "..eventListners.."
          );

  @ArchTest
  static final ArchRule api_should_not_be_used_by_domain =
      noClasses()
          .that()
          .resideInAnyPackage(
              "..api.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..database.."
          );
  @ArchTest
  static final ArchRule database_should_not_depend_on_api =
      noClasses()
          .that()
          .resideInAnyPackage(
              "..database.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..api.."
          );

  @ArchTest
  static final ArchRule database_should_not_be_imported_by_domain =
      noClasses()
          .that()
          .resideInAnyPackage(
              "..domains.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..database.."
          );

  @ArchTest
  static final ArchRule listeners_should_not_be_called_from_domain =
      noClasses()
          .that()
          .resideInAnyPackage(
              "..domains.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..eventListners.."
          );
  @ArchTest
  static final ArchRule domain_should_not_use_spring =
      noClasses()
          .that()
          .resideInAnyPackage("..domains..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework.."
          );
  @ArchTest
  static final ArchRule ports_should_be_interfaces =
      classes()
          .that()
          .resideInAnyPackage("..ports..")
          .should()
          .beInterfaces();
  @ArchTest
  static final ArchRule port_implementations_are_outside_domain =
      classes()
          .that()
          .implement(
              JavaClass.Predicates.resideInAnyPackage("..domains..")
          )
          .should()
          .resideOutsideOfPackage("..domains..");

  @ArchTest
  static final ArchRule hexagonal =
      com.tngtech.archunit.library.Architectures
          .onionArchitecture()
          .domainModels("..domains..")
          .adapter("in", "..api..")
          .adapter("out", "..database..");
}