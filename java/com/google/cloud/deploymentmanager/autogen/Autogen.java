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
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.TierTemplateName;
import com.google.cloud.deploymentmanager.autogen.proto.DeploymentPackageAutogenSpec;
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
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
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
      BUNDLED_RESOURCE_PATH + "/sharedsupport";

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

  private static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new TemplateRenderer.Module());
      install(new SoyFunctions.Module());
      install(new SoyDirectives.Module());
    }

    // Lists the soy template files to compile.
    @Provides
    @Singleton
    TemplateRenderer.FileSet provideFileSet(TemplateRenderer.FileSet.Builder builder) {
      return builder
          .addContentFromResource(resource("singlevm/c2d_deployment_configuration.json.soy"))
          .addContentFromResource(resource("singlevm/solution.jinja.soy"))
          .addContentFromResource(resource("singlevm/solution.jinja.schema.soy"))
          .addContentFromResource(resource("singlevm/solution.jinja.display.soy"))
          .addContentFromResource(resource("singlevm/test_config.yaml.soy"))
          .addContentFromResource(resource("multivm/c2d_deployment_configuration.json.soy"))
          .addContentFromResource(resource("multivm/solution.jinja.soy"))
          .addContentFromResource(resource("multivm/solution.jinja.schema.soy"))
          .addContentFromResource(resource("multivm/solution.jinja.display.soy"))
          .addContentFromResource(resource("multivm/test_config.yaml.soy"))
          .addContentFromResource(resource("multivm/tier.jinja.soy"))
          .addContentFromResource(resource("multivm/tier.jinja.schema.soy"))
          .addContentFromResource(resource("display.soy"))
          .addContentFromResource(resource("renders.soy"))
          .addContentFromResource(resource("utilities.soy"))
          .addProtoDescriptors(
              DeploymentPackageAutogenSpecProtos.getDescriptor(),
              MarketingInfoProtos.getDescriptor())
          .build();
    }
  }

  private static final Supplier<Injector> injector =
      Suppliers.memoize(
          new Supplier<Injector>() {
            @Override
            public Injector get() {
              return Guice.createInjector(new Module());
            }
          });

  /** Returns a preconfigured instance. */
  public static Autogen getInstance() {
    return injector.get().getInstance(Autogen.class);
  }

  private final TemplateRenderer.FileSet fileSet;

  @Inject
  Autogen(TemplateRenderer.FileSet fileSet) {
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
    SolutionPackage.Builder builder = SolutionPackage.newBuilder();
    String solutionId = input.getSolutionId();
    ImageInfo imageInfo = generateImages(input, builder);
    Map<String, Object> params = makeSingleVmParams(input, imageInfo);

    builder
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja")
                .setContent(fileSet.newRenderer("vm.single.jinja").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja.schema")
                .setContent(fileSet.newRenderer("vm.single.schema").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja.display")
                .setContent(fileSet.newRenderer("vm.single.display").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("test_config.yaml")
                .setContent(fileSet.newRenderer("vm.single.test_config").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("c2d_deployment_configuration.json")
                .setContent(fileSet.newRenderer("vm.single.c2d_deployment_configuration")
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

  /** Builds the deployment package for {@link MultiVmDeploymentPackageSpec} */
  private SolutionPackage buildMultiVm(
      DeploymentPackageInput input, SharedSupportFilesStrategy sharedSupportFilesStrategy) {
    SolutionPackage.Builder builder = SolutionPackage.newBuilder();
    String solutionId = input.getSolutionId();
    ImageInfo imageInfo = generateImages(input, builder);
    MultiVmDeploymentPackageSpec spec = input.getSpec().getMultiVm();
    Map<String, Object> params = makeMultiVmParams(input, imageInfo);

    for (VmTierSpec tierSpec : spec.getTiersList()) {
      ImmutableMap<String, Object> tierParams = ImmutableMap.of(
          "spec", tierSpec, "packageSpec", spec);
      builder
          .addFiles(
              SolutionPackage.File.newBuilder()
                  .setPath(TierTemplateName.apply(tierSpec))
                  .setContent(
                      fileSet.newRenderer("vm.multi.tierJinja").setData(tierParams).render()))
          .addFiles(
              SolutionPackage.File.newBuilder()
                  .setPath(TierTemplateName.apply(tierSpec) + ".schema")
                  .setContent(
                      fileSet
                          .newRenderer("vm.multi.tierJinjaSchema")
                          .setData(tierParams)
                          .render()));
    }
    builder
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja")
                .setContent(fileSet.newRenderer("vm.multi.jinja").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja.schema")
                .setContent(fileSet.newRenderer("vm.multi.schema").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath(solutionId + ".jinja.display")
                .setContent(fileSet.newRenderer("vm.multi.display").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("test_config.yaml")
                .setContent(fileSet.newRenderer("vm.multi.test_config").setData(params).render()))
        .addFiles(
            SolutionPackage.File.newBuilder()
                .setPath("c2d_deployment_configuration.json")
                .setContent(fileSet.newRenderer("vm.multi.c2d_deployment_configuration")
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
        .build();
  }

  private ImmutableMap<String, Object> makeMultiVmParams(
      DeploymentPackageInput input, ImageInfo imageInfo) {
    return makeTemplateParams(input, imageInfo)
        .put("spec", input.getSpec().getMultiVm())
        .build();
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
    return new Yaml(new SafeConstructor(), new Representer(), dumperOptions)
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
