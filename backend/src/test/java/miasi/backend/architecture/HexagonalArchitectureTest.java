package miasi.backend.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class HexagonalArchitectureTest {

  private static final String BASE_PACKAGE = "miasi.backend";

  private static final Set<String> BOUNDED_CONTEXTS =
      Set.of("configuration", "schedule", "analysis", "authorization", "visualization");

  private static final Set<String> LEGACY_ROOT_PACKAGES =
      Set.of(
          "miasi.backend.adapter",
          "miasi.backend.api",
          "miasi.backend.bootstrap",
          "miasi.backend.config",
          "miasi.backend.database",
          "miasi.backend.domains",
          "miasi.backend.enums",
          "miasi.backend.eventListeners",
          "miasi.backend.events",
          "miasi.backend.sharedkernel");

  private static final Pattern HEXAGONAL_DOMAIN_PACKAGE =
      Pattern.compile(
          "^miasi\\.backend\\.(configuration|schedule|analysis|authorization|visualization)\\.domain(\\..*)?$");

  private static final Pattern HEXAGONAL_APPLICATION_PACKAGE =
      Pattern.compile(
          "^miasi\\.backend\\.(configuration|schedule|analysis|authorization|visualization)\\.application(\\..*)?$");

  private static final Pattern HEXAGONAL_INFRASTRUCTURE_PACKAGE =
      Pattern.compile(
          "^miasi\\.backend\\.(configuration|schedule|analysis|authorization|visualization|common)\\.infrastructure(\\..*)?$");

  @Test
  void productionCodeDoesNotUseLegacyRootPackages() {
    Set<String> actualViolations =
        productionClasses().stream()
            .map(JavaClass::getPackageName)
            .filter(
                packageName ->
                    LEGACY_ROOT_PACKAGES.stream()
                        .anyMatch(
                            legacyPackage ->
                                packageName.equals(legacyPackage)
                                    || packageName.startsWith(legacyPackage + ".")))
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        Set.of(),
        actualViolations,
        "Production code must use context/domain/application/infrastructure/common packages, not legacy roots.");
  }

  @Test
  void boundedContextClassesLiveUnderStandardLayerModules() {
    Set<String> actualViolations =
        productionClasses().stream()
            .filter(HexagonalArchitectureTest::isBoundedContextClass)
            .filter(
                javaClass ->
                    !javaClass
                        .getPackageName()
                        .matches(
                            "^miasi\\.backend\\.(configuration|schedule|analysis|authorization|visualization)(\\.(domain|application|infrastructure)(\\..*)?)?$"))
            .map(JavaClass::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        Set.of(),
        actualViolations,
        "Bounded contexts must be organized as context/domain, context/application or context/infrastructure.");
  }

  @Test
  void targetDomainPackagesDoNotDependOnApplicationOrInfrastructure() {
    Set<String> actualViolations =
        productionClasses().stream()
            .filter(
                javaClass -> HEXAGONAL_DOMAIN_PACKAGE.matcher(javaClass.getPackageName()).matches())
            .filter(
                javaClass ->
                    dependsOnAny(
                        javaClass,
                        packageName ->
                            HEXAGONAL_APPLICATION_PACKAGE.matcher(packageName).matches()
                                || HEXAGONAL_INFRASTRUCTURE_PACKAGE.matcher(packageName).matches()
                                || packageName.startsWith("org.springframework")))
            .map(JavaClass::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        Set.of(),
        actualViolations,
        "Domain packages may not depend on application, infrastructure or Spring packages.");
  }

  @Test
  void targetApplicationPackagesDoNotDependOnInfrastructure() {
    Set<String> actualViolations =
        productionClasses().stream()
            .filter(
                javaClass ->
                    HEXAGONAL_APPLICATION_PACKAGE.matcher(javaClass.getPackageName()).matches())
            .filter(
                javaClass ->
                    dependsOnAny(
                        javaClass,
                        packageName ->
                            HEXAGONAL_INFRASTRUCTURE_PACKAGE.matcher(packageName).matches()))
            .map(JavaClass::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        Set.of(),
        actualViolations,
        "Application packages may not depend on infrastructure packages.");
  }

  private static JavaClasses productionClasses() {
    return new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages(BASE_PACKAGE);
  }

  private static boolean dependsOnAny(JavaClass source, Predicate<String> packagePredicate) {
    return source.getDirectDependenciesFromSelf().stream()
        .map(Dependency::getTargetClass)
        .map(JavaClass::getPackageName)
        .anyMatch(packagePredicate);
  }

  private static boolean isBoundedContextClass(JavaClass javaClass) {
    String packageName = javaClass.getPackageName();
    return BOUNDED_CONTEXTS.stream()
        .anyMatch(
            context ->
                packageName.equals(BASE_PACKAGE + "." + context)
                    || packageName.startsWith(BASE_PACKAGE + "." + context + "."));
  }
}
