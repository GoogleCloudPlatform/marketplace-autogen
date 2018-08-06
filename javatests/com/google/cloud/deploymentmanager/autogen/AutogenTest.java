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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.deploymentmanager.autogen.Autogen.SharedSupportFilesStrategy;
import com.google.cloud.deploymentmanager.autogen.proto.DeploymentPackageInput;
import com.google.cloud.deploymentmanager.autogen.proto.SolutionPackage;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests actual generation of full deployment packages. */
@RunWith(JUnit4.class)
public class AutogenTest {

  private static final String RELATIVE_TESTDATA_PATH =
      "javatests/com/google/cloud/deploymentmanager/autogen/testdata/";

  private static final String TESTDATA_PATH =
      System.getenv("JAVA_RUNFILES")
          + "/__main__/"
          + RELATIVE_TESTDATA_PATH;

  private static class Solution {
    private final String name;
    private final File solutionFolder;
    private final File goldenFolder;
    private final Supplier<SolutionPackage> solutionPackage;

    /** Finds all solutions under testdata folder. */
    static Iterable<Solution> findAll() {
      File root = new File(TESTDATA_PATH);
      return FluentIterable.from(Files.fileTraverser().depthFirstPreOrder(root))
          .filter(Files.isFile())
          .filter(
              new Predicate<File>() {
                @Override
                public boolean apply(File f) {
                  return "input.prototext".equals(f.getName());
                }
              })
          .transform(
              new Function<File, File>() {
                @Override
                public File apply(File f) {
                  return f.getParentFile();
                }
              })
          .transform(relativePathFunction(root))
          .transform(
              new Function<String, Solution>() {
                @Override
                public Solution apply(String name) {
                  return new Solution(name);
                }
              })
          .toList();
    }

    Solution(String folder) {
      name = folder;
      solutionFolder = new File(TESTDATA_PATH + folder);
      goldenFolder = new File(solutionFolder, "golden");
      solutionPackage =
          Suppliers.memoize(
              new Supplier<SolutionPackage>() {
                @Override
                public SolutionPackage get() {
                  try {
                    DeploymentPackageInput.Builder input = DeploymentPackageInput.newBuilder();
                    TextFormat.getParser()
                        .merge(
                            Files.asCharSource(
                                    new File(solutionFolder, "input.prototext"),
                                    StandardCharsets.UTF_8)
                                .read(),
                            input);
                    return Autogen.getInstance()
                        .generateDeploymentPackage(
                            input.build(), SharedSupportFilesStrategy.INCLUDED);
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                }
              });
    }
  }

  @Test
  public void goldenVerifyAll() throws Exception {
    File root = new File(TESTDATA_PATH);
    File tempDir = Files.createTempDir();
    for (Solution solution : Solution.findAll()) {
      // Writes the generated files to a temporary folder for diff commands.
      File solutionDir = new File(tempDir, solution.name);
      for (SolutionPackage.File f : solution.solutionPackage.get().getFilesList()) {
        File file = new File(solutionDir, f.getPath());
        Files.createParentDirs(file);
        if (file.getPath().endsWith(".png") || file.getPath().endsWith(".jpg")) {
          Files.write(BaseEncoding.base64().decode(f.getContent()), file);
        } else {
          Files.asCharSink(file, StandardCharsets.UTF_8).write(f.getContent());
        }
      }

      // Asserts that the generated file paths are the same ones in the golden folder.
      ImmutableSet<String> expectedFilePaths =
          FluentIterable.from(Files.fileTraverser().depthFirstPreOrder(solution.goldenFolder))
              .filter(Files.isFile())
              .transform(relativePathFunction(solution.goldenFolder))
              .toSet();
      ImmutableSet<String> actualFilePaths =
          FluentIterable.from(solution.solutionPackage.get().getFilesList())
              .transform(
                  new Function<SolutionPackage.File, String>() {
                    @Override
                    public String apply(SolutionPackage.File f) {
                      return f.getPath();
                    }
                  })
              .toSet();
      assertThat(actualFilePaths).containsExactlyElementsIn(expectedFilePaths);

      // Now asserts the contents of the files.
      for (SolutionPackage.File file : solution.solutionPackage.get().getFilesList()) {
        File goldenFile = new File(solution.goldenFolder, file.getPath());
        if (file.getPath().endsWith(".png") || file.getPath().endsWith(".jpg")) {
          String expected = BaseEncoding.base64().encode(Files.toByteArray(goldenFile));
          assertWithMessage(generateDiffMessage(solution, file.getPath(), tempDir))
              .that(file.getContent())
              .isEqualTo(expected);
        } else {
          String expected = Files.asCharSource(goldenFile, StandardCharsets.UTF_8).read();
          String actual = file.getContent();
          String diff = StringUtils.difference(expected, actual);
          assertWithMessage(generateDiffMessage(solution, file.getPath(), tempDir))
              .that(diff)
              .isEmpty();
        }
      }
    }
  }

  private static Function<File, String> relativePathFunction(final File parent) {
    return new Function<File, String>() {
      @Override
      public String apply(File f) {
        Path parentPath = Paths.get(parent.getAbsolutePath()).normalize();
        Path filePath = Paths.get(f.getAbsolutePath()).normalize();
        return parentPath.relativize(filePath).toString();
      }
    };
  }

  private static String generateDiffMessage(Solution solution, String filePath, File tempDir) {
    return String.format(
        "Diff found in %s.\nRun this test locally with --test_strategy=local. "
            + "The diff command is: \ndiff -ur %s/%s/golden %s\n",
        filePath,
        RELATIVE_TESTDATA_PATH,
        solution.name,
        new File(tempDir, solution.name).getAbsolutePath());
  }
}
