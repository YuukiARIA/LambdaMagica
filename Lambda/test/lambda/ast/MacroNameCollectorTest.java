package lambda.ast;

import java.util.Collections;
import java.util.Set;

import lambda.ast.parser.ParserException;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;

import static org.junit.Assert.assertThat;

public class MacroNameCollectorTest
{
	@Test
	public void returnsNothingForLambdaDoesNotContainAnyMacros() throws ParserException
	{
		Lambda lambda = Lambda.parse("\\z.(\\x.(\\f.f)x(\\a.z(a)))");
		Set<String> macroNames = MacroNameCollector.collectMacroName(lambda);
		assertThat(macroNames, equalTo(Collections.<String>emptySet()));
	}

	@Test
	public void returnsMacroNamesWhenLamdaContainsSomeMacros() throws ParserException
	{
		Lambda lambda = Lambda.parse("<id><123>(\\x.<aaa>x(\\a.(<macro>)))");
		Set<String> macroNames = MacroNameCollector.collectMacroName(lambda);
		assertThat(macroNames, hasItems("id", "123", "aaa", "macro"));
	}
}
