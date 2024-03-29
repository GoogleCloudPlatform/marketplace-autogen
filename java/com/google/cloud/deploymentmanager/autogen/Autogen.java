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

import com.google.auto.value.AutoValue;
import com.google.cloud.deploymentmanager.autogen.Autogen.Module.TemplateFileSet;
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.TierTemplateName;
import com.google.cloud.deploymentmanager.autogen.proto.DeploymentPackageAutogenSpec;
import com.google.cloud.deploymentmanager.autogen.proto.DeploymentPackageAutogenSpec.DeploymentTool;
import com.google.cloud.deploymentmanager.autogen.proto.DeploymentPackageAutogenSpecProtos;
import com.google.cloud.deploymentmanager.autogen.proto.DeploymentPackageInput;
import com.google.cloud.deploymentmanager.autogen.proto.Image;
import com.google.cloud.deploymentmanager.autogen.proto.MarketingInfoProtos;
import com.google.cloud.deploymentmanager.autogen.proto.MultiVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.SingleVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.SolutionPackage;
import com.google.cloud.deploymentmanager.autogen.proto.VmTierSpec;
import com.google.cloud.deploymentmanager.autogen.soy.TemplateRenderer;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import javax.inject.Qualifier;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Main entry point to the autogen library.
 */
// TODO(huyhuynh): Add tests.
public class Autogen {

  private static final String BUNDLED_RESOURCE_PATH =
      "com/google/cloud/deploymentmanager/autogen/templates";
  private static final String BUNDLED_SHARED_SUPPORT_RESOURCE_PATH =
      BUNDLED_RESOURCE_PATH + "/dm/sharedsupport";

  private static final String MEDIA_RESOURCE_PREFIX = "@media/";
  private static final String RESOURCE_PATH_PREFIX = "resources/en-us/";

  // TODO(huyhuynh): Consider including only the needed files based on spec, instead of always all.
  private static final ImmutableList<String> SINGLE_VM_SHARED_SUPPORT_FILES = ImmutableList.of(
      "common/common.py",
      "common/default.py",
      "common/password.py",
      "common/path_utils.jinja",
      "common/software_status.py",
      "common/software_status.py.schema",
      "common/software_status.sh.tmpl",
      "common/software_status_script.py",
      "common/software_status_script.py.schema",
      "common/vm_instance.py",
      "common/vm_instance.py.schema");

  private static final ImmutableList<String> DEPLOYMENT_MANAGER_SOY_FILES =
      ImmutableList.of(
          "singlevm/c2d_deployment_configuration.json.soy",
          "singlevm/solution.jinja.soy",
          "singlevm/solution.jinja.schema.soy",
          "singlevm/solution.jinja.display.soy",
          "singlevm/test_config.yaml.soy",
          "multivm/c2d_deployment_configuration.json.soy",
          "multivm/solution.jinja.soy",
          "multivm/solution.jinja.schema.soy",
          "multivm/solution.jinja.display.soy",
          "multivm/test_config.yaml.soy",
          "multivm/tier.jinja.soy",
          "multivm/tier.jinja.schema.soy",
          "display.soy",
          "renders.soy",
          "utilities.soy");

  private static final ImmutableList<String> TERRAFORM_SOY_FILES =
      ImmutableList.of(
          "singlevm/README.md.soy",
          "singlevm/main.tf.soy",
          "singlevm/variables.tf.soy",
          "singlevm/marketplace_test.tfvars.soy",
          "singlevm/outputs.tf.soy",
          "singlevm/metadata.yaml.soy",
          "singlevm/metadata.display.yaml.soy",
          "multivm/README.md.soy",
          "multivm/main.tf.soy",
          "multivm/variables.tf.soy",
          "multivm/marketplace_test.tfvars.soy",
          "multivm/outputs.tf.soy",
          "multivm/metadata.yaml.soy",
          "multivm/metadata.display.yaml.soy",
          "multivm/tier.main.tf.soy",
          "multivm/tier.variables.tf.soy",
          "multivm/tier.outputs.tf.soy",
          "blocks.soy",
          "constants.soy",
          "util.soy",
          "metadata_blocks.soy",
          "metadata_display_blocks.soy");

  private static final LoadingCache<String, String> sharedSupportFilesCache =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<String, String>() {
                @Override
                public String load(String filename) throws Exception {
                  return Resources.toString(
                      Resources.getResource(sharedSupportResource(filename)),
                      StandardCharsets.UTF_8);
                }
              });

  /**
   * Defines the behavior of autogen wrt shared support files.
   *
   * <p>These files are sharable across solutions. The caller can decide not to include and add them
   * to the deployment package after the fact. Useful when, for example, the generated package files
   * are checked into source control and the shared support files should be symlinks.
   */
  public enum SharedSupportFilesStrategy {
    INCLUDED,
    EXCLUDED,
  }

  static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new TemplateRenderer.Module());
      install(new SoyFunctions.Module());
      install(new SoyDirectives.Module());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    @interface TemplateFileSet {}

    // Lists the soy template files to compile.
    @Provides
    @Singleton
    @TemplateFileSet
    TemplateRenderer.FileSet provideFileSet(TemplateRenderer.FileSet.Builder builder) {
      DEPLOYMENT_MANAGER_SOY_FILES.forEach(
          file -> builder.addContentFromResource(resource("dm/" + file)));
      TERRAFORM_SOY_FILES.forEach(file -> builder.addContentFromResource(resource("tf/" + file)));

      return builder
          .addProtoDescriptors(
              DeploymentPackageAutogenSpecProtos.getDescriptor(),
              MarketingInfoProtos.getDescriptor())
          .build();
    }
  }

  public static AbstractModule getAutogenModule() {
    return new Module();
  }

  private final TemplateRenderer.FileSet fileSet;

  @Inject
  Autogen(@TemplateFileSet TemplateRenderer.FileSet fileSet) {
    this.fileSet = fileSet;
  }

  /** Generates the deployment package. */
  public SolutionPackage generateDeploymentPackage(
      DeploymentPackageInput input, SharedSupportFilesStrategy sharedSupportFilesStrategy) {
    validate(input);
    DeploymentPackageInput.Builder inputBuilder = input.toBuilder();
    DeploymentPackageAutogenSpec.Builder specBuilder = inputBuilder.getSpecBuilder();
    switch (specBuilder.getSpecCase()) {
      case SINGLE_VM:
        {
          SpecDefaults.fillInMissingDefaults(specBuilder.getSingleVmBuilder());
          DeploymentPackageInput built = inputBuilder.build();
          SpecValidations.validate(built.getSpec().getSingleVm());
          return buildSingleVm(built, sharedSupportFilesStrategy);
        }
      case MULTI_VM:
        {
          SpecDefaults.fillInMissingDefaults(specBuilder.getMultiVmBuilder());
          DeploymentPackageInput built = inputBuilder.build();
          SpecValidations.validate(built.getSpec().getMultiVm());
          return buildMultiVm(built, sharedSupportFilesStrategy);
        }
      default:
        throw new IllegalArgumentException("No valid autogen spec is specified");
    }
  }

  /** Holds information about various images in the deployment package. */
  @AutoValue
  abstract static class ImageInfo {
    static Builder builder() {
      return new AutoValue_Autogen_ImageInfo.Builder();
    }

    @Nullable public abstract String logoPath();
    @Nullable public abstract String logoDescription();
    @Nullable public abstract String iconPath();
    @Nullable public abstract String iconDescription();
    @Nullable public abstract String architectureDiagramPath();
    @Nullable public abstract String architectureDiagramDescription();

    @AutoValue.Builder
    interface Builder {
      Builder logoPath(String path);
      Builder logoDescription(String description);
      Builder iconPath(String path);
      Builder iconDescription(String description);
      Builder architectureDiagramPath(String path);
      Builder architectureDiagramDescription(String description);
      ImageInfo build();
    }
  }

  /** Builds the deployment package for {@link SingleVmDeploymentPackageSpec} */
  private SolutionPackage buildSingleVm(
      DeploymentPackageInput input, SharedSupportFilesStrategy sharedSupportFilesStrategy) {
    switch (input.getSpec().getDeploymentTool()) {
      case DEPLOYMENT_TOOL_UNSPECIFIED:
      case DEPLOYMENT_MANAGER:
        return buildDmSingleVm(input, sharedSupportFilesStrategy);
      case TERRAFORM:
        return buildTerraformSingleVm(input);
      case UNRECOGNIZED:
        throw new AssertionError("unrecognized deployment tool");
    }
    throw new AssertionError("unreachable");
  }

  private SolutionPackage buildDmSingleVm(
      DeploymentPackageInput input, SharedSupportFilesStrategy sharedSupportFilesStrategy) {
    SolutionPackage.Builder builder = SolutionPackage.newBuilder();
    String solutionId = input.getSolutionId();
    ImageInfo imageInfo = generateImages(input, builder);
    Map<String, Object> params = makeSingleVmParams(input, imageInfo);

    builder
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja")
                .setContent(fileSet.newRenderer("vm.single.jinja.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja.schema")
                .setContent(fileSet.newRenderer("vm.single.schema.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja.display")
                .setContent(fileSet.newRenderer("vm.single.display.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("test_config.yaml")
                .setContent(
                    fileSet.newRenderer("vm.single.test_config.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("c2d_deployment_configuration.json")
                .setContent(
                    fileSet
                        .newRenderer("vm.single.c2d_deployment_configuration.main")
                        .setData(params)
                        .render()));

    if (sharedSupportFilesStrategy == SharedSupportFilesStrategy.INCLUDED) {
      for (String filename : SINGLE_VM_SHARED_SUPPORT_FILES) {
        try {
          builder
              .addFilesBuilder()
              .setPath(filename)
              .setContent(sharedSupportFilesCache.get(filename));
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return builder.build();
  }

  private SolutionPackage buildTerraformSingleVm(DeploymentPackageInput input) {
    SolutionPackage.Builder builder = SolutionPackage.newBuilder();
    ImageInfo imageInfo = ImageInfo.builder().build();
    ImmutableMap<String, Object> params = makeSingleVmParams(input, imageInfo);

    builder
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("README.md")
                .setContent(fileSet.newRenderer("vm.single.readme.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("main.tf")
                .setContent(fileSet.newRenderer("vm.single.tf.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("variables.tf")
                .setContent(
                    fileSet.newRenderer("vm.single.variables.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("marketplace_test.tfvars")
                .setContent(fileSet.newRenderer("vm.single.tfvars.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("outputs.tf")
                .setContent(fileSet.newRenderer("vm.single.outputs.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("metadata.yaml")
                .setContent(
                    fileSet.newRenderer("vm.single.metadata.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("metadata.display.yaml")
                .setContent(
                    fileSet
                        .newRenderer("vm.single.metadata.display.main")
                        .setData(params)
                        .render()));

    return builder.build();
  }

  /** Builds the deployment package for {@link MultiVmDeploymentPackageSpec} */
  private SolutionPackage buildMultiVm(
      DeploymentPackageInput input, SharedSupportFilesStrategy sharedSupportFilesStrategy) {
    switch (input.getSpec().getDeploymentTool()) {
      case DEPLOYMENT_TOOL_UNSPECIFIED:
      case DEPLOYMENT_MANAGER:
        return buildDmMultiVm(input, sharedSupportFilesStrategy);
      case TERRAFORM:
        return buildTerraformMultiVm(input);
      case UNRECOGNIZED:
        throw new AssertionError("unrecognized deployment tool");
    }
    throw new AssertionError("unreachable");
  }

  private SolutionPackage buildDmMultiVm(
      DeploymentPackageInput input, SharedSupportFilesStrategy sharedSupportFilesStrategy) {
    SolutionPackage.Builder builder = SolutionPackage.newBuilder();
    String solutionId = input.getSolutionId();
    ImageInfo imageInfo = generateImages(input, builder);
    MultiVmDeploymentPackageSpec spec = input.getSpec().getMultiVm();
    ImmutableMap<String, Object> params = makeMultiVmParams(input, imageInfo);

    for (VmTierSpec tierSpec : spec.getTiersList()) {
      ImmutableMap<String, Object> tierParams = ImmutableMap.of(
          "spec", tierSpec, "packageSpec", spec);
      builder
          .addFiles(
              SolutionPackage.File.newBuilder()
                  .setPath(TierTemplateName.apply(tierSpec))
                  .setContent(
                      fileSet.newRenderer("vm.multi.tierJinja.main").setData(tierParams).render()))
          .addFiles(
              SolutionPackage.File.newBuilder()
                  .setPath(TierTemplateName.apply(tierSpec) + ".schema")
                  .setContent(
                      fileSet
                          .newRenderer("vm.multi.tierSchema.main")
                          .setData(tierParams)
                          .render()));
    }
    builder
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja")
                .setContent(fileSet.newRenderer("vm.multi.jinja.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja.schema")
                .setContent(fileSet.newRenderer("vm.multi.schema.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja.display")
                .setContent(fileSet.newRenderer("vm.multi.display.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("test_config.yaml")
                .setContent(
                    fileSet.newRenderer("vm.multi.test_config.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("c2d_deployment_configuration.json")
                .setContent(
                    fileSet
                        .newRenderer("vm.multi.c2d_deployment_configuration.main")
                        .setData(params)
                        .render()));

    if (sharedSupportFilesStrategy == SharedSupportFilesStrategy.INCLUDED) {
      for (String filename : SINGLE_VM_SHARED_SUPPORT_FILES) {
        try {
          builder
              .addFilesBuilder()
              .setPath(filename)
              .setContent(sharedSupportFilesCache.get(filename));
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return builder.build();
  }

  private SolutionPackage buildTerraformMultiVm(DeploymentPackageInput input) {
    SolutionPackage.Builder builder = SolutionPackage.newBuilder();
    ImageInfo imageInfo = ImageInfo.builder().build();
    ImmutableMap<String, Object> params = makeMultiVmParams(input, imageInfo);
    MultiVmDeploymentPackageSpec spec = input.getSpec().getMultiVm();
    for (VmTierSpec tierSpec : spec.getTiersList()) {
      ImmutableMap<String, Object> tierParams = ImmutableMap.of(
          "spec", tierSpec, "packageSpec", spec);
      builder
          .addFiles(
              SolutionPackage.File.newBuilder()
                  .setPath(String.format("modules/%s/main.tf", tierSpec.getName()))
                  .setContent(
                      fileSet.newRenderer("vm.multi.tierTf.main").setData(tierParams).render()))
          .addFiles(
              SolutionPackage.File.newBuilder()
                  .setPath(String.format("modules/%s/variables.tf", tierSpec.getName()))
                  .setContent(
                      fileSet
                          .newRenderer("vm.multi.tier.variables.main")
                          .setData(tierParams)
                          .render()))
          .addFiles(
              SolutionPackage.File.newBuilder()
                  .setPath(String.format("modules/%s/outputs.tf", tierSpec.getName()))
                  .setContent(
                      fileSet
                          .newRenderer("vm.multi.tier.outputs.main")
                          .setData(tierParams)
                          .render()));
    }

    builder
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("README.md")
                .setContent(fileSet.newRenderer("vm.multi.readme.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("main.tf")
                .setContent(fileSet.newRenderer("vm.multi.tf.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("variables.tf")
                .setContent(
                    fileSet.newRenderer("vm.multi.variables.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("outputs.tf")
                .setContent(fileSet.newRenderer("vm.multi.outputs.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("marketplace_test.tfvars")
                .setContent(fileSet.newRenderer("vm.multi.tfvars.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("metadata.yaml")
                .setContent(fileSet.newRenderer("vm.multi.metadata.main").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("metadata.display.yaml")
                .setContent(
                    fileSet
                        .newRenderer("vm.multi.metadata.display.main")
                        .setData(params)
                        .render()));
    return builder.build();
  }

  private void validate(DeploymentPackageInput input) {
    Preconditions.checkArgument(!input.getSolutionId().isEmpty(), "solution_id is required");
    Preconditions.checkArgument(!input.getPartnerId().isEmpty(), "partner_id is required");
    Preconditions.checkArgument(input.hasSpec(), "spec is required");
    if (input.hasSolutionInfo()) {
      Preconditions.checkArgument(
          !input.getSolutionInfo().getName().isEmpty(), "name in solution info is required");
    }
    if (input.hasIcon()) {
      validate(input.getIcon());
    }
    if (input.hasLogo()) {
      validate(input.getLogo());
    }
    if (input.hasArchitectureDiagram()) {
      validate(input.getArchitectureDiagram());
    }
    if (input.getSpec().getDeploymentTool().equals(DeploymentTool.TERRAFORM)) {
      validateTerraformAutogen(input.getSpec());
    }
  }

  private void validateTerraformAutogen(DeploymentPackageAutogenSpec spec) {
    if (spec.hasSingleVm()) {
      Preconditions.checkArgument(
          !spec.getSingleVm().hasApplicationStatus(),
          "Terraform autogen does not support Application Status.");
    } else if (spec.hasMultiVm()) {
      for (VmTierSpec tier : spec.getMultiVm().getTiersList()) {
        Preconditions.checkArgument(
            !tier.hasApplicationStatus(), "Terraform autogen does not support Application Status.");
      }
    }
  }

  private void validate(Image image) {
    Preconditions.checkArgument(image.hasRaw(), "Image raw bytes are required");
    Preconditions.checkArgument(
        !image.getRaw().getContent().isEmpty(), "Raw image content is required");
    Preconditions.checkArgument(
        image.getRaw().getContentTypeValue() > 0, "Raw image content type is required");
  }

  private ImageInfo generateImages(
      DeploymentPackageInput input, SolutionPackage.Builder builder) {
    String solutionId = input.getSolutionId();
    ImageInfo.Builder imageInfoBuilder = ImageInfo.builder();
    if (input.hasLogo()) {
      Image image = input.getLogo();
      String namePrefix = solutionId + "_store";
      SolutionPackage.File fileContent = makeImageFileContent(image.getRaw(), namePrefix);
      String imageName = makeImageName(image.getRaw(), namePrefix);
      builder.addFiles(fileContent);
      imageInfoBuilder.logoPath(MEDIA_RESOURCE_PREFIX + imageName);
      if (!image.getDescription().isEmpty()) {
        imageInfoBuilder.logoDescription(image.getDescription());
      }
    }
    if (input.hasIcon()) {
      Image image = input.getIcon();
      String namePrefix = solutionId + "_small";
      SolutionPackage.File fileContent = makeImageFileContent(image.getRaw(), namePrefix);
      String imageName = makeImageName(image.getRaw(), namePrefix);
      builder.addFiles(fileContent);
      imageInfoBuilder.iconPath(MEDIA_RESOURCE_PREFIX + imageName);
      if (!image.getDescription().isEmpty()) {
        imageInfoBuilder.iconDescription(image.getDescription());
      }
    }
    if (input.hasArchitectureDiagram()) {
      Image image = input.getArchitectureDiagram();
      String namePrefix = solutionId + "_architecture_diagram";
      SolutionPackage.File fileContent = makeImageFileContent(image.getRaw(), namePrefix);
      String imageName = makeImageName(image.getRaw(), namePrefix);
      builder.addFiles(fileContent);
      imageInfoBuilder.architectureDiagramPath(MEDIA_RESOURCE_PREFIX + imageName);
      if (!image.getDescription().isEmpty()) {
        imageInfoBuilder.architectureDiagramDescription(image.getDescription());
      }
    }
    return imageInfoBuilder.build();
  }

  private ImmutableMap<String, Object> makeSingleVmParams(
      DeploymentPackageInput input, ImageInfo imageInfo) {
    return makeTemplateParams(input, imageInfo)
        .put("spec", input.getSpec().getSingleVm())
        .buildOrThrow();
  }

  private ImmutableMap<String, Object> makeMultiVmParams(
      DeploymentPackageInput input, ImageInfo imageInfo) {
    return makeTemplateParams(input, imageInfo)
        .put("spec", input.getSpec().getMultiVm())
        .buildOrThrow();
  }

  private Builder<String, Object> makeTemplateParams(
      DeploymentPackageInput input, ImageInfo imageInfo) {
    Builder<String, Object> params = ImmutableMap.builder();
    params.put("solutionId", input.getSolutionId());
    if (!input.getPartnerId().isEmpty()) {
      params.put("partnerId", input.getPartnerId());
    }
    if (input.hasPartnerInfo()) {
      params.put("partnerInfo", input.getPartnerInfo());
    }
    if (input.hasSolutionInfo()) {
      params.put("solutionInfo", input.getSolutionInfo());
    }
    if (!Strings.isNullOrEmpty(input.getSpec().getVersion())) {
      params.put("templateVersion", input.getSpec().getVersion());
    }
    params.put("descriptionYaml", makeDescriptionYaml(input, imageInfo));
    return params;
  }

  // Constructs the description section of the display metadata, and serializes it to yaml.
  private String makeDescriptionYaml(DeploymentPackageInput input, ImageInfo imageInfo) {
    if (input.hasSolutionInfo()) {
      Map<String, Object> description =
          DisplayDescriptionGenerator.createConfigDisplayDescription(
                  input.getSolutionInfo(), input.getPartnerInfo(), imageInfo);
      return toYaml(description);
    } else {
      return "";
    }
  }

  private SolutionPackage.File makeImageFileContent(
      Image.RawImage logo, String namePrefix) {
    String name = makeImageName(logo, namePrefix);
    return SolutionPackage.File.newBuilder()
            .setPath(RESOURCE_PATH_PREFIX + name)
            .setContent(BaseEncoding.base64().encode(logo.getContent().toByteArray()))
            .build();
  }

  private String makeImageName(Image.RawImage logo, String prefix) {
    switch (logo.getContentType()) {
      case PNG:
        return prefix + ".png";
      case JPEG:
        return prefix + ".jpg";
      default:
        throw new IllegalArgumentException("Content type must be specified");
    }
  }

  // Dumps a protobuf message map as yaml.
  private static String toYaml(Map<String, Object> message) {
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
    dumperOptions.setWidth(3000);  // No auto line breaking on a field.
    dumperOptions.setDefaultScalarStyle(ScalarStyle.PLAIN);
    return new Yaml(
            new SafeConstructor(new LoaderOptions()),
            new Representer(dumperOptions),
            dumperOptions)
        .dump(message)
        .trim();
  }

  private static String resource(String resName) {
    return BUNDLED_RESOURCE_PATH + "/" + resName;
  }

  private static String sharedSupportResource(String resName) {
    return BUNDLED_SHARED_SUPPORT_RESOURCE_PATH + "/" + resName;
  }
}
