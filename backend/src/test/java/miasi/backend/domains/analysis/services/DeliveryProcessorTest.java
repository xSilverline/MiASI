package miasi.backend.domains.analysis.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import miasi.backend.domains.analysis.domain.schedule.DeliveryProcessor;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.schedule.Delivery;
import org.junit.jupiter.api.Test;

class DeliveryProcessorTest {

  @Test
  void shouldAddModulesAndResourcesWhenDeliveryIsToday() {
    // Given
    DeliveryProcessor processor = new DeliveryProcessor();
    Delivery delivery = mock(Delivery.class);
    Module module = mock(Module.class);

    when(delivery.getSol()).thenReturn(5);
    when(delivery.getModules()).thenReturn(List.of(module));
    when(delivery.getResources()).thenReturn(
        List.of(new Resource(ResourceType.WATER, 20))
    );

    List<Module> modules = new ArrayList<>();
    List<Resource> warehouse = new ArrayList<>();

    // When
    processor.process(
        5,
        List.of(delivery),
        modules,
        warehouse
    );

    // Then
    assertEquals(1, modules.size());
    assertEquals(1, warehouse.size());
    assertEquals(20, warehouse.getFirst().getAmount());
  }

  @Test
  void shouldIncreaseExistingResourceAmount() {// Given
    DeliveryProcessor processor = new DeliveryProcessor();
    Delivery delivery = mock(Delivery.class);
    Resource existing = new Resource(ResourceType.WATER, 50);

    when(delivery.getSol()).thenReturn(1);
    when(delivery.getResources()).thenReturn(
        List.of(new Resource(ResourceType.WATER, 30))
    );

    List<Resource> warehouse = new ArrayList<>(List.of(existing));

    // When
    processor.process(
        1,
        List.of(delivery),
        null,
        warehouse
    );

    // Then
    assertEquals(80, warehouse.getFirst().getAmount());
    assertEquals(1, warehouse.size());
  }

  @Test
  void shouldAddResourceWhenWarehouseDoesNotContainIt() {
    // Given
    DeliveryProcessor processor = new DeliveryProcessor();
    Delivery delivery = mock(Delivery.class);
    when(delivery.getSol()).thenReturn(2);
    when(delivery.getResources()).thenReturn(
        List.of(new Resource(ResourceType.FOOD, 15))
    );
    List<Resource> warehouse = new ArrayList<>();

    // When
    processor.process(
        2,
        List.of(delivery),
        null,
        warehouse
    );

    // Then
    assertEquals(
        1,
        warehouse.size()
    );
    assertEquals(
        ResourceType.FOOD,
        warehouse.getFirst().getType()
    );
    assertEquals(
        15,
        warehouse.getFirst().getAmount()
    );
  }

  @Test
  void shouldNotAddModulesWhenModulesAreNull() {
    // Given
    DeliveryProcessor processor = new DeliveryProcessor();
    Delivery delivery = mock(Delivery.class);
    when(delivery.getSol()).thenReturn(1);
    when(delivery.getModules()).thenReturn(List.of(mock(Module.class)));

    // When
    processor.process(
        1,
        List.of(delivery),
        null,
        new ArrayList<>()
    );

    // Then
    verify(delivery).getModules();
  }

  @Test
  void shouldSkipResourcesWhenResourcesAreNull() {
    // Given
    DeliveryProcessor processor = new DeliveryProcessor();
    Delivery delivery = mock(Delivery.class);
    when(delivery.getSol()).thenReturn(1);
    when(delivery.getResources()).thenReturn(null);
    List<Resource> warehouse = new ArrayList<>();

    // When
    processor.process(
        1,
        List.of(delivery),
        new ArrayList<>(),
        warehouse
    );

    // Then
    assertTrue(warehouse.isEmpty());
  }

  @Test
  void shouldSkipResourcesWhenWarehouseIsNull() {
    // Given
    DeliveryProcessor processor = new DeliveryProcessor();
    Delivery delivery = mock(Delivery.class);
    when(delivery.getSol()).thenReturn(1);
    when(delivery.getResources()).thenReturn(
        List.of(new Resource(ResourceType.OXYGEN, 10))
    );

    // When
    processor.process(
        1,
        List.of(delivery),
        new ArrayList<>(),
        null
    );

    // Then
    verify(delivery).getResources();
  }

  @Test
  void shouldIgnoreDeliveryFromDifferentDay() {
    // Given
    DeliveryProcessor processor = new DeliveryProcessor();
    Delivery delivery = mock(Delivery.class);
    when(delivery.getSol()).thenReturn(10);
    List<Resource> warehouse = new ArrayList<>();

    // When
    processor.process(
        5,
        List.of(delivery),
        new ArrayList<>(),
        warehouse
    );

    // Then
    assertTrue(warehouse.isEmpty());
  }

  @Test
  void shouldDoNothingWhenDeliveriesAreNull() {
    // Given
    DeliveryProcessor processor = new DeliveryProcessor();

    // When
    processor.process(
        1,
        null,
        new ArrayList<>(),
        new ArrayList<>()
    );

    // Then
    assertTrue(true);
  }

  @Test
  void shouldProcessOnlyMatchingDeliveryFromManyDeliveries() {
    // Given
    DeliveryProcessor processor = new DeliveryProcessor();
    Delivery today = mock(Delivery.class);
    Delivery tomorrow = mock(Delivery.class);

    when(today.getSol()).thenReturn(5);
    when(today.getResources()).thenReturn(
        List.of(new Resource(ResourceType.WATER, 10))
    );
    when(tomorrow.getSol()).thenReturn(6);
    when(tomorrow.getResources()).thenReturn(
        List.of(new Resource(ResourceType.WATER, 100))
    );
    List<Resource> warehouse = new ArrayList<>();

    // When
    processor.process(
        5,
        List.of(today, tomorrow),
        new ArrayList<>(),
        warehouse
    );

    // Then
    assertEquals(10, warehouse.getFirst().getAmount());
  }
}