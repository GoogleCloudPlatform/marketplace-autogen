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
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Preprocesses a soy template content by inserting special characters to preserve line breaks and
 * indentations, as well as auto-escaping special Jinja delimiters. This helps preserving YAML
 * structure, as well as easing the crafting of Jinja templates.
 * 
 * <p>The preprocessor strips off all comments.
 *
 * <p>The following Jinja delimiters are automatically escaped: <code>{%</code>, <code>%}</code>,
 * <code>{{</code>, and <code>}}</code>.
 *
 * <p>"Left as-is" means that the line is unmodified and will follow the soy's line joining.
 * Otherwise, a <code>{nil}</code> is inserted at the beginning of the line, and a
 * <code>{\n}</code> is inserted at the end of the line. A line is left as-is if:
 * <ul>
 * <li>It is empty or contains only spaces
 * <li>It has only soy commands other than {@code print}
 * <li>It contains any special character in the following list: <code>{sp}</code>,
 * <code>{nil}</code>, <code>{\r}</code>, <code>{\n}</code>, and <code>{\t}</code>
 * </ul>
 */
final class Preprocessor {

  // Soy double slash single line comment only counts if it's preceded by an empty space.
  // This is because soy wants to support "http://" without the needs for escaping.
  // Also counts leading spaces as part of the comment.
  private static final Pattern SOY_SINGLE_LINE_COMMENT_REGEX = Pattern.compile(
      "(?:^| +)//.*?$", Pattern.MULTILINE);
  
  // Also counts leading spaces as part of the comment.
  private static final Pattern SOY_BLOCK_COMMENT_REGEX = Pattern.compile(
      "(?: *)/[*].*?[*]/", Pattern.MULTILINE | Pattern.DOTALL);

  // If this regex is found, don't insert line breaks or preserve leading spaces.
  @VisibleForTesting
  static final Pattern ESCAPE_REGEX = Pattern.compile(
      "\\{((sp)|(nil)|(\\\\r)|(\\\\n)|(\\\\t))\\}");

  // If this regex is found, preserve the leading spaces.
  private static final Pattern PRESERVE_LEADING_REGEX = Pattern.compile("\\{plsp\\}");

  // Substrings found by this regex should be removed as they serve only as directives.
  // For now we have only one directive.
  private static final Pattern DIRECTIVE_REGEX = PRESERVE_LEADING_REGEX;
  
  // This regex should be used as an inner regex to capture the content inside a soy command,
  // which is single-quote-sensitive. It specifically consumes all non-closed-curly-brace
  // characters, except the ones inside a pair of single quotes.
  private static final String SOY_COMMAND_CONTENT = "(?:(?:[^}']+)|(?:'(?:[^']|\\\\')*'))+";
  
  // Captures a single-lined soy command. This does not include the short version of the print
  // command (something like "{$variable}") and special characters.
  // This consumes leading and trailing spaces.
  @VisibleForTesting
  static final Pattern SOY_COMMAND_REGEX = Pattern.compile(
      String.format("\\s*(\\{[/@]?([a-z?]+)(\\s+%s)?\\})\\s*", SOY_COMMAND_CONTENT));
  @VisibleForTesting
  static final Pattern SOY_COMMAND_BLACKLIST_REGEX = Pattern.compile(
      "\\{((sp)|(nil)|(\\\\r)|(\\\\n)|(\\\\t)|(lb)|(rb)|(print\\s+.*))\\}");

  private static final Pattern JINJA_STATEMENT_BEGIN_REGEX = Pattern.compile("\\{%(-)?");
  private static final Pattern JINJA_STATEMENT_END_REGEX = Pattern.compile("(-)?%\\}");
  private static final Pattern JINJA_EXPRESSION_BEGIN_REGEX = Pattern.compile("\\{\\{(-)?");
  private static final Pattern JINJA_EXPRESSION_END_REGEX = Pattern.compile("(-)?\\}\\}");
  private static final Pattern JINJA_COMMENT_BEGIN_REGEX = Pattern.compile("\\{#(-)?");
  private static final Pattern JINJA_COMMENT_END_REGEX = Pattern.compile("(-)?#\\}");
  private static final ImmutableMap<String, Pattern> JINJA_DELIMITER_REPLACEMENTS =
      ImmutableMap.<String, Pattern>builder()
          .put("{lb}%$1", JINJA_STATEMENT_BEGIN_REGEX)
          .put("$1%{rb}", JINJA_STATEMENT_END_REGEX)
          .put("{lb}{lb}$1", JINJA_EXPRESSION_BEGIN_REGEX)
          .put("$1{rb}{rb}", JINJA_EXPRESSION_END_REGEX)
          .put("{lb}#$1", JINJA_COMMENT_BEGIN_REGEX)
          .put("$1#{rb}", JINJA_COMMENT_END_REGEX)
          .build();

  private static final Predicate<String> IS_EMPTY_OR_HAS_ONLY_SPACES =
      new Predicate<String>() {
        @Override
        public boolean apply(String trimmedLine) {
          return trimmedLine.isEmpty();
        }
      };

  // Predicates that a trimmed line has only soy commands other than print.
  @VisibleForTesting
  static final Predicate<String> HAS_ONLY_SOY_COMMANDS =
      new Predicate<String>() {
        @Override
        public boolean apply(String trimmedLine) {
          // Removes all non-print commands. If the result is an empty string, then return true.
          Matcher m = SOY_COMMAND_REGEX.matcher(trimmedLine);
          StringBuffer sb = new StringBuffer();
          while (m.find()) {
            if (!SOY_COMMAND_BLACKLIST_REGEX.matcher(m.group(1)).matches()) {
              m.appendReplacement(sb, "");
            } else {
              m.appendReplacement(sb, Matcher.quoteReplacement(m.group()));
            }
            if (sb.length() > 0) {
              return false;
            }
          }
          m.appendTail(sb);
          return sb.length() == 0;
        }
      };

  // Predicates that a trimmed line has at least one special character that prevents automatic
  // insertion of line breaks.
  @VisibleForTesting
  static final Predicate<String> HAS_ESCAPES =
      new Predicate<String>() {
        @Override
        public boolean apply(String trimmedLine) {
          return ESCAPE_REGEX.matcher(trimmedLine).find();
        }
      };

  // Predicates that a trimmed line has a special directive to preserve leading spaces.
  static final Predicate<String> SHOULD_PRESERVE_LEADING =
      new Predicate<String>() {
        @Override
        public boolean apply(String trimmedLine) {
          return PRESERVE_LEADING_REGEX.matcher(trimmedLine).find();
        }
      };

  // Predicates that a trimmed line should not have line breaks inserted.
  private static final Predicate<String> SHOULD_SKIP_LINE_BREAKS = Predicates.or(ImmutableList.of(
      IS_EMPTY_OR_HAS_ONLY_SPACES, HAS_ONLY_SOY_COMMANDS, HAS_ESCAPES, SHOULD_PRESERVE_LEADING));

  private static final Joiner LINE_JOINER = Joiner.on('\n');

  // Captures all soy commands, including multi-lined ones and short versions of print command.
  // This regex is sensitive to jinja delimiters. It's crafted to especially avoid matching 
  // something like "{{ abc }}", since such construct could have been potentially recognized as a
  // "{ abc }" soy command, with leading { and trailing }. Note that a Jinja delimiter is always
  // separated by at least one space, hence the space requirement in the lookbehind expression.
  @VisibleForTesting
  static final Pattern SOY_COMMAND_TO_COLLAPSE_REGEX =
      Pattern.compile(
          String.format("(?<![{])\\{(?![%%#{]-?\\s)%s\\}", SOY_COMMAND_CONTENT), Pattern.MULTILINE);
  private static final Pattern LINE_COLLAPSING_REGEX =
      Pattern.compile("\\s*\\n\\s*", Pattern.MULTILINE);

  /** Preprocesses the content of a template file, returning the modified content. */
  public static String preprocess(String content) {
    content = stripComments(content);
    content = collapseMultilineCommandsAndEscapeJinjaDelimiters(content);
    try (Scanner scanner = new Scanner(content)) {
      List<String> lines = new ArrayList<>();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String trimmed = line.trim();
        
        if (!SHOULD_SKIP_LINE_BREAKS.apply(trimmed)) {
          line = "{nil}" + line + "{\\n}";
        } else if (SHOULD_PRESERVE_LEADING.apply(trimmed)) {
          line = "{nil}" + line;
        }
        line = removeDirectives(line);
        lines.add(line);
      }
      return LINE_JOINER.join(lines);
    }
  }
  
  @VisibleForTesting
  static String stripComments(String content) {
    content = SOY_SINGLE_LINE_COMMENT_REGEX.matcher(content).replaceAll("");
    content = SOY_BLOCK_COMMENT_REGEX.matcher(content).replaceAll("");
    return content;
  }
  
  // Collapsing multiline commands is necessary so that we don't insert line breaks on command
  // continuation lines.
  @VisibleForTesting
  static String collapseMultilineCommandsAndEscapeJinjaDelimiters(String content) {
    StringBuilder sb = new StringBuilder();
    Iterable<String> nonSoyCommands = Splitter.on(SOY_COMMAND_TO_COLLAPSE_REGEX).split(content);
    Matcher m = SOY_COMMAND_TO_COLLAPSE_REGEX.matcher(content);
    for (String nonSoyCommand : nonSoyCommands) {
      sb.append(doEscapeJinjaDelimiters(nonSoyCommand));
      if (m.find()) {
        sb.append(LINE_COLLAPSING_REGEX.matcher(m.group()).replaceAll(" "));
      }
    }
    return sb.toString();
  }

  @VisibleForTesting
  static String removeDirectives(String line) {
    return DIRECTIVE_REGEX.matcher(line).replaceAll("");
  }
  
  private static String doEscapeJinjaDelimiters(String toEscape) {
    for (Map.Entry<String, Pattern> entry : JINJA_DELIMITER_REPLACEMENTS.entrySet()) {
      toEscape = entry.getValue().matcher(toEscape).replaceAll(entry.getKey());
    }
    return toEscape;
  }

  private Preprocessor() {}
}
