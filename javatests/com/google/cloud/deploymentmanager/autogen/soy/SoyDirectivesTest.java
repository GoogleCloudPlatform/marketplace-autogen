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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.deploymentmanager.autogen.soy.SoyDirectives.Doublequoted;
import com.google.cloud.deploymentmanager.autogen.soy.SoyDirectives.Indent;
import com.google.cloud.deploymentmanager.autogen.soy.SoyDirectives.Lowercased;
import com.google.cloud.deploymentmanager.autogen.soy.SoyDirectives.Quoted;
import com.google.cloud.deploymentmanager.autogen.soy.SoyDirectives.ReplaceAll;
import com.google.cloud.deploymentmanager.autogen.soy.SoyDirectives.Trim;
import com.google.cloud.deploymentmanager.autogen.soy.SoyDirectives.Uppercased;
import com.google.cloud.deploymentmanager.autogen.soy.SoyDirectives.YamlPrimitive;
import com.google.common.collect.ImmutableList;
import com.google.template.soy.data.SanitizedContents;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.SoyValueConverter;
import com.google.template.soy.shared.restricted.SoyJavaPrintDirective;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests {@link SoyDirectives}. */
@RunWith(JUnit4.class)
public class SoyDirectivesTest {

  Doublequoted doublequoted;
  Indent indent;
  Lowercased lowercased;
  Quoted quoted;
  ReplaceAll replaceAll;
  Trim trim;
  Uppercased uppercased;
  YamlPrimitive yamlPrimitive;

  @Before
  public void createDirectives() {
    doublequoted = new Doublequoted();
    indent = new Indent();
    lowercased = new Lowercased();
    quoted = new Quoted();
    replaceAll = new ReplaceAll();
    trim = new Trim();
    uppercased = new Uppercased();
    yamlPrimitive = new YamlPrimitive();
  }

  @Test
  public void testDoublequoted() {
    assertOutput("abc", "abc", doublequoted);
    assertOutput("ab\\\\c", "ab\\c", doublequoted);
    assertOutput("ab\\\"c", "ab\"c", doublequoted);
  }

  @Test
  public void testIndent() {
    assertOutput("abc", "abc", indent, 0);
    assertOutput("abc", "abc", indent, 4);
    assertOutput("    abc", "abc", indent, 4, true);
    assertOutput("abc\ndef", "abc\ndef", indent, 0);
    assertOutput("abc\n  def", "abc\ndef", indent, 2);
    assertOutput("  abc\n  def", "abc\ndef", indent, 2, true);
    assertOutput("\n\n", "\n\n", indent, 2, true);
    assertOutput(" \n \n\n ", " \n \n\n ", indent, 2, true);
    assertOutput(
        "  abc\n\n  def\n \n", "abc\n\ndef\n \n", indent, 2, true);
  }
  
  @Test
  public void testLowercased() {
    assertOutput("abcd", "AbCd", lowercased);
  }

  @Test
  public void testQuoted() {
    assertOutput("abc", "abc", quoted);
    assertOutput("ab''s", "ab's", quoted);
  }

  @Test
  public void testReplaceAll() {
    assertOutput("abcdefabc", "123def123", replaceAll, "123", "abc");
    assertOutput("abcdefabc", "abcdefabc", replaceAll, "123", "abc");
    assertOutput("def", "def", replaceAll, "123", "abc");
  }

  @Test
  public void testTrim() {
    assertOutput("", "", trim, "");
    assertOutput("", "  ", trim, "");
    assertOutput("a", " a ", trim, "");
    assertOutput("", "\n\n", trim, "");
    assertOutput("a", "\na\n", trim, "");
    assertOutput("a", " \na \n", trim, "");
    assertOutput("\na\n ", " \na\n ", trim, "leading,spaces");
    assertOutput("\n a\n ", "\n a\n ", trim, "leading,spaces");
    assertOutput("\n a ", "\n a \n", trim, "trailing,newlines");
    assertOutput("\n a\n ", "\n a\n ", trim, "trailing,newlines");
  }

  @Test
  public void testUppercased() {
    assertOutput("ABCD", "AbCd", uppercased);
  }

  @Test
  public void testYamlPrimitive() {
    assertOutput("true", true, yamlPrimitive, 0);
    assertOutput("sample string", "sample string", yamlPrimitive, 0);
    assertOutput("sample string", "sample string", yamlPrimitive, 2);
    assertOutput(
        "|-\n  sample\n  multiline", "sample\nmultiline", yamlPrimitive, 0);
    assertOutput(
        "|-\n    sample\n    multiline", "sample\nmultiline", yamlPrimitive, 2);
    assertOutput("1000", 1000, yamlPrimitive, 0);
    assertOutput("100.0", 100.0f, yamlPrimitive, 0);
    assertOutput("'100.0'", "100.0", yamlPrimitive, 0);
    assertOutput("'''abc'''", "'abc'", yamlPrimitive, 0);
    assertOutput(
        "abc", SanitizedContents.constantHtml("abc"), yamlPrimitive, 0);
  }

  private static void assertOutput(
      Object expected, SoyValue value, SoyJavaPrintDirective directive, Object... args) {
    ImmutableList.Builder<SoyValue> argsData = ImmutableList.builder();
    for (Object arg : args) {
      argsData.add(SoyValueConverter.UNCUSTOMIZED_INSTANCE.convert(arg).resolve());
    }
    assertThat(directive.applyForJava(value, argsData.build()))
        .isEqualTo(SoyValueConverter.UNCUSTOMIZED_INSTANCE.convert(expected).resolve());
  }

  private static void assertOutput(
      Object expected, Object value, SoyJavaPrintDirective directive, Object... args) {
    assertOutput(expected, SoyValueConverter.UNCUSTOMIZED_INSTANCE.convert(value).resolve(),
        directive, args);
  }
}
