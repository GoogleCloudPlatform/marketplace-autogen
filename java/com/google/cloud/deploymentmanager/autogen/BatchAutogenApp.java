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

package com.google.cloud.deploymentmanager.autogen;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.cloud.deploymentmanager.autogen.Autogen.SharedSupportFilesStrategy;
import com.google.cloud.deploymentmanager.autogen.proto.BatchInput;
import com.google.cloud.deploymentmanager.autogen.proto.BatchOutput;
import com.google.cloud.deploymentmanager.autogen.proto.DeploymentPackageInput;
import com.google.cloud.deploymentmanager.autogen.proto.SolutionPackage;
import com.google.errorprone.annotations.Immutable;
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
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.yaml.snakeyaml.Yaml;

/**
 * An app that autogens a batch of solutions.
 */
public final class BatchAutogenApp {

  private static final String OPTION_NAME_HELP = "help";
  private static final String OPTION_NAME_MODE = "mode";
  private static final String OPTION_NAME_INPUT = "input";
  private static final String OPTION_NAME_OUTPUT = "output";
  private static final String OPTION_NAME_INPUT_TYPE = "input_type";
  private static final String OPTION_NAME_OUTPUT_TYPE = "output_type";
  private static final String OPTION_NAME_INCLUDE_SHARED_SUPPORT_FILES =
      "include_shared_support_files";

  private static boolean printUsage = false;
  private static AutogenMode mode = AutogenMode.MULTIPLE;
  private static String input = "";
  private static String output = "";
  private static ContentType inputType = ContentType.PROTOTEXT;
  private static ContentType outputType = ContentType.PROTOTEXT;
  private static boolean includeSharedSupportFiles = false;


  private enum AutogenMode {
    MULTIPLE(new MultipleSolutionsReader()),
    SINGLE(new SingleSolutionReader());

    private final SolutionReader reader;

    AutogenMode(SolutionReader reader) {
      this.reader = reader;
    }

    public BatchInput readInput() throws IOException {
      return reader.readInput();
    }
  }


  private enum ContentType {
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

  private static String getJsonFromMessage(Message message) {
    try {
      return JsonFormat.printer().print(message);
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalArgumentException("Cannot convert to JSON", e);
    }
  }

  private static Options buildCommandOptions() {
    Options options = new Options();
    options.addOption(null, OPTION_NAME_HELP, false, "Prints usage help");
    options.addOption(null, OPTION_NAME_MODE,
        true, "Mode for input conversion - single or multiple solutions");
    options.addOption(null, OPTION_NAME_INPUT,
        true, "Input source, a filename or empty for stdin");
    options.addOption(null, OPTION_NAME_OUTPUT,
        true, "Output destination, a filename or empty for stdout");
    options.addOption(null, OPTION_NAME_INPUT_TYPE,
        true, "Content type of the input");
    options.addOption(null, OPTION_NAME_OUTPUT_TYPE,
        true, "Content type of the output");
    options.addOption(null, OPTION_NAME_INCLUDE_SHARED_SUPPORT_FILES,
        true, "Whether to include symlinkable shared support files");
    return options;
  }

  private static Options parseCommandOptions(String[] args) throws ParseException {
    Options options = buildCommandOptions();
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);
    if (cmd.hasOption(OPTION_NAME_HELP)) {
      printUsage = true;
    }
    if (cmd.hasOption(OPTION_NAME_MODE)) {
      mode = AutogenMode.valueOf(cmd.getOptionValue(OPTION_NAME_MODE));
    }
    if (cmd.hasOption(OPTION_NAME_INPUT)) {
      input = cmd.getOptionValue(OPTION_NAME_INPUT);
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
    return options;
  }

  public static void main(String[] args) throws IOException, ParseException {
    Options options = parseCommandOptions(args);
    if (printUsage) {
      printUsage(options);
      return;
    }

    BatchInput input = mode.readInput();
    BatchOutput.Builder builder = BatchOutput.newBuilder();
    for (DeploymentPackageInput solution : input.getSolutionsList()) {
      SolutionPackage dp =
          Autogen.getInstance()
              .generateDeploymentPackage(
                  solution,
                  includeSharedSupportFiles
                      ? SharedSupportFilesStrategy.INCLUDED
                      : SharedSupportFilesStrategy.EXCLUDED);
      builder
          .addSolutionsBuilder()
          .setPartnerId(solution.getPartnerId())
          .setSolutionId(solution.getSolutionId())
          .setPackage(dp);
    }
    writeOutput(builder.build());
  }

  private static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("BatchAutogenApp", options);
  }

  private static void writeOutput(BatchOutput data) throws IOException {
    if (output.isEmpty()) {
      outputType.write(data, System.out);
    } else {
      try (OutputStream fo = new BufferedOutputStream(new FileOutputStream(output))) {
        outputType.write(data, fo);
      }
    }
  }

  @Immutable
  private abstract static class SolutionReader {

    public abstract BatchInput readInput() throws IOException;

     void loadInputToMessageBuilder(Message.Builder builder) throws IOException {
      if (input.isEmpty()) {
        inputType.read(builder, System.in);
      } else {
        try (InputStream fi = new BufferedInputStream(new FileInputStream(input))) {
          inputType.read(builder, fi);
        }
      }
    }
  }

  @Immutable
  private static class SingleSolutionReader extends SolutionReader {
    @Override
    public BatchInput readInput() throws IOException {
      DeploymentPackageInput.Builder builder = DeploymentPackageInput.newBuilder();
      loadInputToMessageBuilder(builder);
      return BatchInput.newBuilder()
          .addSolutions(builder.build())
          .build();
    }
  }

  @Immutable
  private static class MultipleSolutionsReader extends SolutionReader {
    @Override
    public BatchInput readInput() throws IOException {
      BatchInput.Builder builder = BatchInput.newBuilder();
      loadInputToMessageBuilder(builder);
      return builder.build();
    }
  }

}
