package net.sourceforge.kolmafia.textui.parsetree;

import static net.sourceforge.kolmafia.textui.ScriptData.invalid;
import static net.sourceforge.kolmafia.textui.ScriptData.valid;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import net.sourceforge.kolmafia.textui.ParserTest;
import net.sourceforge.kolmafia.textui.ScriptData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ForLoopTest {
  public static Stream<ScriptData> data() {
    return Stream.of(
        invalid(
            "For loop, no/bad initial expression",
            "for x from",
            "Expression for initial value expected"),
        invalid(
            "For loop, no/bad ceiling expression",
            "for x from 0 to",
            "Expression for floor/ceiling value expected"),
        invalid(
            "For loop, no/bad increment expression",
            "for x from 0 to 1 by",
            "Expression for increment value expected"),
        valid(
            "for-from-to",
            "for i from 1 to 10000;",
            Arrays.asList("for", "i", "from", "1", "to", "10000", ";"),
            Arrays.asList("1-1", "1-5", "1-7", "1-12", "1-14", "1-17", "1-22")),
        valid(
            "for-from-upto",
            "for i from 1 upto 10000;",
            Arrays.asList("for", "i", "from", "1", "upto", "10000", ";"),
            Arrays.asList("1-1", "1-5", "1-7", "1-12", "1-14", "1-19", "1-24")),
        valid(
            "for-from-to-by",
            "for i from 1 to 10000 by 100;",
            Arrays.asList("for", "i", "from", "1", "to", "10000", "by", "100", ";"),
            Arrays.asList("1-1", "1-5", "1-7", "1-12", "1-14", "1-17", "1-23", "1-26", "1-29"),
            scope -> {
              List<Command> commands = scope.getCommandList();

              // Loop location test
              ForLoop forLoop = assertInstanceOf(ForLoop.class, commands.get(0));
              // From the "for" up to the end of its scope
              ParserTest.assertLocationEquals(1, 1, 1, 30, forLoop.getLocation());

              // Scope location test
              Scope loopScope = forLoop.getScope();
              ParserTest.assertLocationEquals(1, 29, 1, 30, loopScope.getLocation());

              // Variable + VariableReference location test
              Iterator<Variable> variables = loopScope.getVariables().iterator();
              assertTrue(variables.hasNext());
              Variable var = variables.next();
              VariableReference varRef = forLoop.getVariable();
              ParserTest.assertLocationEquals(1, 5, 1, 6, var.getLocation());
              ParserTest.assertLocationEquals(1, 5, 1, 6, varRef.getLocation());
              assertSame(var, varRef.target);
              assertFalse(variables.hasNext());
            }),
        valid(
            "for-from-downto",
            // This is valid, but will immediately return.
            "for i from 1 downto 10000;",
            Arrays.asList("for", "i", "from", "1", "downto", "10000", ";"),
            Arrays.asList("1-1", "1-5", "1-7", "1-12", "1-14", "1-21", "1-26")),
        invalid(
            "for with reserved index",
            "for int from 1 upto 10;",
            "Reserved word 'int' cannot be an index variable"),
        invalid(
            "for with existing index",
            // Oddly, this is unsupported, when other for loops will create
            // a nested scope.
            "int i; for i from 1 upto 10;",
            "Index variable 'i' is already defined"),
        invalid("for without from", "for i in range(10):\n  print(i)", "Expected from, found in"),
        invalid(
            "for with invalid dest keyword",
            "for i from 1 until 10;",
            "Expected to, upto, or downto, found until"));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testScriptValidity(ScriptData script) {
    ParserTest.testScriptValidity(script);
  }
}