package miasi.backend.domains.configuration.other;

import static org.junit.jupiter.api.Assertions.assertEquals;

import miasi.backend.domains.configuration.SexProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SexProfileTest {

  @Nested
  class changePopulation {
    SexProfile profile;
    int start;

    @BeforeEach
    void prep() {
      // given - before each test
      profile = new SexProfile();
      start = profile.getPopulation();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, -3, 0})
    void add(int number) {
      // given
      int start = profile.getPopulation();

      // when
      profile.changePopulation(number);

      // then
      assertEquals(start + number, profile.getPopulation());
    }
  }
}
