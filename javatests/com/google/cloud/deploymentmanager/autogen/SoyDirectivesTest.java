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
