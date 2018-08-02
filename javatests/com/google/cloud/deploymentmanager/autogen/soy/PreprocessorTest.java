// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.cloud.deploymentmanager.autogen.soy;

import static com.google.cloud.deploymentmanager.autogen.soy.Preprocessor.ESCAPE_REGEX;
import static com.google.cloud.deploymentmanager.autogen.soy.Preprocessor.SOY_COMMAND_BLACKLIST_REGEX;
import static com.google.cloud.deploymentmanager.autogen.soy.Preprocessor.SOY_COMMAND_REGEX;
import static com.google.cloud.deploymentmanager.autogen.soy.Preprocessor.SOY_COMMAND_TO_COLLAPSE_REGEX;
import static com.google.cloud.deploymentmanager.autogen.soy.Preprocessor.removeDirectives;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.cloud.deploymentmanager.autogen.soy.Preprocessor;
import com.google.common.base.Joiner;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link Preprocessor}.
 */
@RunWith(JUnit4.class)
public class PreprocessorTest {
  
  @Test
  public void testLineBreakEscapeCharRegex() {
    assertTrue(ESCAPE_REGEX.matcher("{sp}").matches());
    assertTrue(ESCAPE_REGEX.matcher("{nil}").matches());
    assertTrue(ESCAPE_REGEX.matcher("{\\n}").matches());
    assertTrue(ESCAPE_REGEX.matcher("{\\r}").matches());
    assertTrue(ESCAPE_REGEX.matcher("{\\t}").matches());
    assertFalse(ESCAPE_REGEX.matcher("{lb}").matches());
    assertFalse(ESCAPE_REGEX.matcher("{rb}").matches());
    assertFalse(ESCAPE_REGEX.matcher("{if}").matches());
  }
  
  @Test
  public void testSoyCommandRegex() {
    assertTrue(SOY_COMMAND_REGEX.matcher("{template .abc kind=\"text\"}").matches());
    assertTrue(SOY_COMMAND_REGEX.matcher("{if $a > $b}").matches());
    assertTrue(SOY_COMMAND_REGEX.matcher("{/for}").matches());
    assertTrue(SOY_COMMAND_REGEX.matcher("{@param $a: string}").matches());
    assertTrue(SOY_COMMAND_REGEX.matcher("{@param? $a: string}").matches());
    assertTrue(SOY_COMMAND_REGEX.matcher("{param $p: 'abc'/}").matches());
    assertTrue(SOY_COMMAND_REGEX.matcher("{print $a |capitalized}").matches());
    assertTrue(SOY_COMMAND_REGEX.matcher("{sp}").matches());
    assertTrue(SOY_COMMAND_REGEX.matcher("{lb}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{$param}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{ $param }").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{'abc'}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("some{sp}text").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("some text").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{{ jinja_variable }}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{{ jinja_variable -}}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{{- jinja_variable }}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{{- jinja_variable -}}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{% if jinja_boolean %}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{%- if jinja_boolean -%}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{% if jinja_boolean -%}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{%- if jinja_boolean %}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{%- if jinja_boolean -%}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{# jinja comment #}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{# jinja comment -#}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{#- jinja comment #}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("{#- jinja comment -#}").matches());
    assertFalse(SOY_COMMAND_REGEX.matcher("<head>").matches());
  }
  
  @Test
  public void testSoyCommandBlacklistRegex() {
    assertTrue(SOY_COMMAND_BLACKLIST_REGEX.matcher("{sp}").matches());
    assertTrue(SOY_COMMAND_BLACKLIST_REGEX.matcher("{nil}").matches());
    assertTrue(SOY_COMMAND_BLACKLIST_REGEX.matcher("{lb}").matches());
    assertTrue(SOY_COMMAND_BLACKLIST_REGEX.matcher("{rb}").matches());
    assertTrue(SOY_COMMAND_BLACKLIST_REGEX.matcher("{\\n}").matches());
    assertTrue(SOY_COMMAND_BLACKLIST_REGEX.matcher("{\\r}").matches());
    assertTrue(SOY_COMMAND_BLACKLIST_REGEX.matcher("{\\t}").matches());
    assertTrue(SOY_COMMAND_BLACKLIST_REGEX.matcher("{print $abc}").matches());
    assertFalse(SOY_COMMAND_BLACKLIST_REGEX.matcher("{if a > b}").matches());
  }
  
  @Test
  public void testSoyCommandToCollapseRegex() {
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{template .abc kind=\"text\"}").matches());
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{if $a > $b}").matches());
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{/for}").matches());
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{@param $a: string}").matches());
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{@param? $a: string}").matches());
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{param $p: 'abc'/}").matches());
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{print $a |capitalized}").matches());
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{sp}").matches());
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{lb}").matches());
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{$param}").matches());
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{ $param }").matches());
    assertTrue(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{'abc'}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("some{sp}text").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("some text").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{{ jinja_variable }}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{{ jinja_variable -}}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{{- jinja_variable }}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{{- jinja_variable -}}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{% if jinja_boolean %}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{% if jinja_boolean -%}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{%- if jinja_boolean %}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{%- if jinja_boolean -%}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{# jinja comment #}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{# jinja comment -#}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{#- jinja comment #}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("{#- jinja comment -#}").matches());
    assertFalse(SOY_COMMAND_TO_COLLAPSE_REGEX.matcher("<head>").matches());
  }

  @Test
  public void testRemoveDirectives() {
    assertThat(removeDirectives("{plsp}")).isEqualTo("");
    assertThat(removeDirectives("  {plsp}")).isEqualTo("  ");
    assertThat(removeDirectives("  {plsp}abc")).isEqualTo("  abc");
    assertThat(removeDirectives("abc{plsp}def")).isEqualTo("abcdef");
    assertThat(removeDirectives("abc {plsp}def")).isEqualTo("abc def");
    assertThat(removeDirectives("abc{plsp}  ")).isEqualTo("abc  ");
  }
  
  @Test
  public void testStripComments() {
    assertThat(
        Preprocessor.stripComments(lines(
            "http://abc",
            "def //comment here  ", 
            "//comment here  ",
            "   // more comment here  ",
            "ghi")))
        .isEqualTo(lines(
            "http://abc",
            "def",
            "",
            "",
            "ghi"));

    assertThat(
        Preprocessor.stripComments(lines(
            "abc /** comment here */ def /**comment here*/",
            "/**comment here*/  ",
            "  /**comment here*/ ",
            "/**",
            " * long",
            " * comment",
            " */",
            "ghi")))
        .isEqualTo(lines(
            "abc def",
            "  ",
            " ",
            "",
            "ghi"));
    
    assertThat(
        Preprocessor.stripComments(lines(
            "// comment /** now",
            "this shouldn't be a comment",
            "neither is this tag */")))
        .isEqualTo(lines(
            "",
            "this shouldn't be a comment",
            "neither is this tag */"));
  }
  
  @Test
  public void testCollapseMultilineCommandsAndEscapeJinjaDelimiters() {
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(lines(
            "{if a = b}")))
        .isEqualTo(lines(
            "{if a = b}"));
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(lines(
            "{if a  ",
            "   = b}")))
        .isEqualTo(lines(
            "{if a = b}"));    
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(lines(
            "{if",
            "a = b}")))
        .isEqualTo(lines(
            "{if a = b}"));
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(lines(
            "{if  ",
            "  a = b}")))
        .isEqualTo(lines(
            "{if a = b}"));
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(lines(
            "{($a ",
            " + $b) * 3",
            "}")))
        .isEqualTo(lines(
            "{($a + $b) * 3 }"));
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(lines(
            "{'abc'",
            " + $a}")))
        .isEqualTo(lines(
            "{'abc' + $a}"));
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(lines(
            "{'abc'",
            " + $a}")))
        .isEqualTo(lines(
            "{'abc' + $a}"));
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(lines(
            "{$a ",
            "  |directive}")))
        .isEqualTo(lines(
            "{$a |directive}"));
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(lines(
            "{ $a ",
            "  |directive}")))
        .isEqualTo(lines(
            "{ $a |directive}"));
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(lines(
            "{#  ",
            "  jinja",
            "  comment #}")))
        .isEqualTo(lines(
            "{lb}#  ",
            "  jinja",
            "  comment #{rb}"));
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(
            "{% if a > b %}{{ a }}{% endif %}"))
        .isEqualTo("{lb}% if a > b %{rb}{lb}{lb} a {rb}{rb}{lb}% endif %{rb}");
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(
            "{%- if a > b -%}{{- a -}}{%- endif -%}"))
        .isEqualTo("{lb}%- if a > b -%{rb}{lb}{lb}- a -{rb}{rb}{lb}%- endif -%{rb}");
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(
            "{if supportA}{{ a }}{/if}"))
        .isEqualTo("{if supportA}{lb}{lb} a {rb}{rb}{/if}");
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(
            "abc: {# comment here #}"))
        .isEqualTo("abc: {lb}# comment here #{rb}");
    // Should not escape the ones inside a literal string inside a (even multilined) command. 
    assertThat(
        Preprocessor.collapseMultilineCommandsAndEscapeJinjaDelimiters(lines(
            "{if $a == ", 
            "'{{ test }}'}{{ no_test }}{/if}")))
        .isEqualTo("{if $a == '{{ test }}'}{lb}{lb} no_test {rb}{rb}{/if}");
  }

  @Test
  public void testHasOnlySoyCommandsPredicate() {
    assertHasOnlySoyCommands("{template}");
    assertHasOnlySoyCommands("{/template}");
    assertHasOnlySoyCommands("{@param abc: string}");
    assertHasOnlySoyCommands("{if a > b}");
    assertHasOnlySoyCommands("{@param abc: string}{@param def: int}");
    assertHasOnlySoyCommands("{@param? abc: string}{@param? def: int}");
    assertHasOnlySoyCommands("{@param? abc: string} {@param? def: int}");
    assertHasOtherThanSoyCommands("{$abc}");
    assertHasOtherThanSoyCommands("{ $abc }");
    assertHasOtherThanSoyCommands("{'xyz'}");
    assertHasOtherThanSoyCommands("{ 'xyz' }");
    assertHasOtherThanSoyCommands("{lb}");
    assertHasOtherThanSoyCommands("{print $abc}");
    assertHasOtherThanSoyCommands("{if a > b}{print $abc}{/if}");
  }

  @Test
  public void testHasEscapesPredicate() {
    assertHasEscapes("{sp}");
    assertHasEscapes("{sp}{sp}xtest");
    assertHasEscapes("test {sp} test");
    assertHasEscapes("test {\\n}");
    assertHasEscapes("test {\\r} test");
    assertHasEscapes("test {nil} test");
    assertHasEscapes("test {\\t} test");
    assertHasNoEscapes("{lb}");
    assertHasNoEscapes("{lb} {rb}");
  }
  
  @Test
  public void testPreprocess() {
    // Quick test. A much more extensive test is executed as part of TemplateRendererTest.
    String preprocessed = Preprocessor.preprocess(lines(
        "",
        "resources:", 
        "{if a > b}",
        "{\\n}",
        "  {$value}"));
    assertThat(preprocessed).isEqualTo(lines(
        "",
        "{nil}resources:{\\n}",
        "{if a > b}",
        "{\\n}",
        "{nil}  {$value}{\\n}"));
  }

  private static void assertHasOnlySoyCommands(String trimmedLine) {
    assertThat(Preprocessor.HAS_ONLY_SOY_COMMANDS.apply(trimmedLine)).isTrue();
  }

  private static void assertHasOtherThanSoyCommands(String trimmedLine) {
    assertThat(Preprocessor.HAS_ONLY_SOY_COMMANDS.apply(trimmedLine)).isFalse();
  }

  private static void assertHasEscapes(String trimmedLine) {
    assertThat(Preprocessor.HAS_ESCAPES.apply(trimmedLine)).isTrue();
  }

  private static void assertHasNoEscapes(String trimmedLine) {
    assertThat(Preprocessor.HAS_ESCAPES.apply(trimmedLine)).isFalse();
  }

  private static String lines(String... lines) {
    return Joiner.on('\n').join(Arrays.asList(lines));
  }
}
