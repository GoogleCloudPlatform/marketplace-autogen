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

package com.google.cloud.deploymentmanager.autogen.cli;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** Class to encapsulate the settings used to run Autogen */
final class AutogenSettings {
  private static final String OPTION_HELP = "help";
  private static final String OPTION_SINGLE_INPUT = "single_input";
  private static final String OPTION_BATCH_INPUT = "batch_input";
  private static final String OPTION_OUTPUT = "output";
  private static final String OPTION_INPUT_TYPE = "input_type";
  private static final String OPTION_OUTPUT_TYPE = "output_type";
  private static final String OPTION_EXCLUDE_SHARED_SUPPORT_FILES = "exclude_shared_support_files";

  private static final String HELP_DESC = "Prints usage help";
  private static final String SINGLE_INPUT_DESC = "Input source, a filename or empty for stdin,"
      + " for single Solution processing. See DeploymentPackageInput proto";
  private static final String BATCH_INPUT_DESC = "Input source, a filename or empty for stdin,"
      + " for batch Solution processing. See BatchInput proto";
  private static final String OUTPUT_DESC = "Output destination folder if output_type is PACKAGE,"
      + " or filename otherwise (Optional, current directory will be used for output_type PACKAGE"
      + " and stdout for other types, if option not present)";
  private static final String INPUT_TYPE_DESC = "Input content type";
  private static final String OUTPUT_TYPE_DESC = "Output content type";
  private static final String EXCLUDE_SHARED_SUPPORT_FILES_DESC =
      "Whether to exclude symlinkable shared support files";

  private boolean singleMode;
  private String input;
  private String output;
  private InputType inputType;
  private OutputType outputType;
  private boolean excludeSharedSupportFiles;

  enum InputType {
    PROTOTEXT,
    JSON,
    YAML,
    WIRE
  }

  enum OutputType {
    PROTOTEXT,
    JSON,
    YAML,
    WIRE,
    PACKAGE
  }

  private static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("Autogen", options);
  }

  private static Option getOptionalArgOption(String opt, String desc) {
    return Option.builder().longOpt(opt).hasArg(true).desc(desc).optionalArg(true).build();
  }

  private static Options buildCommandOptions() {
    Options options = new Options();
    return options
        .addOption(null, OPTION_HELP, false, HELP_DESC)
        .addOption(getOptionalArgOption(OPTION_SINGLE_INPUT, SINGLE_INPUT_DESC))
        .addOption(getOptionalArgOption(OPTION_BATCH_INPUT, BATCH_INPUT_DESC))
        .addOption(null, OPTION_OUTPUT, true, OUTPUT_DESC)
        .addOption(null, OPTION_INPUT_TYPE, true, INPUT_TYPE_DESC)
        .addOption(null, OPTION_OUTPUT_TYPE, true, OUTPUT_TYPE_DESC)
        .addOption(
            null, OPTION_EXCLUDE_SHARED_SUPPORT_FILES, false, EXCLUDE_SHARED_SUPPORT_FILES_DESC);
  }

  private static void validateCliOptions(CommandLine cmd) {
    List<String> errors = new ArrayList<>();
    if (cmd.hasOption(OPTION_SINGLE_INPUT) && cmd.hasOption(OPTION_BATCH_INPUT)) {
      errors.add("--single_input and --batch_input can't be used at the same time");
    } else if (!cmd.hasOption(OPTION_SINGLE_INPUT) && !cmd.hasOption(OPTION_BATCH_INPUT)) {
      errors.add("at least one of --single_input or --batch_input must be specified");
    }

    if (!errors.isEmpty()) {
      System.out.println("Error!");
      for (String error : errors) {
        System.out.println(error);
      }
      System.exit(1);
    }
  }

  static AutogenSettings build(String[] args) throws ParseException {
    Options options = buildCommandOptions();
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption(OPTION_HELP)) {
      printUsage(options);
      System.exit(0);
    }

    validateCliOptions(cmd);

    AutogenSettings settings = new AutogenSettings();

    if (cmd.hasOption(OPTION_SINGLE_INPUT)) {
      settings.input = cmd.getOptionValue(OPTION_SINGLE_INPUT, settings.input);
      settings.singleMode = true;
    }
    if (cmd.hasOption(OPTION_BATCH_INPUT)) {
      settings.input = cmd.getOptionValue(OPTION_BATCH_INPUT, settings.input);
      settings.singleMode = false;
    }
    settings.output = cmd.getOptionValue(OPTION_OUTPUT, settings.output);
    settings.inputType =
        InputType.valueOf(cmd.getOptionValue(OPTION_INPUT_TYPE, settings.inputType.name()));
    settings.outputType =
        OutputType.valueOf(cmd.getOptionValue(OPTION_OUTPUT_TYPE, settings.outputType.name()));
    settings.excludeSharedSupportFiles = cmd.hasOption(OPTION_EXCLUDE_SHARED_SUPPORT_FILES);

    return settings;
  }

  private AutogenSettings() {
    this.singleMode = true;
    this.input = "";
    this.output = "";
    this.inputType = InputType.PROTOTEXT;
    this.outputType = OutputType.PROTOTEXT;
    this.excludeSharedSupportFiles = false;
  }

  public String getInput() {
    return this.input;
  }

  public String getOutput() {
    return this.output;
  }

  public InputType getInputType() {
    return this.inputType;
  }

  public OutputType getOutputType() {
    return this.outputType;
  }

  public boolean shouldExcludeSharedSupportFiles() {
    return this.excludeSharedSupportFiles;
  }

  public boolean isSingleMode() {
    return this.singleMode;
  }
}
