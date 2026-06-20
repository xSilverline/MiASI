package miasi.backend.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "miasi.backend", importOptions = ImportOption.DoNotIncludeTests.class)
public class PackageArchTests {

  @ArchTest()
  static final ArchRule enumPackage = classes()
      .that().areEnums()
      .should().resideInAPackage("..enums..");
}

