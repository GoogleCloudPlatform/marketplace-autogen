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

import static com.google.common.truth.Truth.assertWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/** Parameterized test that will go through each solution file and compare it to a golden. */
@RunWith(Parameterized.class)
public class VerifyAutogenFileContent {
  @Parameter public String solutionName;

  @Parameter(1)
  public String goldenFolder;

  @Parameter(2)
  public String fileRelativePath;

  @Parameter(3)
  public String fileContent;

  @Parameters(name = "File {2} from Solution {0}")
  public static Collection<String[]> data() {
    var solutions = AutogenMediumTestsSuite.Solution.allSolutionsActualFilesDesc();
    Preconditions.checkArgument(!solutions.isEmpty(), "Did not find any test solutions");
    return solutions;
  }

  @Test
  public void goldenVerifyFile() throws Exception {
    File tempDir = Files.createTempDir();
    File actualFile = new File(tempDir, fileRelativePath);
    Files.createParentDirs(actualFile);
    File goldenFile = new File(goldenFolder, fileRelativePath);
    if (actualFile.getPath().endsWith(".png") || actualFile.getPath().endsWith(".jpg")) {
      Files.write(BaseEncoding.base64().decode(fileContent), actualFile);
      String expected = BaseEncoding.base64().encode(Files.toByteArray(goldenFile));
      assertWithMessage(generateDiffMessage(tempDir)).that(fileContent).isEqualTo(expected);
    } else {
      Files.asCharSink(actualFile, StandardCharsets.UTF_8).write(fileContent);
      String expected = Files.asCharSource(goldenFile, StandardCharsets.UTF_8).read();
      // From StringUtils.difference docs:
      // "returns the remainder of the second String, starting from where it's different from the
      // first. This means that the difference between "abc" and "ab" is the empty String and not
      // "c".
      // Because of that we need to compare actual vs expected as well

      String diffMessage = generateDiffMessage(tempDir);

      // This overwrites the test files when using bazel run
      if (System.getenv("BUILD_WORKSPACE_DIRECTORY") != null
          && (!StringUtils.difference(expected, fileContent).isEmpty()
              || !StringUtils.difference(fileContent, expected).isEmpty())) {
        File overwriteFile =
            new File(
                Path.of(
                        System.getenv("BUILD_WORKSPACE_DIRECTORY"),
                        ".",
                        AutogenMediumTestsSuite.RELATIVE_TESTDATA_PATH,
                        Path.of(solutionName).getParent().toString(),
                        Path.of(goldenFolder).getFileName().toString(),
                        fileRelativePath)
                    .toString());
        Files.write(fileContent.getBytes(UTF_8), overwriteFile);
      }

      assertWithMessage(diffMessage).that(StringUtils.difference(expected, fileContent)).isEmpty();
      assertWithMessage(diffMessage).that(StringUtils.difference(fileContent, expected)).isEmpty();
    }
  }

  private String generateDiffMessage(File tempDir) {
    return String.format(
        "Diff found in %s.\nRun this test locally with --test_strategy=local. "
            + "The diff command is: \ndiff -ur %s %s\n",
        fileRelativePath, goldenFolder, tempDir);
  }
}
