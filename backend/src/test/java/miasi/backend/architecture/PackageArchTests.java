package miasi.backend.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "miasi.backend", importOptions = ImportOption.DoNotIncludeTests.class)
public class PackageArchTests {

  @ArchTest
  static final ArchRule events_should_be_in_events_package =
      classes()
          .that()
          .areAssignableTo(java.util.EventObject.class)
          .should()
          .resideInAnyPackage("..events..");

  @ArchTest
  static final ArchRule repositories_should_be_only_in_ports_or_database =
      classes()
          .that().areNotInterfaces()
          .and().haveSimpleNameContaining("Repository")
          .should()
          .resideInAnyPackage(
              "..database..",
              "..ports.."
          );

  @ArchTest
  static final ArchRule controllers_should_be_in_api =
      classes()
          .that()
          .haveSimpleNameContaining("Controller")
          .should()
          .resideInAnyPackage("..api..", "..infrastructure.in.web..");

  @ArchTest
  static final ArchRule services_should_not_be_in_domain =
      noClasses()
          .that()
          .haveSimpleNameEndingWith("Service")
          .should()
          .resideInAnyPackage("..domains..domain..");

  @ArchTest
  static final ArchRule dto_should_be_in_api =
      classes()
          .that()
          .haveSimpleNameContaining("Request")
          .or()
          .haveSimpleNameContaining("Response")
          .should()
          .resideInAnyPackage("..api.jsons..", "..infrastructure.in.web.dto..");
}

