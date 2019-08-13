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

import com.google.cloud.deploymentmanager.autogen.proto.SolutionPackage;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import java.io.File;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Parameterized test that will compare the list of generated files by the Solution and compare to
 * our golden folder of that solution
 */
@RunWith(Parameterized.class)
public class VerifySolutionFileSetTest {
  @Parameter public AutogenMediumTestsSuite.Solution solution;

  @Parameters(name = "Solution {0}")
  public static Collection<AutogenMediumTestsSuite.Solution> data() {
    return AutogenMediumTestsSuite.Solution.findAllSolutions();
  }

  @Test
  public void verifyGeneratedFiles() {
    final ImmutableSet<String> expectedFiles =
        FluentIterable.from(Files.fileTraverser().depthFirstPreOrder(solution.goldenFolder))
            .filter(Files.isFile())
            .transform(relativePathFunction(solution.goldenFolder))
            .toSet();

    final ImmutableSet<String> actualFilePaths =
        FluentIterable.from(solution.solutionPackage.get().getFilesList())
            .transform(
                new Function<SolutionPackage.File, String>() {
                  @Override
                  public String apply(SolutionPackage.File f) {
                    return f.getPath();
                  }
                })
            .toSet();
    assertThat(actualFilePaths).containsExactlyElementsIn(expectedFiles);
  }

  static Function<File, String> relativePathFunction(final File parent) {
    return new Function<File, String>() {
      @Override
      public String apply(File f) {
        return AutogenMediumTestsSuite.relativePathFunction(parent, f);
      }
    };
  }
}
