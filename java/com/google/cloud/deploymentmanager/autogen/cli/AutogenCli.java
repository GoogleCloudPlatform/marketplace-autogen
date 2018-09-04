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

import com.google.cloud.deploymentmanager.autogen.Autogen;
import com.google.cloud.deploymentmanager.autogen.Autogen.SharedSupportFilesStrategy;
import com.google.cloud.deploymentmanager.autogen.proto.BatchInput;
import com.google.cloud.deploymentmanager.autogen.proto.BatchOutput;
import com.google.cloud.deploymentmanager.autogen.proto.DeploymentPackageInput;
import com.google.cloud.deploymentmanager.autogen.proto.SolutionPackage;
import com.google.protobuf.Message;
import java.io.IOException;
import org.apache.commons.cli.ParseException;

/**
 * Class to host the common operations for running Autogen on BatchInput and DeploymentPackageInput
 * specs
 */
public final class AutogenCli {
  private static SolutionPackage getSolutionPackage(
      DeploymentPackageInput solution, AutogenSettings settings) {
    SharedSupportFilesStrategy strategy = settings.shouldExcludeSharedSupportFiles()
            ? SharedSupportFilesStrategy.EXCLUDED : SharedSupportFilesStrategy.INCLUDED;
    return Autogen.getInstance().generateDeploymentPackage(solution, strategy);
  }

  static void runAutogen(String[] args) throws IOException, ParseException {
    AutogenSettings settings = AutogenSettings.build(args);

    Message.Builder inputBuilder =
        settings.isSingleMode() ? DeploymentPackageInput.newBuilder() : BatchInput.newBuilder();
    Message inputMessage = InputReaderFactory.getReader(settings).readInput(inputBuilder);

    OutputWriterFactory.OutputWriter writer = OutputWriterFactory.getWriter(settings);

    if (settings.isSingleMode()) {
      writer.writeOutput(getSolutionPackage((DeploymentPackageInput) inputMessage, settings));
    } else {
      BatchInput message = (BatchInput) inputMessage;
      BatchOutput.Builder builder = BatchOutput.newBuilder();
      for (DeploymentPackageInput solution : message.getSolutionsList()) {
        SolutionPackage dp = getSolutionPackage(solution, settings);
        builder
            .addSolutionsBuilder()
            .setPartnerId(solution.getPartnerId())
            .setSolutionId(solution.getSolutionId())
            .setPackage(dp);
      }
      writer.writeOutput(builder.build());
    }
  }

  public static void main(String[] args) throws IOException, ParseException {
    AutogenCli.runAutogen(args);
  }
}
