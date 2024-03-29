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

import com.google.cloud.deploymentmanager.autogen.soy.TemplateRenderer.Module.SoyFiles;
import com.google.common.io.Resources;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.jbcsrc.api.LegacySoyJavaStringCaller;
import com.google.template.soy.jbcsrc.api.SoySauce;
import com.google.template.soy.jbcsrc.api.SoySauce.Renderer;
import com.google.template.soy.shared.restricted.SoyFunction;
import com.google.template.soy.shared.restricted.SoyPrintDirective;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import javax.inject.Qualifier;

/**
 * Supports rendering of soy templates
 *
 * <p>Uses a preprocessor to works around soy's limitation to enable easy crafting of yaml jinja soy
 * templates by:
 *
 * <ul>
 *   <li>Preserving indentations and line breaks
 *   <li>Allowing jinja delimiters directly in the soy template without the need to escape them
 *   <li>Adding new soy directives to support yaml conventions (see {@link SoyDirectives})
 * </ul>
 *
 * <p>See {@link TemplateRendererTest} for examples of how to take advantage of this templating
 * system.
 */
public final class TemplateRenderer {

  /**
   * A bundle of soy template files from which renderers can be created, similar to
   * {@code SoyFileSet}.
   */
  public static class FileSet {

    /** Builds {@link FileSet}. */
    public static class Builder {

      private final SoyFileSet.Builder soyFileSetBuilder;

      /** Use {@link FileSet#builder} to create an instance. */
      @Inject
      Builder(@SoyFiles SoyFileSet.Builder soyFileSetBuilder) {
        this.soyFileSetBuilder = soyFileSetBuilder;
      }

      /** Adds the content of a file. */
      private Builder add(String content, String filePath) {
        soyFileSetBuilder.add(Preprocessor.preprocess(content), filePath);
        return this;
      }

      /** Adds a resource file from the bundled resources. */
      @CanIgnoreReturnValue
      public Builder addContentFromResource(String resourceName) {
        try {
          String content =
              Resources.toString(Resources.getResource(resourceName), StandardCharsets.UTF_8);
          return add(content, resourceName);
        } catch (IOException ioe) {
          throw new RuntimeException(ioe);
        }
      }

      /** Adds the descriptors for protos that will be used by the soy templates. */
      public Builder addProtoDescriptors(GenericDescriptor... descriptors) {
        soyFileSetBuilder.addProtoDescriptors(descriptors);
        return this;
      }

      public FileSet build() {
        return new FileSet(soyFileSetBuilder.build().compileTemplates());
      }
    }

    private final SoySauce soySauce;

    private FileSet(SoySauce soySauce) {
      this.soySauce = soySauce;
    }

    @LegacySoyJavaStringCaller
    public TemplateRenderer newRenderer(String templateName) {
      return new TemplateRenderer(soySauce.renderTemplate(templateName));
    }

  }

  /**
   * Base Guice module to use if you want to add custom modules to your soy environment.
   * {@code SoyModule} is already installed as part of this.
   */
  public static final class Module extends AbstractModule {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface SoyFiles {}

    @Override
    protected void configure() {
      install(new SoyDirectives.Module());
    }

    @SoyFiles
    @Provides
    SoyFileSet.Builder provideBuilder(
        Set<SoyFunction> pluginFunctions, Set<SoyPrintDirective> pluginDirectives) {
      return SoyFileSet.builder()
          .addSoyFunctions(pluginFunctions)
          .addSoyPrintDirectives(pluginDirectives);
    }
  }

  private final Renderer delegate;

  /** Use {@link TemplateRenderer.FileSet#builder} to construct an instance. */
  private TemplateRenderer(Renderer delegate) {
    this.delegate = delegate;
  }

  /** Sets data to call the template with. */
  public TemplateRenderer setData(Map<String, ?> data) {
    delegate.setData(data);
    return this;
  }

  /** Sets injected data to call the template with. */
  public TemplateRenderer setInjectedData(Map<String, ?> data) {
    delegate.setIj(data);
    return this;
  }

  /** Renders the template. */
  public String render() {
    return delegate.renderText().get();
  }
}
