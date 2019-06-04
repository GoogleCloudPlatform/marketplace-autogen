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

import static com.google.cloud.deploymentmanager.autogen.proto.DeployInputSection.Placement.CUSTOM_BOTTOM;
import static com.google.cloud.deploymentmanager.autogen.proto.DeployInputSection.Placement.MAIN;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import com.google.cloud.deploymentmanager.autogen.SoyFunctions.BooleanExpressionDisplayCondition;
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.DependentTiers;
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.DeployInputFieldName;
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.FindDeployInputField;
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.FindDisplayGroup;
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.FindInputTestDefaultValue;
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.FindInputsWithTestDefaultValues;
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.FindVmTier;
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.ListDeployInputFields;
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.SolutionHasGpus;
import com.google.cloud.deploymentmanager.autogen.SoyFunctions.TierPrefixed;
import com.google.cloud.deploymentmanager.autogen.proto.AcceleratorSpec;
import com.google.cloud.deploymentmanager.autogen.proto.BooleanExpression;
import com.google.cloud.deploymentmanager.autogen.proto.BooleanExpression.BooleanDeployInputField;
import com.google.cloud.deploymentmanager.autogen.proto.BooleanExpression.ExternalIpAvailability;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.BooleanCheckbox;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.GroupedBooleanCheckbox;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.GroupedBooleanCheckbox.DisplayGroup;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.IntegerBox;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.StringBox;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSection;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSection.Placement;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSpec;
import com.google.cloud.deploymentmanager.autogen.proto.GceMetadataItem;
import com.google.cloud.deploymentmanager.autogen.proto.GceMetadataItem.TierVmNames;
import com.google.cloud.deploymentmanager.autogen.proto.MultiVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.OptionalInt32;
import com.google.cloud.deploymentmanager.autogen.proto.SingleVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.VmTierSpec;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.ThrowableSubject;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.template.soy.data.SoyList;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.SoyValueConverter;
import com.google.template.soy.shared.restricted.SoyJavaFunction;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.CheckReturnValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests {@link SoyFunctions}. */
@RunWith(JUnit4.class)
public class SoyFunctionsTest {

  @Inject DependentTiers dependentTiers;
  @Inject DeployInputFieldName deployInputFieldName;
  @Inject FindInputsWithTestDefaultValues findInputsWithTestDefaultValues;
  @Inject FindInputTestDefaultValue findInputTestDefaultValue;
  @Inject FindDeployInputField findDeployInputField;
  @Inject FindDisplayGroup findDisplayGroup;
  @Inject FindVmTier findVmTier;
  @Inject TierPrefixed tierPrefixed;
  @Inject ListDeployInputFields listDeployInputFields;
  @Inject BooleanExpressionDisplayCondition booleanExpressionDisplayCondition;
  @Inject SolutionHasGpus solutionHasGpus;

  @Before
  public void setupInjector() {
    Guice.createInjector().injectMembers(this);
  }

  @Test
  public void testDependentTiers_gceMetadataItemsVmNames() {
    List<VmTierSpec> vmTiers =
        ImmutableList.of(
            VmTierSpec.newBuilder().setName("tier0").build(),
            VmTierSpec.newBuilder().setName("tier1").build(),
            VmTierSpec.newBuilder().setName("tier2").build());
    VmTierSpec tier =
        VmTierSpec.newBuilder()
            .addGceMetadataItems(
                GceMetadataItem.newBuilder()
                    .setTierVmNames(TierVmNames.newBuilder().setTier("tier0")))
            .addGceMetadataItems(
                GceMetadataItem.newBuilder()
                    .setTierVmNames(TierVmNames.newBuilder().setTier("tier2")))
            .build();
    assertFunctionCall(dependentTiers, tier, vmTiers)
        .hasIterableResultThat()
        .containsExactly(toSoyValue(vmTiers.get(0)), toSoyValue(vmTiers.get(2)));
  }

  @Test
  public void testDependentTiers_gceMetadataItemsVmNamesWithInvalidTier() {
    List<VmTierSpec> vmTiers =
        ImmutableList.of(
            VmTierSpec.newBuilder().setName("tier0").build(),
            VmTierSpec.newBuilder().setName("tier1").build(),
            VmTierSpec.newBuilder().setName("tier2").build());
    VmTierSpec tier =
        VmTierSpec.newBuilder()
            .addGceMetadataItems(
                GceMetadataItem.newBuilder()
                    .setTierVmNames(TierVmNames.newBuilder().setTier("tier10")))
            .build();
    assertFunctionCall(dependentTiers, tier, vmTiers)
        .throwsExceptionThat(IllegalArgumentException.class)
        .hasMessageThat()
        .startsWith("Unable to find tier");
  }

  @Test
  public void testDeployInputFieldName() {
    assertFunctionCall(
        deployInputFieldName,
        DeployInputField.newBuilder().setName("someField").build())
        .hasResultThat().isEqualTo(toSoyValue("input_someField"));
    assertFunctionCall(deployInputFieldName, "someField")
        .throwsException(ClassCastException.class);
    assertFunctionCall(deployInputFieldName, 13)
        .throwsException(ClassCastException.class);
  }

  @Test
  public void testFindInputsWithTestDefaultValues() {
    DeployInputField nonRequired = DeployInputField.newBuilder()
        .setRequired(false)
        .build();

    IntegerBox intBoxWithDefaultValue = IntegerBox.newBuilder()
        .setDefaultValue(OptionalInt32.newBuilder().setValue(10).build())
        .build();
    DeployInputField requiredIntWithDefaultValue = DeployInputField.newBuilder()
        .setRequired(true)
        .setIntegerBox(intBoxWithDefaultValue)
        .build();

    IntegerBox boxWithoutDefaultValue = IntegerBox.newBuilder()
        .setTestDefaultValue(OptionalInt32.newBuilder().setValue(10).build())
        .build();
    DeployInputField requiredWithoutDefaultValue = DeployInputField.newBuilder()
        .setRequired(true)
        .setName("TruePositive")
        .setIntegerBox(boxWithoutDefaultValue)
        .build();

    StringBox boxWithDefaultValue = StringBox.newBuilder()
        .setDefaultValue("some default")
        .build();
    DeployInputField requiredStringWithDefaultValue = DeployInputField.newBuilder()
        .setRequired(true)
        .setStringBox(boxWithDefaultValue)
        .build();

    DeployInputSection section = DeployInputSection.newBuilder()
        .addFields(nonRequired)
        .addFields(requiredIntWithDefaultValue)
        .addFields(requiredStringWithDefaultValue)
        .addFields(requiredWithoutDefaultValue)
        .build();

    DeployInputSpec spec = DeployInputSpec.newBuilder()
        .addSections(section)
        .build();

    assertFunctionCall(findInputsWithTestDefaultValues, spec)
        .hasIterableResultThat().containsExactly(toSoyValue(requiredWithoutDefaultValue));
  }

  @Test
  public void testFindInputTestDefaultValueForIntegerBox() {
    IntegerBox box = IntegerBox.newBuilder()
        .setTestDefaultValue(OptionalInt32.newBuilder().setValue(2).build())
        .build();
    DeployInputField field = DeployInputField.newBuilder()
        .setIntegerBox(box)
        .build();
    assertFunctionCall(findInputTestDefaultValue, field)
        .hasResultThat().isEqualTo(toSoyValue(2));
  }

  @Test
  public void testFindInputTestDefaultValueForStringBox() {
    StringBox box = StringBox.newBuilder()
        .setTestDefaultValue("my-value")
        .build();
    DeployInputField field = DeployInputField.newBuilder()
        .setStringBox(box)
        .build();
    assertFunctionCall(findInputTestDefaultValue, field)
        .hasResultThat().isEqualTo(toSoyValue("my-value"));
  }

  @Test
  public void testFindInputTestDefaultValueForUnsupportedInput() {
    BooleanCheckbox checkbox = BooleanCheckbox.newBuilder().getDefaultInstanceForType();
    DeployInputField field = DeployInputField.newBuilder()
        .setBooleanCheckbox(checkbox)
        .build();
    assertFunctionCall(findInputTestDefaultValue, field)
        .throwsException(IllegalArgumentException.class);
  }

  @Test
  public void testFindDeployInputField() {
    DeployInputField[] fields = {
        DeployInputField.newBuilder()
            .setName("field0")
            .setIntegerBox(IntegerBox.newBuilder())
            .build(),
        DeployInputField.newBuilder()
            .setName("field1")
            .setGroupedBooleanCheckbox(GroupedBooleanCheckbox.newBuilder())
            .build(),
        DeployInputField.newBuilder()
            .setName("field2")
            .setStringBox(StringBox.newBuilder())
            .build(),
    };
    DeployInputSpec deployInputSpec =
        DeployInputSpec.newBuilder()
            .addSections(
                DeployInputSection.newBuilder()
                    .addFields(fields[0])
                    .addFields(fields[1]))
            .addSections(
                DeployInputSection.newBuilder()
                    .addFields(fields[2]))
            .build();
    assertFunctionCall(findDeployInputField, "field0", deployInputSpec)
        .hasResultThat().isEqualTo(toSoyValue(fields[0]));
    assertFunctionCall(findDeployInputField, "field1", deployInputSpec)
        .hasResultThat().isEqualTo(toSoyValue(fields[1]));
    assertFunctionCall(findDeployInputField, "field2", deployInputSpec)
        .hasResultThat().isEqualTo(toSoyValue(fields[2]));
    assertFunctionCall(findDeployInputField, "field3", deployInputSpec)
        .throwsException(IllegalArgumentException.class);
  }

  @Test
  public void testFindDisplayGroup() {
    DisplayGroup displayGroup0 = DisplayGroup.newBuilder().setName("DISPLAY_GROUP0").build();
    DisplayGroup displayGroup1 = DisplayGroup.newBuilder().setName("DISPLAY_GROUP1").build();
    DeployInputSection.Builder sectionBuilder = DeployInputSection.newBuilder();
    sectionBuilder.addFieldsBuilder()
        .setName("nonGroupedBooleanCheckboxField").setIntegerBox(IntegerBox.newBuilder());
    sectionBuilder.addFieldsBuilder()
        .setName("group0LeadingField")
        .setGroupedBooleanCheckbox(
            GroupedBooleanCheckbox.newBuilder().setDisplayGroup(displayGroup0));
    sectionBuilder.addFieldsBuilder().setName("group0SecondField")
        .setGroupedBooleanCheckbox(GroupedBooleanCheckbox.newBuilder());
    sectionBuilder.addFieldsBuilder()
        .setName("group1LeadingField")
        .setGroupedBooleanCheckbox(
            GroupedBooleanCheckbox.newBuilder().setDisplayGroup(displayGroup1));

    DeployInputSection section = sectionBuilder.build();
    assertFunctionCall(findDisplayGroup, section.getFields(1), section)
        .hasResultThat().isEqualTo(toSoyValue(displayGroup0));
    assertFunctionCall(findDisplayGroup, section.getFields(2), section)
        .hasResultThat().isEqualTo(toSoyValue(displayGroup0));
    assertFunctionCall(findDisplayGroup, section.getFields(3), section)
        .hasResultThat().isEqualTo(toSoyValue(displayGroup1));
  }

  @Test
  public void testFindDisplayGroup_noDisplayGroup() {
    DeployInputSection section = DeployInputSection.newBuilder()
        .addFields(
            DeployInputField.newBuilder().setName("field0")
                .setGroupedBooleanCheckbox(GroupedBooleanCheckbox.newBuilder()))
        .addFields(
            DeployInputField.newBuilder().setName("field1")
                .setGroupedBooleanCheckbox(GroupedBooleanCheckbox.newBuilder()))
        .build();
    assertFunctionCall(findDisplayGroup, section.getFields(0), section)
        .throwsExceptionThat(RuntimeException.class)
        .hasMessageThat().startsWith("Unable to locate display group");
    assertFunctionCall(findDisplayGroup, section.getFields(1), section)
        .throwsExceptionThat(RuntimeException.class)
        .hasMessageThat().startsWith("Unable to locate display group");
  }

  @Test
  public void testFindDisplayGroup_precededByAnotherFieldType() {
    DeployInputSection section = DeployInputSection.newBuilder()
        .addFields(
            DeployInputField.newBuilder().setName("field0")
                .setIntegerBox(IntegerBox.newBuilder()))
        .addFields(
            DeployInputField.newBuilder().setName("field1")
                .setGroupedBooleanCheckbox(GroupedBooleanCheckbox.newBuilder()))
        .build();
    assertFunctionCall(findDisplayGroup, section.getFields(1), section)
        .throwsExceptionThat(RuntimeException.class)
        .hasMessageThat().startsWith("No preceding grouped boolean checkbox field");
  }

  @Test
  public void testFindVmTier_vmTierListAsSecondArg() {
    runTestFindVmTier(Functions.identity());
  }

  @Test
  public void testFindVmTier_mutliVmSpecAsSecondArg() {
    runTestFindVmTier(new Function<List<VmTierSpec>, MultiVmDeploymentPackageSpec>() {
      @Override
      public MultiVmDeploymentPackageSpec apply(List<VmTierSpec> vmTiers) {
        return MultiVmDeploymentPackageSpec.newBuilder().addAllTiers(vmTiers).build();
      }
    });
  }

  private void runTestFindVmTier(Function<List<VmTierSpec>, ?> secondArgSupplier) {
    List<VmTierSpec> vmTiers = ImmutableList.of(
        VmTierSpec.newBuilder().setName("tier0").build(),
        VmTierSpec.newBuilder().setName("tier1").build());
    Object secondArg = secondArgSupplier.apply(vmTiers);
    assertFunctionCall(findVmTier, "tier0", secondArg)
        .hasResultThat().isEqualTo(toSoyValue(vmTiers.get(0)));
    assertFunctionCall(findVmTier, "tier1", secondArg)
        .hasResultThat().isEqualTo(toSoyValue(vmTiers.get(1)));
    assertFunctionCall(findVmTier, "tier2", secondArg)
        .throwsExceptionThat(RuntimeException.class)
        .hasMessageThat().isEqualTo("Unable to locate tier with name tier2");
  }

  @Test
  public void testFindVmTier_invalidSecondArg() {
    assertFunctionCall(findVmTier, "tier0", "tiers")
        .throwsExceptionThat(IllegalArgumentException.class)
        .hasMessageThat().startsWith("Unexpected 2nd arg type");
  }

  @Test
  public void testTierPrefixed() {
    VmTierSpec tier = VmTierSpec.newBuilder().setName("mytier").build();
    assertFunctionCall(tierPrefixed, "someProperty", (Object) null)
        .hasResultThat().isEqualTo(toSoyValue("someProperty"));
    assertFunctionCall(tierPrefixed, "someProperty", (Object) null, "-")
        .hasResultThat().isEqualTo(toSoyValue("someProperty"));
    assertFunctionCall(tierPrefixed, "someProperty", tier)
        .hasResultThat().isEqualTo(toSoyValue("mytier_someProperty"));
    assertFunctionCall(tierPrefixed, "someProperty", tier, "-")
        .hasResultThat().isEqualTo(toSoyValue("mytier-someProperty"));
  }

  @Test
  public void testExtractDeployInputFields() {
    DeployInputField domain = buildStringBoxInput("domain");
    DeployInputField email = buildStringBoxInput("email");
    DeployInputField username = buildStringBoxInput("username");
    DeployInputField additionalDisks = buildIntegerBoxInput("additional-disks");

    DeployInputSection mainSection = buildInputSection("Main", MAIN, domain);
    DeployInputSection userDataSection =
        buildInputSection("User data", MAIN, email, username);
    DeployInputSection otherSection =
        buildInputSection("Other", CUSTOM_BOTTOM, additionalDisks);

    DeployInputSpec inputSpec = DeployInputSpec.newBuilder()
        .addSections(mainSection)
        .addSections(userDataSection)
        .addSections(otherSection)
        .build();

    List<SoyValue> allFields = ImmutableList.of(
        toSoyValue(domain),
        toSoyValue(email),
        toSoyValue(username),
        toSoyValue(additionalDisks));

    assertFunctionCall(listDeployInputFields, inputSpec)
        .hasIterableResultThat().containsExactlyElementsIn(allFields);
  }

  @Test
  public void testFilterInputFieldsByTier() {
    DeployInputField email = buildStringBoxInput("email");
    DeployInputField username = buildStringBoxInput("username");
    DeployInputField additionalDisks = buildIntegerBoxInput("additional-disks");

    DeployInputSection userDataSection =
        buildInputSection("User data", MAIN, email, username);
    DeployInputSection otherSection =
        buildInputSection("Other", CUSTOM_BOTTOM, additionalDisks);

    DeployInputSpec inputSpec = DeployInputSpec.newBuilder()
        .addSections(userDataSection)
        .addSections(otherSection)
        .build();

    VmTierSpec vmTierSpec = VmTierSpec.newBuilder()
        .addGceMetadataItems(
            GceMetadataItem.newBuilder()
                .setKey("some-email")
                .setValueFromDeployInputField("email"))
        .addGceMetadataItems(
            GceMetadataItem.newBuilder()
                .setKey("disks-count")
                .setValueFromDeployInputField("additional-disks"))
        .build();

    assertFunctionCall(listDeployInputFields, inputSpec, vmTierSpec)
        .hasIterableResultThat()
        .containsExactly(toSoyValue(email), toSoyValue(additionalDisks));
  }

  @Test
  public void testBooleanExpressionDisplayConditionForDeployInputField() {
    BooleanDeployInputField field = BooleanDeployInputField.newBuilder()
        .setName("enableXYZ")
        .setNegated(true)
        .build();
    BooleanExpression expression = BooleanExpression.newBuilder()
        .setBooleanDeployInputField(field)
        .build();
    assertFunctionCall(booleanExpressionDisplayCondition, expression)
        .hasResultThat()
        .isEqualTo("!properties().input_enableXYZ");
  }

  @Test
  public void testBooleanExpressionDisplayConditionForExternalIPAvailability_single() {
    BooleanExpression expression = BooleanExpression.newBuilder()
        .setHasExternalIp(ExternalIpAvailability.newBuilder().setNegated(true))
        .build();
    assertFunctionCall(booleanExpressionDisplayCondition, expression)
        .hasResultThat()
        .isEqualTo("properties().externalIP == \"NONE\"");
  }

  @Test
  public void testBooleanExpressionDisplayConditionForExternalIPAvailability_multi() {
    ExternalIpAvailability externalIpAvailability = ExternalIpAvailability.newBuilder()
        .setTier("tier0")
        .setNegated(false)
        .build();
    BooleanExpression expression = BooleanExpression.newBuilder()
        .setHasExternalIp(externalIpAvailability)
        .build();
    List<VmTierSpec> tiersList = ImmutableList.of(
        VmTierSpec.newBuilder().setName("tier0").build(),
        VmTierSpec.newBuilder().setName("tier1").build()
    );
    assertFunctionCall(booleanExpressionDisplayCondition, expression, tiersList)
        .hasResultThat()
        .isEqualTo("properties().tier0_externalIP != \"NONE\"");
  }

  @Test
  public void testSolutionHasGpusWithNoGpuListMultiVm() {
    List<VmTierSpec> vmTiers =
        ImmutableList.of(
            VmTierSpec.newBuilder().setName("tier0").build(),
            VmTierSpec.newBuilder().setName("tier1").build());
    MultiVmDeploymentPackageSpec spec =
        MultiVmDeploymentPackageSpec.newBuilder().addAllTiers(vmTiers).build();

    assertFunctionCall(solutionHasGpus, spec).hasResultThat().isEqualTo(toSoyValue(false));
  }

  @Test
  public void testSolutionHasGpusWithNoGpuListSingleVm() {
    SingleVmDeploymentPackageSpec spec =
        SingleVmDeploymentPackageSpec.newBuilder().getDefaultInstanceForType();

    assertFunctionCall(solutionHasGpus, spec).hasResultThat().isEqualTo(toSoyValue(false));
  }

  @Test
  public void testSolutionHasGpusWithGpuListMultiVm() {
    List<String> types = Arrays.asList("nvidia-tesla-p100");
    AcceleratorSpec acceleratorSpec = AcceleratorSpec.newBuilder().addAllTypes(types).build();
    List<VmTierSpec> vmTiers =
        ImmutableList.of(
            VmTierSpec.newBuilder().setName("tier0").addAccelerators(acceleratorSpec).build(),
            VmTierSpec.newBuilder().setName("tier1").build());
    MultiVmDeploymentPackageSpec spec =
        MultiVmDeploymentPackageSpec.newBuilder().addAllTiers(vmTiers).build();

    assertFunctionCall(solutionHasGpus, spec).hasResultThat().isEqualTo(toSoyValue(true));
  }

  @Test
  public void testSolutionHasGpusWithGpuListSingleVm() {
    List<String> types = Arrays.asList("nvidia-tesla-p100");
    AcceleratorSpec acceleratorSpec = AcceleratorSpec.newBuilder().addAllTypes(types).build();
    SingleVmDeploymentPackageSpec spec =
        SingleVmDeploymentPackageSpec.newBuilder().addAccelerators(acceleratorSpec).build();

    assertFunctionCall(solutionHasGpus, spec).hasResultThat().isEqualTo(toSoyValue(true));
  }

  private DeployInputField buildStringBoxInput(String fieldName) {
    return DeployInputField.newBuilder()
        .setName(fieldName).setStringBox(StringBox.newBuilder()).build();
  }

  private DeployInputField buildIntegerBoxInput(String fieldName) {
    return DeployInputField.newBuilder()
        .setName(fieldName).setIntegerBox(IntegerBox.newBuilder()).build();
  }

  private DeployInputSection buildInputSection(
      String name, Placement placement, DeployInputField ... fields) {
    return DeployInputSection.newBuilder()
        .setName(name)
        .setPlacement(placement)
        .addAllFields(ImmutableList.copyOf(fields))
        .build();
  }

  private class ResultAssert {
    private final Callable<SoyValue> callable;

    ResultAssert(Callable<SoyValue> callable) {
      this.callable = callable;
    }

    @CheckReturnValue
    ThrowableSubject throwsExceptionThat(Class<? extends Throwable> throwableClass) {
      try {
        callable.call();
      } catch (Throwable throwable) {
        assertWithMessage("Expected exception of type " + throwableClass.getName())
            .that(throwable).isInstanceOf(throwableClass);
        return assertThat(throwable);
      }
      fail("Should have thrown exception of type " + throwableClass.getName());
      return null;
    }

    void throwsException(Class<? extends Throwable> throwableClass) {
      throwsExceptionThat(throwableClass).isNotNull();
    }

    @CheckReturnValue
    Subject hasResultThat() {
      try {
        return assertThat(callable.call());
      } catch (Exception e) {
        throw new AssertionError("Unexpected exception", e);
      }
    }

    @CheckReturnValue
    IterableSubject hasIterableResultThat() {
      try {
        return assertThat(((SoyList) callable.call()).asResolvedJavaList());
      } catch (Exception e) {
        throw new AssertionError("Unexpected exception", e);
      }
    }
  }

  private ResultAssert assertFunctionCall(final SoyJavaFunction function, final Object... args) {
    return new ResultAssert(
        new Callable<SoyValue>() {
          @Override
          public SoyValue call() throws Exception {
            ImmutableList.Builder<SoyValue> argsData = ImmutableList.builder();
            for (Object arg : args) {
              argsData.add(SoyValueConverter.INSTANCE.convert(arg).resolve());
            }
            return function.computeForJava(argsData.build());
          }
        });
  }

  private SoyValue toSoyValue(Object displayGroup0) {
    return SoyValueConverter.INSTANCE.convert(displayGroup0).resolve();
  }
}
