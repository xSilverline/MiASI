package miasi.backend.domains.analysis.types.core;

import miasi.backend.domains.analysis.types.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyBalanceTest {

  @Test
  void shouldAddNewProductionResource() {
    // Given
    DailyBalance balance = new DailyBalance();
    Resource resource = new Resource(ResourceType.OXYGEN, 10);

    // When
    balance.addProduction(resource);

    // Then
    assertEquals(10, balance.getProduced().getFirst().getAmount());
  }

  @Test
  void shouldMergeExistingProductionResource() {
    // Given
    DailyBalance balance = new DailyBalance();
    balance.addProduction(new Resource(ResourceType.WATER, 10));

    // When
    balance.addProduction(new Resource(ResourceType.WATER, 5));

    // Then
    assertEquals(15, balance.getProduced().getFirst().getAmount());
  }

  @Test
  void shouldInitializeProductionWhenNull() {
    // Given
    DailyBalance balance = new DailyBalance(null, new ArrayList<>());

    // When
    balance.addProduction(new Resource(ResourceType.FOOD, 20));

    // Then
    assertEquals(20, balance.getProduced().getFirst().getAmount());
  }

  @Test
  void shouldAddNewConsumptionResource() {
    // Given
    DailyBalance balance = new DailyBalance();

    // When
    balance.addConsumption(new Resource(ResourceType.FOOD, 15));

    // Then
    assertEquals(15, balance.getConsumed().getFirst().getAmount());
  }

  @Test
  void shouldMergeExistingConsumptionResource() {
    // Given
    DailyBalance balance = new DailyBalance();

    balance.addConsumption(new Resource(ResourceType.WATER, 10));

    // When
    balance.addConsumption(new Resource(ResourceType.WATER, 5));

    // Then
    assertEquals(15, balance.getConsumed().getFirst().getAmount());
  }

  @Test
  void shouldInitializeConsumptionWhenNull() {
    // Given
    DailyBalance balance = new DailyBalance(new ArrayList<>(), null);

    // When
    balance.addConsumption(new Resource(ResourceType.OXYGEN, 30));

    // Then
    assertEquals(30, balance.getConsumed().getFirst().getAmount());
  }

  @Test
  void shouldApplyBalanceToInventory() {
    // Given
    DailyBalance balance = new DailyBalance();

    balance.addProduction(new Resource(ResourceType.FOOD, 20));
    balance.addConsumption(new Resource(ResourceType.FOOD, 5));

    // When
    List<Resource> result = balance.applyTo(List.of(new Resource(ResourceType.FOOD, 10)));

    // Then
    assertEquals(25, result.getFirst().getAmount());
  }

  @Test
  void shouldApplyBalanceWithoutInitialInventory() {
    // Given
    DailyBalance balance = new DailyBalance();

    balance.addProduction(new Resource(ResourceType.OXYGEN, 50));

    // When
    List<Resource> result = balance.applyTo(null);

    // Then
    assertEquals(50, result.getFirst().getAmount());
  }

  @Test
  void shouldHandleNullProducedListInApplyTo() {
    // Given
    DailyBalance balance = new DailyBalance(null, new ArrayList<>());

    // When
    List<Resource> result = balance.applyTo(List.of(new Resource(ResourceType.WATER, 10)));

    // Then
    assertEquals(10, result.getFirst().getAmount());
  }

  @Test
  void shouldHandleNullConsumedListInApplyTo() {
    // Given
    DailyBalance balance = new DailyBalance(new ArrayList<>(), null);

    balance.addProduction(new Resource(ResourceType.FOOD, 15));

    // When
    List<Resource> result = balance.applyTo(null);

    // Then
    assertEquals(15, result.getFirst().getAmount());
  }

  @Test
  void shouldCreateMultipleInventoryEntries() {
    // Given
    DailyBalance balance = new DailyBalance();

    balance.addProduction(new Resource(ResourceType.FOOD, 10));
    balance.addProduction(new Resource(ResourceType.WATER, 20));

    // When
    List<Resource> result = balance.applyTo(null);

    // Then
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(r -> r.getType() == ResourceType.FOOD));
    assertTrue(result.stream().anyMatch(r -> r.getType() == ResourceType.WATER));
  }

  @Test
  void shouldSubtractConsumptionFromInventory() {
    // Given
    DailyBalance balance = new DailyBalance();

    balance.addConsumption(new Resource(ResourceType.WATER, 30));

    // When
    List<Resource> result = balance.applyTo(List.of(new Resource(ResourceType.WATER, 100)));

    // Then
    assertEquals(70, result.getFirst().getAmount());
  }
}