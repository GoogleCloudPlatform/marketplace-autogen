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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.escape.CharEscaperBuilder;
import com.google.common.escape.Escaper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.template.soy.data.SanitizedContent;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.restricted.BooleanData;
import com.google.template.soy.data.restricted.FloatData;
import com.google.template.soy.data.restricted.IntegerData;
import com.google.template.soy.data.restricted.PrimitiveData;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.shared.restricted.SoyJavaPrintDirective;
import com.google.template.soy.shared.restricted.SoyPrintDirective;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;
import org.yaml.snakeyaml.serializer.Serializer;

/**
 * Adds additional soy print directives to support our templating.
 */
final class SoyDirectives {

  static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      Multibinder<SoyPrintDirective> soyDirectiveSetBinder =
          Multibinder.newSetBinder(binder(), SoyPrintDirective.class);
      soyDirectiveSetBinder.addBinding().to(Doublequoted.class);
      soyDirectiveSetBinder.addBinding().to(Indent.class);
      soyDirectiveSetBinder.addBinding().to(Lowercased.class);
      soyDirectiveSetBinder.addBinding().to(Quoted.class);
      soyDirectiveSetBinder.addBinding().to(ReplaceAll.class);
      soyDirectiveSetBinder.addBinding().to(Trim.class);
      soyDirectiveSetBinder.addBinding().to(Uppercased.class);
      soyDirectiveSetBinder.addBinding().to(YamlPrimitive.class);
    }
  }

  private abstract static class BaseDirective implements SoyJavaPrintDirective {
    @Override
    public Set<Integer> getValidArgsSizes() {
      return ImmutableSet.of(0);
    }

    public boolean shouldCancelAutoescape() {
      return false;
    }
  }

  /**
   * Escapes a string to be put inside double quotes.
   *
   * <pre><code>
   * {$'"' |quoted}  // Would produce \"
   * </code></pre>
   */
  @VisibleForTesting
  @Singleton
  static class Doublequoted extends BaseDirective {
    private static final Escaper ESCAPER =
        new CharEscaperBuilder()
            .addEscape('\\', "\\\\")
            .addEscape('"', "\\\"")
            .toEscaper();

    @Override
    public String getName() {
      return "|doublequoted";
    }

    @Override
    public SoyValue applyForJava(SoyValue value, List<SoyValue> args) {
      return StringData.forValue(ESCAPER.escape(value.coerceToString()));
    }
  }

  /**
   * Add leading spaces to lines in a string. The second argument is a boolean controlling whether
   * the first line should also be indented, which defaults to false.
   * 
   * <p>Lines containing only spaces are not indented.
   */
  @VisibleForTesting
  @Singleton
  static class Indent extends BaseDirective {
    private static final Splitter SPLITTER = Splitter.on('\n');
    private static final Joiner JOINER = Joiner.on('\n');

    @Override
    public Set<Integer> getValidArgsSizes() {
      return ImmutableSet.of(1, 2);
    }

    @Override
    public String getName() {
      return "|indent";
    }

    @Override
    public SoyValue applyForJava(SoyValue value, List<SoyValue> args) {
      final String spacer = Strings.repeat(" ", args.get(0).integerValue());
      Iterable<String> pre;
      Iterable<String> toBeIndented;
      if (getIndentFirstArg(args)) {
        pre = ImmutableList.of();
        toBeIndented = SPLITTER.split(value.coerceToString());
      } else {
        List<String> lines = SPLITTER.splitToList(value.coerceToString());
        pre = Iterables.limit(lines, 1);
        toBeIndented = Iterables.skip(lines, 1);
      }
      Iterable<String> indented = Iterables.transform(toBeIndented, new Function<String, String>() {
        @Override
        public String apply(String s) {
          if (s.trim().isEmpty()) {
            // Don't indent lines with only spaces.
            return s;
          }
          return spacer + s;
        }
      });
      return StringData.forValue(JOINER.join(Iterables.concat(pre, indented)));
    }

    private boolean getIndentFirstArg(List<SoyValue> args) {
      if (args.size() > 1) {
        return args.get(1).booleanValue();
      }
      return false;
    }
  }
  
  /**
   * Converts all characters to lowercased.
   */
  @VisibleForTesting
  @Singleton
  static class Lowercased extends BaseDirective {
    @Override
    public String getName() {
      return "|lowercased";
    }

    @Override
    public SoyValue applyForJava(SoyValue value, List<SoyValue> args) {
      return StringData.forValue(value.coerceToString().toLowerCase());
    }
  }

  /**
   * Escapes a yaml value that appears inside a pair of single quotes.
   *
   * <pre><code>
   * key: {$someValueWithQuotes |quoted}
   * </code></pre>
   */
  @VisibleForTesting
  @Singleton
  static class Quoted extends BaseDirective {
    private static final Escaper ESCAPER =
        new CharEscaperBuilder().addEscape('\'', "''").toEscaper();

    @Override
    public String getName() {
      return "|quoted";
    }

    @Override
    public SoyValue applyForJava(SoyValue value, List<SoyValue> args) {
      return StringData.forValue(ESCAPER.escape(value.coerceToString()));
    }
  }

  /**
  * Replaces all occurrences of a string by another string.
  *
  * <pre><code>
  * key: {$someString |replaceAll: 'abc', 'def'}
  * </code></pre>
  */
  @VisibleForTesting
  @Singleton
  static class ReplaceAll extends BaseDirective {
    @Override
    public Set<Integer> getValidArgsSizes() {
     return ImmutableSet.of(2);
    }

    @Override
    public String getName() {
     return "|replaceAll";
    }

    @Override
    public SoyValue applyForJava(SoyValue value, List<SoyValue> args) {
     return StringData.forValue(value.coerceToString().replace(
         args.get(0).coerceToString(), args.get(1).coerceToString()));
    }
  }

  /**
   * Trims spaces and new lines at the beginning or end of a string.
   * This directive takes an argument in a form of a comma-separated list of the following flags:
   * <ul>
   *   <li>{@code leading|trailing|both}. Only up to one of these should be specified.
   *   If none these are specified, {@code both} is implied.
   *   <li>{@code spaces|newlines}. If none of these are explicitly specified, both are implied.
   * </ul>
   *
   * <pre><code>
   * {$someString |trim: 'both,spaces,newlines'}
   * </code></pre>
   */
  @VisibleForTesting
  @Singleton
  static class Trim extends BaseDirective {

    enum Flag {
      LEADING,
      TRAILING,
      BOTH,
      SPACES,
      NEWLINES;

      private static final Splitter SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();
    }

    private static final ImmutableSet<Flag> END_FLAGS =
        ImmutableSet.of(Flag.LEADING, Flag.TRAILING, Flag.BOTH);
    private static final ImmutableSet<Flag> CHAR_FLAGS =
        ImmutableSet.of(Flag.SPACES, Flag.NEWLINES);

    @Override
    public Set<Integer> getValidArgsSizes() {
      return ImmutableSet.of(0, 1);
    }

    @Override
    public String getName() {
      return "|trim";
    }

    @Override
    public SoyValue applyForJava(SoyValue soyValue, List<SoyValue> list) {
      Set<Flag> flags = getFlagsArgument(list);
      String value = soyValue.stringValue();
      int begin = 0;
      int end = value.length();
      if (flags.contains(Flag.BOTH) || flags.contains(Flag.LEADING)) {
        while (begin < end) {
          if (!shouldTrim(value.charAt(begin), flags)) {
            break;
          }
          begin++;
        }
      }
      if (flags.contains(Flag.BOTH) || flags.contains(Flag.TRAILING)) {
        while (begin < end) {
          if (!shouldTrim(value.charAt(end - 1), flags)) {
            break;
          }
          end--;
        }
      }
      return StringData.forValue(value.substring(begin, end));
    }

    private static ImmutableSet<Flag> getFlagsArgument(List<SoyValue> list) {
      if (list.isEmpty()) {
        return ImmutableSet.of(Flag.BOTH, Flag.SPACES, Flag.NEWLINES);
      }
      HashSet<Flag> flags = new HashSet<>();
      for (String v : Flag.SPLITTER.splitToList(list.get(0).stringValue())) {
        flags.add(Flag.valueOf(v.trim().toUpperCase()));
      }

      if (Sets.intersection(flags, END_FLAGS).isEmpty()) {
        flags.add(Flag.BOTH);
      }
      if (Sets.intersection(flags, CHAR_FLAGS).isEmpty()) {
        flags.addAll(CHAR_FLAGS);
      }
      return ImmutableSet.copyOf(flags);
    }

    private static boolean shouldTrim(char c, Set<Flag> flags) {
      return (flags.contains(Flag.SPACES) && c == ' ')
          || (flags.contains(Flag.NEWLINES) && c == '\n');
    }
  }

  /**
   * Converts all characters to uppercased.
   */
  @VisibleForTesting
  @Singleton
  static class Uppercased extends BaseDirective {
    @Override
    public String getName() {
      return "|uppercased";
    }

    @Override
    public SoyValue applyForJava(SoyValue value, List<SoyValue> args) {
      return StringData.forValue(value.coerceToString().toUpperCase());
    }
  }

  /**
   * Outputs a primitive as a yaml value, with proper quoting and escaping. This simplifies writing
   * yaml values.
   * <p>
   * This takes the current indent as an argument to make sure folded or literal style strings are
   * properly indented. This indent argument is explicitly required to avoid mistakes.
   * 
   * <pre><code>
   * key: {$someValue |yamlprimitive: [indent_value]}
   * </code></pre>
   */
  @VisibleForTesting
  @Singleton
  static class YamlPrimitive extends BaseDirective {

    private final Representer representer = new Representer();
    private final Resolver resolver = new Resolver();
    private final DumperOptions dumperOptions;
    
    @Inject
    YamlPrimitive() {
      dumperOptions = new DumperOptions();
      dumperOptions.setWidth(2048);  // Avoid auto-line breaking, unless it's really long.
    }
    
    @Override
    public Set<Integer> getValidArgsSizes() {
      return ImmutableSet.of(1);
    }

    @Override
    public SoyValue applyForJava(SoyValue value, List<SoyValue> args) {
      Preconditions.checkArgument(
          value instanceof PrimitiveData || value instanceof SanitizedContent,
          "|yamlprimitive directive only supports primitive types");
      Node node;
      if (value instanceof BooleanData) {
        node = representer.represent(value.booleanValue());
      } else if (value instanceof FloatData) {
        node = representer.represent(value.floatValue());
      } else if (value instanceof IntegerData) {
        node = representer.represent(value.integerValue());
      } else {
        node = representer.represent(value.coerceToString());
      }
      int indent = args.get(0).integerValue();
      StringWriter writer = new StringWriter();
      Serializer serializer = new Serializer(
          new Emitter(writer, dumperOptions), resolver, dumperOptions, null);
      try {
        serializer.open();
        serializer.serialize(node);
        serializer.close();
        return StringData.forValue(indentLines(writer.toString().trim(), indent));
      } catch (IOException e) {
        // Should not happen.
        throw new RuntimeException(e);
      }
    }

    @Override
    public String getName() {
      return "|yamlprimitive";
    }
    
    private static String indentLines(String value, int indent) {
      return value.replace("\n", "\n" + Strings.repeat(" ", indent));
    }
  }

  private SoyDirectives() {}
}
