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

import com.google.cloud.deploymentmanager.autogen.proto.VmTierSpec;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.template.soy.data.SoyProtoValue;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.restricted.NullData;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.shared.restricted.SoyJavaPrintDirective;
import com.google.template.soy.shared.restricted.SoyPrintDirective;
import java.util.List;
import java.util.Set;

/**
 * Adds additional soy print directives to support our templating.
 */
final class SoyDirectives {

  static final class Module extends AbstractModule {

    @Override
    protected void configure() {
      Multibinder<SoyPrintDirective> soyDirectiveSetBinder =
          Multibinder.newSetBinder(binder(), SoyPrintDirective.class);
      soyDirectiveSetBinder.addBinding().to(TierPrefixed.class);
    }
  }

  private abstract static class BaseDirective implements SoyJavaPrintDirective {
    @Override
    public Set<Integer> getValidArgsSizes() {
      return ImmutableSet.of(0);
    }

    public boolean shouldCancelAutoescape() {
      return false;
    }
  }

  /** Prefix with the specified tier's name. */
  @Singleton
  static class TierPrefixed extends BaseDirective {
    @Override
    public String getName() {
      return "|tierprefixed";
    }

    @Override
    public Set<Integer> getValidArgsSizes() {
      return ImmutableSet.of(1, 2);
    }

    @Override
    public SoyValue applyForJava(SoyValue value, List<SoyValue> args) {
      if (args.get(0) instanceof NullData) {
        return value;
      }
      String joiner = "_";
      if (args.size() == 2) {
        joiner = args.get(1).coerceToString();
      }
      VmTierSpec tier = (VmTierSpec) (((SoyProtoValue) args.get(0)).getProto());
      return StringData.forValue(
          String.format("%s%s%s", tier.getName(), joiner, value.coerceToString()));
    }
  }
}
