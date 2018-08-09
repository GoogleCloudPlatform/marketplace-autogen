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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.cloud.deploymentmanager.autogen.Autogen;
import com.google.cloud.deploymentmanager.autogen.Autogen.SharedSupportFilesStrategy;
import com.google.cloud.deploymentmanager.autogen.proto.BatchInput;
import com.google.cloud.deploymentmanager.autogen.proto.BatchOutput;
import com.google.cloud.deploymentmanager.autogen.proto.DeploymentPackageInput;
import com.google.cloud.deploymentmanager.autogen.proto.SolutionPackage;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.google.protobuf.util.JsonFormat;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.yaml.snakeyaml.Yaml;

/**
 * Class to host the common operations for running Autogen on BatchInput and DeploymentPackageInput
 * specs
 */
public final class AutogenCli {
  static final String OPTION_NAME_HELP = "help";
  static final String OPTION_NAME_SINGLE_INPUT = "single_input";
  static final String OPTION_NAME_BATCH_INPUT = "batch_input";
  static final String OPTION_NAME_OUTPUT = "output";
  static final String OPTION_NAME_INPUT_TYPE = "input_type";
  static final String OPTION_NAME_OUTPUT_TYPE = "output_type";
  static final String OPTION_NAME_INCLUDE_SHARED_SUPPORT_FILES = "include_shared_support_files";

  enum ContentType {
    PROTOTEXT,
    JSON,
    YAML,
    WIRE;

    public void write(Message message, OutputStream stream) throws IOException {
      switch (this) {
        case PROTOTEXT:
          new PrintStream(stream).print(TextFormat.printToString(message));
          break;
        case JSON:
          new PrintStream(stream).print(getJsonFromMessage(message));
          break;
        case YAML:
          Yaml yaml = new Yaml();
          new PrintStream(stream).print(yaml.dump(yaml.load(getJsonFromMessage(message))));
          break;
        case WIRE:
          message.writeTo(stream);
          break;
      }
    }

    public void read(Message.Builder builder, InputStream stream) throws IOException {
      switch (this) {
        case PROTOTEXT:
          TextFormat.getParser().merge(new InputStreamReader(stream, UTF_8), builder);
          break;
        case JSON:
          JsonFormat.parser().merge(new InputStreamReader(stream, UTF_8), builder);
          break;
        case YAML:
          Yaml yaml = new Yaml();
          Gson gson = new Gson();
          String json = gson.toJson(yaml.loadAs(new InputStreamReader(stream, UTF_8), Map.class));
          JsonFormat.parser().merge(json, builder);
          break;
        case WIRE:
          builder.mergeFrom(stream);
          break;
      }
    }
  }

  static String getJsonFromMessage(Message message) {
    try {
      return JsonFormat.printer().print(message);
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalArgumentException("Cannot convert to JSON", e);
    }
  }

  static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("Autogen", options);
  }

  static Options buildCommandOptions() {
    Options options = new Options();
    options.addOption(null, OPTION_NAME_HELP, false, "Prints usage help");

    Option.Builder singleInputBuilder = Option.builder()
        .longOpt(OPTION_NAME_SINGLE_INPUT)
        .hasArg(true)
        .desc("Input source, a filename or empty for stdin, for single Solution processing. See"
            + " DeploymentPackageInput proto")
        .optionalArg(true);
    options.addOption(singleInputBuilder.build());

    Option.Builder batchInputBuilder = Option.builder()
        .longOpt(OPTION_NAME_BATCH_INPUT)
        .hasArg(true)
        .desc("Input source, a filename or empty for stdin, for batch Solution processing. See"
            + " BatchInput proto")
        .optionalArg(true);
    options.addOption(batchInputBuilder.build());

    options.addOption(
        null, OPTION_NAME_OUTPUT, true, "Output destination, a filename or empty for stdout");
    options.addOption(null, OPTION_NAME_INPUT_TYPE, true, "Content type of the input");
    options.addOption(null, OPTION_NAME_OUTPUT_TYPE, true, "Content type of the output");
    options.addOption(
        null,
        OPTION_NAME_INCLUDE_SHARED_SUPPORT_FILES,
        true,
        "Whether to include symlinkable shared support files");
    return options;
  }

  static Message readInput(String input, ContentType inputType, Message.Builder builder)
      throws IOException {
    if (input.isEmpty()) {
      inputType.read(builder, System.in);
    } else {
      try (InputStream fi = new BufferedInputStream(new FileInputStream(input))) {
        inputType.read(builder, fi);
      }
    }
    return builder.build();
  }

  static void writeOutput(Message data, String output, ContentType outputType) throws IOException {
    if (output.isEmpty()) {
      outputType.write(data, System.out);
    } else {
      try (OutputStream fo = new BufferedOutputStream(new FileOutputStream(output))) {
        outputType.write(data, fo);
      }
    }
  }

  static SolutionPackage getSolutionPackage(
      DeploymentPackageInput solution, boolean includeSharedSupportFiles) {
    SharedSupportFilesStrategy strategy =
        includeSharedSupportFiles
            ? SharedSupportFilesStrategy.INCLUDED
            : SharedSupportFilesStrategy.EXCLUDED;
    return Autogen.getInstance().generateDeploymentPackage(solution, strategy);
  }

  static void validateCliOptions(CommandLine cmd) {
    List<String> errors = new ArrayList<>();
    if (cmd.hasOption(OPTION_NAME_SINGLE_INPUT) && cmd.hasOption(OPTION_NAME_BATCH_INPUT)) {
      errors.add("--single_input and --batch_input can't be used at the same time");
    } else if (!cmd.hasOption(OPTION_NAME_SINGLE_INPUT)
        && !cmd.hasOption(OPTION_NAME_BATCH_INPUT)) {
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

  static void runAutogen(String[] args) throws IOException, ParseException {
    Options options = buildCommandOptions();
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption(OPTION_NAME_HELP)) {
      printUsage(options);
      return;
    }

    validateCliOptions(cmd);

    boolean singleMode = true;
    String input = "";
    String output = "";
    ContentType inputType = ContentType.PROTOTEXT;
    ContentType outputType = ContentType.PROTOTEXT;
    boolean includeSharedSupportFiles = false;

    if (cmd.hasOption(OPTION_NAME_SINGLE_INPUT)) {
      input = cmd.getOptionValue(OPTION_NAME_SINGLE_INPUT, input);
      singleMode = true;
    }
    if (cmd.hasOption(OPTION_NAME_BATCH_INPUT)) {
      input = cmd.getOptionValue(OPTION_NAME_BATCH_INPUT, input);
      singleMode = false;
    }
    if (cmd.hasOption(OPTION_NAME_OUTPUT)) {
      output = cmd.getOptionValue(OPTION_NAME_OUTPUT);
    }
    if (cmd.hasOption(OPTION_NAME_INPUT_TYPE)) {
      inputType = ContentType.valueOf(cmd.getOptionValue(OPTION_NAME_INPUT_TYPE));
    }
    if (cmd.hasOption(OPTION_NAME_OUTPUT_TYPE)) {
      outputType = ContentType.valueOf(cmd.getOptionValue(OPTION_NAME_OUTPUT_TYPE));
    }
    if (cmd.hasOption(OPTION_NAME_INCLUDE_SHARED_SUPPORT_FILES)) {
      String value = cmd.getOptionValue(OPTION_NAME_INCLUDE_SHARED_SUPPORT_FILES);
      includeSharedSupportFiles = value.isEmpty() || Boolean.parseBoolean(value);
    }

    Message.Builder inputBuilder =
        singleMode ? DeploymentPackageInput.newBuilder() : BatchInput.newBuilder();
    Message inputMessage = readInput(input, inputType, inputBuilder);
    if (singleMode) {
      DeploymentPackageInput message = (DeploymentPackageInput) inputMessage;
      writeOutput(getSolutionPackage(message, includeSharedSupportFiles), output, outputType);
    } else {
      BatchInput message = (BatchInput) inputMessage;
      BatchOutput.Builder builder = BatchOutput.newBuilder();
      for (DeploymentPackageInput solution : message.getSolutionsList()) {
        SolutionPackage dp = getSolutionPackage(solution, includeSharedSupportFiles);
        builder
            .addSolutionsBuilder()
            .setPartnerId(solution.getPartnerId())
            .setSolutionId(solution.getSolutionId())
            .setPackage(dp);
      }
      writeOutput(builder.build(), output, outputType);
    }
  }

  public static void main(String[] args) throws IOException, ParseException {
    AutogenCli.runAutogen(args);
  }
}
