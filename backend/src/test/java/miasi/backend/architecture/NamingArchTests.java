package miasi.backend.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "miasi.backend", importOptions = ImportOption.DoNotIncludeTests.class)
public class NamingArchTests {

  @ArchTest()
  static final ArchRule ControllerNaming = classes()
      .that()
      .areAnnotatedWith(RestController.class)
      .or()
      .areAnnotatedWith(Controller.class)
      .should().haveSimpleNameContaining("Controller");

  @ArchTest()
  static final ArchRule RepositoryNaming = classes()
      .that()
      .areAnnotatedWith(Repository.class)
      .should().haveSimpleNameContaining("Repository");

  @ArchTest()
  static final ArchRule ServiceNaming = classes()
      .that()
      .areAnnotatedWith(Service.class)
      .should().haveSimpleNameContaining("Service");

  @ArchTest()
  static final ArchRule InterfaceNaming = classes()
      .that().areInterfaces()
      .should().haveSimpleNameStartingWith("I");

  @ArchTest
  static final ArchRule EnumNaming = classes()
      .that()
      .areEnums()
      .should()
      .haveSimpleNameNotContaining("Enum");
}
