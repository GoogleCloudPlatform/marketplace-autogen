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

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.google.protobuf.util.JsonFormat;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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
    }
    throw new IllegalArgumentException("Unknown output type: " + settings.getOutputType());
  }

  abstract static class OutputWriter {
    private final AutogenSettings settings;

    OutputWriter(AutogenSettings settings) {
      this.settings = settings;
    }

    void writeOutput(Message message) throws IOException {
      final String output = settings.getOutput();
      try (OutputStream stream = output.isEmpty()
          ? System.out : new BufferedOutputStream(new FileOutputStream(output))) {
        doWriteOutput(stream, message);
      }
    }

    abstract void doWriteOutput(OutputStream stream, Message message) throws IOException;
  }

  static class PrototextWriter extends OutputWriter {
    PrototextWriter(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void doWriteOutput(OutputStream stream, Message message) throws IOException {
      new PrintStream(stream).print(TextFormat.printToString(message));
    }
  }

  static class JsonWriter extends OutputWriter {
    JsonWriter(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void doWriteOutput(OutputStream stream, Message message) throws IOException {
      new PrintStream(stream).print(JsonFormat.printer().print(message));
    }
  }

  static class YamlWriter extends OutputWriter {
    YamlWriter(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void doWriteOutput(OutputStream stream, Message message) throws IOException {
      Yaml yaml = new Yaml();
      new PrintStream(stream).print(yaml.dump(yaml.load(JsonFormat.printer().print(message))));
    }
  }

  static class WireWriter extends OutputWriter {
    WireWriter(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void doWriteOutput(OutputStream stream, Message message) throws IOException {
      message.writeTo(stream);
    }
  }
}
