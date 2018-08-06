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

import com.google.cloud.deploymentmanager.autogen.SoyDirectives.TierPrefixed;
import com.google.cloud.deploymentmanager.autogen.proto.VmTierSpec;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.SoyValueConverter;
import com.google.template.soy.shared.restricted.SoyJavaPrintDirective;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests {@link SoyDirectives}. */
@RunWith(JUnit4.class)
public class SoyDirectivesTest {

  @Inject TierPrefixed tierPrefixed;

  @Before
  public void setupInjector() {
    Guice.createInjector().injectMembers(this);
  }

  @Test
  public void testTierPrefixed() {
    VmTierSpec tier = VmTierSpec.newBuilder().setName("mytier").build();
    assertOutput("someProperty", "someProperty", tierPrefixed, (Object) null);
    assertOutput("someProperty", "someProperty", tierPrefixed, (Object) null, "-");
    assertOutput("mytier_someProperty", "someProperty", tierPrefixed, tier);
    assertOutput("mytier-someProperty", "someProperty", tierPrefixed, tier, "-");
  }

  private void assertOutput(
      Object expected, SoyValue value, SoyJavaPrintDirective directive, Object... args) {
    ImmutableList.Builder<SoyValue> argsData = ImmutableList.builder();
    for (Object arg : args) {
      argsData.add(SoyValueConverter.INSTANCE.convert(arg).resolve());
    }
    assertThat(directive.applyForJava(value, argsData.build()))
        .isEqualTo(SoyValueConverter.INSTANCE.convert(expected).resolve());
  }

  private void assertOutput(
      Object expected, Object value, SoyJavaPrintDirective directive, Object... args) {
    assertOutput(expected, SoyValueConverter.INSTANCE.convert(value).resolve(), directive, args);
  }
}
