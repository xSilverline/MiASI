package configuration.modules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ModuleCatalogTest {
  @Mock
  Module mockModule;

  @Mock
  List<Module> mockList;

  @Test
  void addTest() {
    //Given
    ModuleCatalogExample catalog = new ModuleCatalogExample(mockList);

    //When
    catalog.add(mockModule);

    //Then
    then(mockList).should().add(mockModule);
//    then(mockList).shouldHaveNoMoreInteractions();
//    catalog.getFirstModuleName();

  }

  @Test
  void firstNameTest() {
    //Given
    String name = "Adam";
    List<Module> moduleList = new ArrayList<>();
    moduleList.add(mockModule);
    ModuleCatalogExample catalog = new ModuleCatalogExample(moduleList);
    given(mockModule.getName()).willReturn(name);

    //When
    String result = catalog.getFirstModuleName();

    //Then
    assertEquals(name, result);
  }

  @Test
  void fistCatalogIT() {

  }

}