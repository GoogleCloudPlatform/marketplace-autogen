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

import com.google.cloud.deploymentmanager.autogen.proto.BatchOutput;
import com.google.cloud.deploymentmanager.autogen.proto.SolutionPackage;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.google.protobuf.util.JsonFormat;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

class OutputWriterFactory {
  static OutputWriter getWriter(AutogenSettings settings) {
    switch (settings.getOutputType()) {
      case PROTOTEXT:
        return new PrototextWriter(settings);
      case JSON:
        return new JsonWriter(settings);
      case YAML:
        return new YamlWriter(settings);
      case WIRE:
        return new WireWriter(settings);
      case PACKAGE:
        return new PackageWriter(settings);
    }
    throw new IllegalArgumentException("Unknown output type: " + settings.getOutputType());
  }

  abstract static class OutputWriter {
    protected final AutogenSettings settings;

    OutputWriter(AutogenSettings settings) {
      this.settings = settings;
    }

    OutputStream newOutputStream() throws FileNotFoundException {
      final String output = settings.getOutput();
      return output.isEmpty() ? System.out : new BufferedOutputStream(new FileOutputStream(output));
    }

    abstract void writeOutput(Message message) throws IOException;
  }

  static class PrototextWriter extends OutputWriter {
    PrototextWriter(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void writeOutput(Message message) throws IOException {
      try (OutputStream stream = newOutputStream()) {
        new PrintStream(stream).print(TextFormat.printer().printToString(message));
      }
    }
  }

  static class JsonWriter extends OutputWriter {
    JsonWriter(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void writeOutput(Message message) throws IOException {
      try (OutputStream stream = newOutputStream()) {
        new PrintStream(stream).print(JsonFormat.printer().print(message));
      }
    }
  }

  static class YamlWriter extends OutputWriter {
    YamlWriter(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void writeOutput(Message message) throws IOException {
      try (OutputStream stream = newOutputStream()) {
        Yaml yaml = new Yaml();
        new PrintStream(stream).print(yaml.dump(yaml.load(JsonFormat.printer().print(message))));
      }
    }
  }

  static class WireWriter extends OutputWriter {
    WireWriter(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void writeOutput(Message message) throws IOException {
      try (OutputStream stream = newOutputStream()) {
        message.writeTo(stream);
      }
    }
  }

  static class PackageWriter extends OutputWriter {
    PackageWriter(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void writeOutput(Message message) throws IOException {
      Map<String, SolutionPackage> solutions;
      if (message instanceof BatchOutput) {
        final BatchOutput batchMessage = (BatchOutput) message;
        ImmutableMap.Builder<String, SolutionPackage> builder = ImmutableMap.builder();
        for (BatchOutput.SolutionOutput solution : batchMessage.getSolutionsList()) {
          builder.put(solution.getPartnerId() + "/" + solution.getSolutionId(),
              solution.getPackage());
        }
        solutions = builder.build();
      } else {
        solutions = ImmutableMap.of("", (SolutionPackage) message);
      }

      for (Map.Entry<String, SolutionPackage> entry : solutions.entrySet()) {
        for (SolutionPackage.File file : entry.getValue().getFilesList()) {
          String filePath =
              Paths.get(this.settings.getOutput(), entry.getKey(), file.getPath()).toString();
          File outputFile = new File(filePath);
          Files.createParentDirs(outputFile);
          if (filePath.endsWith(".png") || filePath.endsWith(".jpg")) {
            Files.write(BaseEncoding.base64().decode(file.getContent()), outputFile);
          } else {
            Files.asCharSink(outputFile, UTF_8).write(file.getContent());
          }
        }
      }
    }
  }
}
