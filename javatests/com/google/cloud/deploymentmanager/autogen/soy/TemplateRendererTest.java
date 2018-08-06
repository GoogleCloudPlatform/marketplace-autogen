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

package com.google.cloud.deploymentmanager.autogen.soy;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.deploymentmanager.autogen.soy.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests {@link TemplateRenderer}. */
@RunWith(JUnit4.class)
public class TemplateRendererTest {

  private static final String TEST_DATA_RES_PATH =
      "com/google/cloud/deploymentmanager/autogen/soy/testdata";

  /**
   * Exercises various use cases in crafting Jinja-Yaml templates. See the actual testdata files
   * for details.
   */
  @Test
  public void test() throws Exception {
    TemplateRenderer renderer =
        TemplateRenderer.FileSet.builder()
            .addContentFromResource(testDataResource("sanity_check.jinja.soy"))
            .build()
            .newRenderer("test.sanityCheck");
    renderer.setData(
        ImmutableMap.of(
            "description", "World's best",
            "zone", "myZone",
            "port", 12345,
            "supportFirewall", true,
            "supportStaticIp", false));
    String expectedOutput =
        Resources.toString(
            Resources.getResource(testDataResource("sanity_check.jinja")), StandardCharsets.UTF_8);
    assertThat(renderer.render()).isEqualTo(expectedOutput);
  }

  private static String testDataResource(String resName) {
    return TEST_DATA_RES_PATH + "/" + resName;
  }
}
