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

  private static final Set<String> FORBIDDEN_LEGACY_DOMAIN_DEPENDENCY_PACKAGES =
      Set.of(
          "org.springframework",
          "miasi.backend.api",
          "miasi.backend.database",
          "miasi.backend.events");

  private static final Set<String> DOCUMENTED_LEGACY_DOMAIN_EXCEPTIONS =
      Set.of("miasi.backend.domains.schedule.MissionPlanEventInbox");

  private static final Pattern HEXAGONAL_DOMAIN_PACKAGE =
      Pattern.compile(
          "^miasi\\.backend\\.(configuration|schedule|analysis|authorization|visualization)\\.domain(\\..*)?$");

  private static final Pattern HEXAGONAL_APPLICATION_PACKAGE =
      Pattern.compile(
          "^miasi\\.backend\\.(configuration|schedule|analysis|authorization|visualization)\\.application(\\..*)?$");

  private static final Pattern HEXAGONAL_ADAPTER_PACKAGE =
      Pattern.compile(
          "^miasi\\.backend\\.(configuration|schedule|analysis|authorization|visualization)\\.adapter(\\..*)?$");

  @Test
  void globalEnumsPackageIsNotUsedAsSharedKernel() {
    Set<String> actualViolations =
        productionClasses().stream()
            .map(JavaClass::getPackageName)
            .filter(packageName -> packageName.equals(BASE_PACKAGE + ".enums"))
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        Set.of(),
        actualViolations,
        "Use sharedkernel.model for stable shared types or a bounded-context domain package.");
  }

  @Test
  void legacyDomainsHaveOnlyDocumentedFrameworkOrAdapterDependencies() {
    Set<String> actualViolations =
        productionClasses().stream()
            .filter(javaClass -> javaClass.getPackageName().startsWith(BASE_PACKAGE + ".domains."))
            .filter(
                javaClass ->
                    dependsOnAny(
                        javaClass,
                        HexagonalArchitectureTest::isForbiddenLegacyDomainDependencyPackage))
            .map(JavaClass::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        new TreeSet<>(DOCUMENTED_LEGACY_DOMAIN_EXCEPTIONS),
        actualViolations,
        "Any legacy domain dependency on Spring/API/database/events must be explicit and temporary.");
  }

  @Test
  void targetDomainPackagesDoNotDependOnApplicationOrAdapters() {
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
                                || HEXAGONAL_ADAPTER_PACKAGE.matcher(packageName).matches()))
            .map(JavaClass::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        Set.of(),
        actualViolations,
        "Target hexagonal domain packages may not depend on application or adapter packages.");
  }

  @Test
  void targetApplicationPackagesDoNotDependOnAdapters() {
    Set<String> actualViolations =
        productionClasses().stream()
            .filter(
                javaClass ->
                    HEXAGONAL_APPLICATION_PACKAGE.matcher(javaClass.getPackageName()).matches())
            .filter(
                javaClass ->
                    dependsOnAny(
                        javaClass,
                        packageName -> HEXAGONAL_ADAPTER_PACKAGE.matcher(packageName).matches()))
            .map(JavaClass::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(
        Set.of(),
        actualViolations,
        "Target hexagonal application packages may not depend on adapter packages.");
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

  private static boolean isForbiddenLegacyDomainDependencyPackage(String packageName) {
    return FORBIDDEN_LEGACY_DOMAIN_DEPENDENCY_PACKAGES.stream()
        .anyMatch(
            forbiddenPackage ->
                packageName.equals(forbiddenPackage)
                    || packageName.startsWith(forbiddenPackage + "."));
  }
}
