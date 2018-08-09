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

import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.google.protobuf.util.JsonFormat;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

class InputReaderFactory {
  static InputReader getReader(AutogenSettings settings) {
    switch (settings.getInputType()) {
      case PROTOTEXT:
        return new PrototextReader(settings);
      case JSON:
        return new JsonReader(settings);
      case YAML:
        return new YamlReader(settings);
      case WIRE:
        return new WireReader(settings);
    }
    throw new IllegalArgumentException("Unknown input type: " + settings.getInputType());
  }

  abstract static class InputReader {
    private final AutogenSettings settings;

    InputReader(AutogenSettings settings) {
      this.settings = settings;
    }

    Message readInput(Message.Builder builder) throws IOException {
      final String input = this.settings.getInput();
      try (InputStream stream = input.isEmpty()
          ? System.in : new BufferedInputStream(new FileInputStream(input))) {
        doReadInput(stream, builder);
      }
      return builder.build();
    }

    abstract void doReadInput(InputStream stream, Message.Builder builder) throws IOException;
  }

  static class PrototextReader extends InputReader {
    PrototextReader(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void doReadInput(InputStream stream, Message.Builder builder) throws IOException {
      TextFormat.getParser().merge(new InputStreamReader(stream, UTF_8), builder);
    }
  }

  static class JsonReader extends InputReader {
    JsonReader(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void doReadInput(InputStream stream, Message.Builder builder) throws IOException {
      JsonFormat.parser().merge(new InputStreamReader(stream, UTF_8), builder);
    }
  }

  static class YamlReader extends InputReader {
    YamlReader(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void doReadInput(InputStream stream, Message.Builder builder) throws IOException {
      Yaml yaml = new Yaml();
      Gson gson = new Gson();
      String json = gson.toJson(yaml.loadAs(new InputStreamReader(stream, UTF_8), Map.class));
      JsonFormat.parser().merge(json, builder);
    }
  }

  static class WireReader extends InputReader {
    WireReader(AutogenSettings settings) {
      super(settings);
    }

    @Override
    void doReadInput(InputStream stream, Message.Builder builder) throws IOException {
      builder.mergeFrom(stream);
    }
  }
}
