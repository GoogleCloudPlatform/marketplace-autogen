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

import com.google.cloud.deploymentmanager.autogen.Autogen.ImageInfo;
import com.google.cloud.deploymentmanager.autogen.proto.LinkInfo;
import com.google.cloud.deploymentmanager.autogen.proto.PackagedSoftwareGroup;
import com.google.cloud.deploymentmanager.autogen.proto.PackagedSoftwareGroup.SoftwareComponent;
import com.google.cloud.deploymentmanager.autogen.proto.PartnerMarketingInfo;
import com.google.cloud.deploymentmanager.autogen.proto.SolutionMarketingInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests {@link DisplayDescriptionGenerator}. */
@RunWith(JUnit4.class)
public class DisplayDescriptionGeneratorTest {

  private static final PartnerMarketingInfo PARTNER_INFO =
      PartnerMarketingInfo.newBuilder()
          .setName("partner_name")
          .setDescription("partner long description")
          .setShortDescription("partner short description")
          .setUrl("http://partner_name.com")
          .build();

  private static final SolutionMarketingInfo SOLUTION_INFO =
      SolutionMarketingInfo.newBuilder()
          .setName("solution name")
          .setVersion("1.0")
          .setDescription("solution long description")
          .setUrl("http://partner_name.com/solution_name")
          .setTagline("Partner's Solution")
          .setSupportUrl("http://partner_name.com/support")
          .setSupportInfo("support info")
          .setShowSupportId(true)
          .setEulaUrl("http://partner_name.com/eula")
          .addDocumentations(
              LinkInfo.newBuilder()
                  .addAllDestinations(
                      ImmutableList.of(
                          LinkInfo.DestinationType.DESTINATION_CONFIGURATION,
                          LinkInfo.DestinationType.DESTINATION_SOLUTION_DETAILS))
                  .setTitle("documentation 1")
                  .setDescription("documentation 1 description")
                  .setUrl("http://partner_name.com/solution_name/doc1"))
          .addDocumentations(
              LinkInfo.newBuilder()
                  .addDestinations(LinkInfo.DestinationType.DESTINATION_POST_DEPLOY)
                  .setTitle("documentation 2")
                  .setDescription("documentation 2 description")
                  .setUrl("http://partner_name.com/solution_name/doc2"))
          .addPackagedSoftwareGroups(
              PackagedSoftwareGroup.newBuilder()
                  .setType(PackagedSoftwareGroup.SoftwareGroupType.SOFTWARE_GROUP_OS)
                  .addComponents(
                      SoftwareComponent.newBuilder()
                          .setLicenseTitle("component 1 license")
                          .setLicenseUrl("http://license1.io"))
                  .addComponents(
                      SoftwareComponent.newBuilder()
                          .setLicenseTitle("component 2 license")
                          .setLicenseUrl("http://license2.io")))
          .build();

  private static final ImageInfo IMAGE_INFO = ImageInfo.builder()
      .logoPath("some_logo.png")
      .iconPath("icon_a.jpg")
      .architectureDiagramPath("architecture_diag.png")
      .architectureDiagramDescription("This is an example of a sophisticated architecture.")
      .build();

  private static final Map<String, Object> DESCRIPTION = ImmutableMap.<String, Object>builder()
      .put("author", ImmutableMap.of(
          "title", "partner_name",
          "descriptionHtml", "partner long description",
          "shortDescription", "partner short description",
          "url", "http://partner_name.com"
      ))
      .put("title", "solution name")
      .put("version", "1.0")
      .put("descriptionHtml", "solution long description")
      .put("url", "http://partner_name.com/solution_name")
      .put("tagline", "Partner's Solution")
      .put("eulaUrl", "http://partner_name.com/eula")
      .put("documentations", ImmutableList.of(
          ImmutableMap.of(
              "destinations",
              ImmutableList.of("DESTINATION_CONFIGURATION", "DESTINATION_SOLUTION_DETAILS"),
              "title", "documentation 1",
              "description", "documentation 1 description",
              "url", "http://partner_name.com/solution_name/doc1"
          ),
          ImmutableMap.of(
              "destinations", ImmutableList.of("DESTINATION_POST_DEPLOY"),
              "title", "documentation 2",
              "description", "documentation 2 description",
              "url", "http://partner_name.com/solution_name/doc2"
          )
      ))
      .put("softwareGroups", ImmutableList.of(
          ImmutableMap.of(
              "type", "SOFTWARE_GROUP_OS",
              "software", ImmutableList.of(
                  ImmutableMap.of(
                      "licenseTitle", "component 1 license",
                      "licenseUrl", "http://license1.io"
                  ),
                  ImmutableMap.of(
                      "licenseTitle", "component 2 license",
                      "licenseUrl", "http://license2.io"
                  )
              )
          )
      ))
      .put("support", ImmutableList.of(
          ImmutableMap.of(
              "title", "Support",
              "descriptionHtml", "support info",
              "url", "http://partner_name.com/support",
              "showSupportId", true
          )
      ))
      .put("logo", "some_logo.png")
      .put("icon", "icon_a.jpg")
      .put("architectureDiagram", "architecture_diag.png")
      .put("architectureDescription", "This is an example of a sophisticated architecture.")
      .build();

  @Test
  public void toConfigDisplayDescription() {
    Map<String, Object> description =
        DisplayDescriptionGenerator.createConfigDisplayDescription(
            SOLUTION_INFO, PARTNER_INFO, IMAGE_INFO);
    assertThat(description).isEqualTo(DESCRIPTION);
  }

  @Test
  public void toConfigDisplayDescription_noAuthor() {
    Map<String, Object> description =
        DisplayDescriptionGenerator.createConfigDisplayDescription(
            SOLUTION_INFO, null, IMAGE_INFO);
    Map<String, Object> expected = new HashMap<>(DESCRIPTION);
    expected.remove("author");
    assertThat(description).isEqualTo(expected);
  }
}
