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

import com.google.cloud.deploymentmanager.autogen.Autogen.ImageInfo;
import com.google.cloud.deploymentmanager.autogen.proto.LinkInfo;
import com.google.cloud.deploymentmanager.autogen.proto.LinkInfo.DestinationType;
import com.google.cloud.deploymentmanager.autogen.proto.LinkInfo.LinkType;
import com.google.cloud.deploymentmanager.autogen.proto.PackagedSoftwareGroup;
import com.google.cloud.deploymentmanager.autogen.proto.PackagedSoftwareGroup.SoftwareComponent;
import com.google.cloud.deploymentmanager.autogen.proto.PackagedSoftwareGroup.SoftwareGroupType;
import com.google.cloud.deploymentmanager.autogen.proto.PartnerMarketingInfo;
import com.google.cloud.deploymentmanager.autogen.proto.SolutionMarketingInfo;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Generates the map of the DM display metadata from autogen inputs.
 */
final class DisplayDescriptionGenerator {

  /** Creates the description section of a DM configuration display metadata. */
  public static Map<String, Object> createConfigDisplayDescription(
      SolutionMarketingInfo solutionInfo,
      @Nullable PartnerMarketingInfo partnerInfo,
      ImageInfo imageInfo) {
    Map<String, Object> description = new LinkedHashMap<>();
    if (partnerInfo != null) {
      description.put("author", buildAuthorInfo(partnerInfo));
    }
    putIfNotEmpty(description, "descriptionHtml", solutionInfo.getDescription());
    putIfNotEmpty(description, "logo", imageInfo.logoPath());
    putIfNotEmpty(description, "tagline", solutionInfo.getTagline());
    putIfNotEmpty(description, "title", solutionInfo.getName());
    putIfNotEmpty(description, "url", solutionInfo.getUrl());
    putIfNotEmpty(description, "version", solutionInfo.getVersion());
    putIfNotEmpty(description, "eulaUrl", solutionInfo.getEulaUrl());
    putIfNotEmpty(description, "softwareGroups",
        convertSoftwareGroups(solutionInfo.getPackagedSoftwareGroupsList()));
    putIfNotEmpty(description, "documentations",
        convertDocumentations(solutionInfo.getDocumentationsList()));
    putIfNotEmpty(description, "support", ImmutableList.of(buildSupport(solutionInfo)));
    putIfNotEmpty(description, "icon", imageInfo.iconPath());
    putIfNotEmpty(description, "architectureDiagram", imageInfo.architectureDiagramPath());
    putIfNotEmpty(description,
        "architectureDescription", imageInfo.architectureDiagramDescription());
    return Collections.unmodifiableMap(description);
  }

  private static void putIfNotEmpty(Map<String, Object> map, String key, String value) {
    if (value != null && !value.isEmpty()) {
      map.put(key, value);
    }
  }

  private static void putIfNotEmpty(Map<String, Object> map, String key, List<?> value) {
    if (!value.isEmpty()) {
      map.put(key, value);
    }
  }

  private static Map<String, Object> buildAuthorInfo(PartnerMarketingInfo partnerInfo) {
    Map<String, Object> map = new LinkedHashMap<>();
    putIfNotEmpty(map, "title", partnerInfo.getName());
    putIfNotEmpty(map, "descriptionHtml", partnerInfo.getDescription());
    putIfNotEmpty(map, "shortDescription", partnerInfo.getShortDescription());
    putIfNotEmpty(map, "url", partnerInfo.getUrl());
    return map;
  }

  private static Map<String, Object> buildDocumentation(LinkInfo linkInfo) {
    Map<String, Object> documentation = new LinkedHashMap<>();
    putIfNotEmpty(documentation, "title", linkInfo.getTitle());
    putIfNotEmpty(documentation, "url", linkInfo.getUrl());
    putIfNotEmpty(documentation, "description", linkInfo.getDescription());
    putIfNotEmpty(documentation, "destinations",
        Lists.transform(linkInfo.getDestinationsList(),
            new Function<DestinationType, String>() {
              @Override
              public String apply(DestinationType input) {
                return input.name();
              }
            }));
    if (linkInfo.getType() != LinkType.LINK_GENERAL) {
      documentation.put("linkType", linkInfo.getType().name());
    }
    return documentation;
  }

  private static Map<String, Object> buildSoftware(SoftwareComponent component) {
    Map<String, Object> software = new LinkedHashMap<>();
    putIfNotEmpty(software, "title", component.getName());
    putIfNotEmpty(software, "version", component.getVersion());
    putIfNotEmpty(software, "url", component.getUrl());
    putIfNotEmpty(software, "licenseTitle", component.getLicenseTitle());
    putIfNotEmpty(software, "licenseUrl", component.getLicenseUrl());
    return software;
  }

  private static Map<String, Object> buildSoftwareGroup(PackagedSoftwareGroup group) {
    Map<String, Object> softwareGroup = new LinkedHashMap<>();
    // If if is the default value, don't copy it
    if (group.getType() != SoftwareGroupType.SOFTWARE_GROUP_TYPE_UNSPECIFIED) {
      softwareGroup.put("type", group.getType().name());
    }
    putIfNotEmpty(softwareGroup, "software",
        Lists.transform(group.getComponentsList(), new Function<SoftwareComponent, Object>() {
          @Override
          public Object apply(SoftwareComponent input) {
            return buildSoftware(input);
          }
        }));
    return softwareGroup;
  }

  private static List<Map<String, Object>> convertDocumentations(List<LinkInfo> links) {
    return Lists.transform(links, new Function<LinkInfo, Map<String, Object>>() {
      @Override
      public Map<String, Object> apply(LinkInfo input) {
        return buildDocumentation(input);
      }
    });
  }

  private static List<Map<String, Object>> convertSoftwareGroups(
      List<PackagedSoftwareGroup> groups) {
    return Lists.transform(groups, new Function<PackagedSoftwareGroup, Map<String, Object>>() {
      @Override
      public Map<String, Object> apply(PackagedSoftwareGroup input) {
        return buildSoftwareGroup(input);
      }
    });
  }

  private static Map<String, Object> buildSupport(SolutionMarketingInfo solutionInfo) {
    Map<String, Object> support = new LinkedHashMap<>();
    putIfNotEmpty(support, "title", "Support");
    putIfNotEmpty(support, "descriptionHtml", solutionInfo.getSupportInfo());
    putIfNotEmpty(support, "url", solutionInfo.getSupportUrl());
    if (solutionInfo.getShowSupportId()) {
      support.put("showSupportId", solutionInfo.getShowSupportId());
    }
    return support;
  }
}
