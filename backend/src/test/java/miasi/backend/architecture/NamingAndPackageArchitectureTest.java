package miasi.backend.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

class NamingAndPackageArchitectureTest {

  private static final String BASE_PACKAGE = "miasi.backend";
  private static final String API_PACKAGE = BASE_PACKAGE + ".api";
  private static final String WEB_DTO_PACKAGE = BASE_PACKAGE + ".adapter.in.web.dto";

  @Test
  void controllersShouldBeNamedAndLocatedInApiPackage() {
    Set<String> actualViolations =
        productionClasses().stream()
            .filter(NamingAndPackageArchitectureTest::isController)
            .filter(
                javaClass ->
                    !javaClass.getSimpleName().endsWith("Controller")
                        || !isInPackage(javaClass, API_PACKAGE))
            .map(JavaClass::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        Set.of(),
        actualViolations,
        "REST/MVC controllers should be named *Controller and live in miasi.backend.api.");
  }

  @Test
  void restDtosShouldLiveInWebDtoPackage() {
    Set<String> actualViolations =
        productionClasses().stream()
            .filter(NamingAndPackageArchitectureTest::isRestDto)
            .filter(javaClass -> !isInPackage(javaClass, WEB_DTO_PACKAGE))
            .map(JavaClass::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        Set.of(),
        actualViolations,
        "REST request/response DTOs should live in adapter.in.web.dto, not in API or domain packages.");
  }

  @Test
  void inputPortsShouldBeInterfacesWithUseCaseOrPortSuffix() {
    Set<String> actualViolations =
        productionClasses().stream()
            .filter(javaClass -> javaClass.getPackageName().contains(".application.port.in"))
            .filter(NamingAndPackageArchitectureTest::isTopLevelClass)
            .filter(
                javaClass ->
                    !javaClass.isInterface()
                        || !(javaClass.getSimpleName().endsWith("UseCase")
                            || javaClass.getSimpleName().endsWith("Port")))
            .map(JavaClass::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        Set.of(), actualViolations, "Input ports should be interfaces named *UseCase or *Port.");
  }

  @Test
  void outputPortsShouldBeInterfacesWithPortSuffix() {
    Set<String> actualViolations =
        productionClasses().stream()
            .filter(javaClass -> javaClass.getPackageName().contains(".application.port.out"))
            .filter(NamingAndPackageArchitectureTest::isTopLevelClass)
            .filter(
                javaClass ->
                    !javaClass.isInterface() || !javaClass.getSimpleName().endsWith("Port"))
            .map(JavaClass::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(Set.of(), actualViolations, "Output ports should be interfaces named *Port.");
  }

  @Test
  void servicesShouldBeApplicationServices() {
    Set<String> actualViolations =
        productionClasses().stream()
            .filter(javaClass -> javaClass.getSimpleName().endsWith("Service"))
            .filter(javaClass -> !javaClass.getPackageName().contains(".application"))
            .map(JavaClass::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        Set.of(),
        actualViolations,
        "Classes named *Service should orchestrate use cases in application packages.");
  }

  private static JavaClasses productionClasses() {
    return new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages(BASE_PACKAGE);
  }

  private static boolean isController(JavaClass javaClass) {
    return javaClass.isAnnotatedWith(RestController.class)
        || javaClass.isAnnotatedWith(Controller.class);
  }

  private static boolean isTopLevelClass(JavaClass javaClass) {
    return !javaClass.getName().contains("$");
  }

  private static boolean isRestDto(JavaClass javaClass) {
    return hasName(javaClass, name -> name.endsWith("Request") || name.endsWith("Response"))
        || javaClass.getName().equals(WEB_DTO_PACKAGE + ".BasicResponseEntity");
  }

  private static boolean hasName(JavaClass javaClass, Predicate<String> predicate) {
    return predicate.test(javaClass.getSimpleName());
  }

  private static boolean isInPackage(JavaClass javaClass, String packageName) {
    return javaClass.getPackageName().equals(packageName)
        || javaClass.getPackageName().startsWith(packageName + ".");
  }
}
