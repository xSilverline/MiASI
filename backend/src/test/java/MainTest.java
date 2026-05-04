import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MainTest {

  @Test
  @DisplayName("Should print 'Hello, World!'")
  void mainShouldPrintExpectedString() {
    // Given
    String expected = "Hello, World!";
    ByteArrayOutputStream terminalOutput = new ByteArrayOutputStream();
    PrintStream oldStream = System.out;
    System.setOut(new PrintStream(terminalOutput, true));

    // When
    Main.main(new String[0]);

    // Then
    assertEquals(expected, terminalOutput.toString().trim());

    // restore old output stream
    System.setOut(oldStream);
  }
}
