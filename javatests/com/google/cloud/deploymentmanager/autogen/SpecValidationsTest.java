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

import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validate;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateAccelerators;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateApplicationStatus;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateDeployInput;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateDeployInputFields;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateExternalIp;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateMultiVmBooleanExpression;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateMultiVmGceMetadataItems;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateMultiVmPasswords;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateMultiVmPostDeployInfo;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateSingleVmBooleanExpression;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateSingleVmGceMetadataItems;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateSingleVmPasswords;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateSingleVmPostDeployInfo;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateStackdriver;
import static com.google.cloud.deploymentmanager.autogen.SpecValidations.validateStartupScript;

import com.google.cloud.deploymentmanager.autogen.proto.AcceleratorSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ApplicationStatusSpec;
import com.google.cloud.deploymentmanager.autogen.proto.BooleanExpression;
import com.google.cloud.deploymentmanager.autogen.proto.BooleanExpression.BooleanDeployInputField;
import com.google.cloud.deploymentmanager.autogen.proto.BooleanExpression.ExternalIpAvailability;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.EmailBox;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSection;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSection.Placement;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSpec;
import com.google.cloud.deploymentmanager.autogen.proto.DiskSpec;
import com.google.cloud.deploymentmanager.autogen.proto.DiskSpec.DeviceName;
import com.google.cloud.deploymentmanager.autogen.proto.DiskSpec.DiskSize;
import com.google.cloud.deploymentmanager.autogen.proto.DiskSpec.DiskType;
import com.google.cloud.deploymentmanager.autogen.proto.ExternalIpSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ExternalIpSpec.Type;
import com.google.cloud.deploymentmanager.autogen.proto.FirewallRuleSpec;
import com.google.cloud.deploymentmanager.autogen.proto.FirewallRuleSpec.TrafficSource;
import com.google.cloud.deploymentmanager.autogen.proto.GceMetadataItem;
import com.google.cloud.deploymentmanager.autogen.proto.GceMetadataItem.TierVmNames;
import com.google.cloud.deploymentmanager.autogen.proto.GceStartupScriptSpec;
import com.google.cloud.deploymentmanager.autogen.proto.GcpAuthScopeSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ImageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.InstanceUrlSpec;
import com.google.cloud.deploymentmanager.autogen.proto.MachineTypeSpec;
import com.google.cloud.deploymentmanager.autogen.proto.MachineTypeSpec.MachineType;
import com.google.cloud.deploymentmanager.autogen.proto.MachineTypeSpec.MachineTypeConstraint;
import com.google.cloud.deploymentmanager.autogen.proto.MultiVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.NetworkInterfacesSpec;
import com.google.cloud.deploymentmanager.autogen.proto.PasswordSpec;
import com.google.cloud.deploymentmanager.autogen.proto.PostDeployInfo;
import com.google.cloud.deploymentmanager.autogen.proto.PostDeployInfo.ConnectToInstanceSpec;
import com.google.cloud.deploymentmanager.autogen.proto.SingleVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.StackdriverSpec;
import com.google.cloud.deploymentmanager.autogen.proto.TierVmInstance;
import com.google.cloud.deploymentmanager.autogen.proto.VmTierSpec;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests {@link SpecValidations}. */
@RunWith(JUnit4.class)
public class SpecValidationsTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void missingAnyTier() {
    expectIllegalArgumentException("At least one tier must be specified");
    validate(MultiVmDeploymentPackageSpec.getDefaultInstance());
  }

  @Test
  public void missingImage_single() {
    expectIllegalArgumentException("At least one image must be specified");
    validate(SingleVmDeploymentPackageSpec.getDefaultInstance());
  }

  @Test
  public void missingImage_multi() {
    expectIllegalArgumentException("At least one image must be specified");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpec();
    builder.getTiersBuilder(0).clearImages();
    builder.getTiersBuilder(1).clearImages();
    validate(builder.build());
  }

  @Test
  public void defaultMachineTypeMustBeValid_single() {
    expectIllegalArgumentException("Default machine type name must be valid");
    validate(newSingleSpecWithDefaults().setMachineType(invalidMachineTypeSpec()).build());
  }

  @Test
  public void defaultMachineTypeMustBeValid_multi() {
    expectIllegalArgumentException("Default machine type name must be valid");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(0).setMachineType(invalidMachineTypeSpec());
    validate(builder.build());
  }

  @Test
  public void validDefaultMachineType() {
    validate(newSingleSpecWithDefaults().setMachineType(validMachineTypeSpec()).build());
  }

  @Test
  public void validDefaultMachineType_multi() {
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(0).setMachineType(validMachineTypeSpec());
    builder.getTiersBuilder(1).setMachineType(validMachineTypeSpec());
    validate(builder.build());
  }

  @Test
  public void defaultMachineTypeMustBeValidForAllTiers() {
    expectIllegalArgumentException("Default machine type name must be valid");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(0).setMachineType(validMachineTypeSpec());
    builder.getTiersBuilder(1).setMachineType(invalidMachineTypeSpec());
    validate(builder.build());
  }

  @Test
  public void minimumCpuMustBeNonnegative_single() {
    expectIllegalArgumentException("Minimum CPUs must be nonnegative");
    validate(newSingleSpecWithDefaults().setMachineType(machineTypeSpecWithNegativeCpu()).build());
  }

  @Test
  public void minimumCpuMustBeNonnegative_multi() {
    expectIllegalArgumentException("Minimum CPUs must be nonnegative");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).setMachineType(machineTypeSpecWithNegativeCpu());
    validate(builder.build());
  }

  @Test
  public void minimumRamMustBeNonnegative_single() {
    expectIllegalArgumentException("Minimum RAM must be nonnegative");
    validate(newSingleSpecWithDefaults().setMachineType(machineTypeSpecWithNegativeRam()).build());
  }

  @Test
  public void minimumRamMustBeNonnegative_multi() {
    expectIllegalArgumentException("Minimum RAM must be nonnegative");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).setMachineType(machineTypeSpecWithNegativeRam());
    validate(builder.build());
  }

  @Test
  public void validMinimumCpuAndRamUnset_single() {
    validate(newSingleSpecWithDefaults().setMachineType(machineTypeSpecWithEmptyConstraint()).build());
  }

  @Test
  public void validMinimumCpuAndRamUnset_multi() {
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).setMachineType(machineTypeSpecWithEmptyConstraint());
    validate(builder.build());
  }

  @Test
  public void validMinimumCpuAndRamZero_single() {
    validate(newSingleSpecWithDefaults().setMachineType(machineTypeSpecWithZeroes()).build());
  }

  @Test
  public void validMinimumCpuAndRamZero_multi() {
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).setMachineType(machineTypeSpecWithZeroes());
    validate(builder.build());
  }

  @Test
  public void validMinimumCpuAndRam_single() {
    validate(newSingleSpecWithDefaults().setMachineType(machineTypeSpecWithValidMinimums()).build());
  }

  @Test
  public void validMinimumCpuAndRam_multi() {
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(0).setMachineType(machineTypeSpecWithValidMinimums());
    builder.getTiersBuilder(1).setMachineType(machineTypeSpecWithValidMinimums());
    validate(builder.build());
  }

  @Test
  public void bootDiskTypeMustBeSpecified_single() {
    expectIllegalArgumentException("Disk type must be specified");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getBootDiskBuilder().clearDiskType();
    validate(builder.build());
  }

  @Test
  public void bootDiskTypeMustBeSpecified_multi() {
    expectIllegalArgumentException("Disk type must be specified");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getBootDiskBuilder().clearDiskType();
    validate(builder.build());
  }

  @Test
  public void additionalDiskTypeMustBeSpecified_single() {
    expectIllegalArgumentException("Disk type must be specified");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getAdditionalDisksBuilderList().get(0).clearDiskType();
    validate(builder.build());
  }

  @Test
  public void additionalDiskTypeMustBeSpecified_multi() {
    expectIllegalArgumentException("Disk type must be specified");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getAdditionalDisksBuilderList().get(0).clearDiskType();
    validate(builder.build());
  }

  @Test
  public void invalidDefaultBootDiskType_single() {
    expectIllegalArgumentException("A valid default disk type must be specified");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getBootDiskBuilder().getDiskTypeBuilder().clearDefaultType();
    validate(builder.build());
  }

  @Test
  public void invalidDefaultBootDiskType_multi() {
    expectIllegalArgumentException("A valid default disk type must be specified");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getBootDiskBuilder().getDiskTypeBuilder().clearDefaultType();
    validate(builder.build());
  }

  @Test
  public void invalidDefaultAdditionalDiskType_single() {
    expectIllegalArgumentException("A valid default disk type must be specified");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getAdditionalDisksBuilderList().get(0).getDiskTypeBuilder().clearDefaultType();
    validate(builder.build());
  }

  @Test
  public void invalidDefaultAdditionalDiskType_multi() {
    expectIllegalArgumentException("A valid default disk type must be specified");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1)
        .getAdditionalDisksBuilderList().get(0)
        .getDiskTypeBuilder().clearDefaultType();
    validate(builder.build());
  }

  @Test
  public void bootDiskSizeMustBeSpecified_single() {
    expectIllegalArgumentException("Disk size must be specified");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getBootDiskBuilder().clearDiskSize();
    validate(builder.build());
  }

  @Test
  public void bootDiskSizeMustBeSpecified_multi() {
    expectIllegalArgumentException("Disk size must be specified");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getBootDiskBuilder().clearDiskSize();
    validate(builder.build());
  }

  @Test
  public void additionalDiskSizeMustBeSpecified_single() {
    expectIllegalArgumentException("Disk size must be specified");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getAdditionalDisksBuilderList().get(0).clearDiskSize();
    validate(builder.build());
  }

  @Test
  public void additionalDiskSizeMustBeSpecified_multi() {
    expectIllegalArgumentException("Disk size must be specified");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getAdditionalDisksBuilderList().get(0).clearDiskSize();
    validate(builder.build());
  }

  @Test
  public void invalidBootDiskSize_single() {
    expectIllegalArgumentException("A valid default disk size must be specified");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getBootDiskBuilder().getDiskSizeBuilder().clearDefaultSizeGb();
    validate(builder.build());
  }

  @Test
  public void invalidBootDiskSize_multi() {
    expectIllegalArgumentException("A valid default disk size must be specified");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getBootDiskBuilder().getDiskSizeBuilder().clearDefaultSizeGb();
    validate(builder.build());
  }

  @Test
  public void invalidAdditionalDiskSize_single() {
    expectIllegalArgumentException("A valid default disk size must be specified");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getAdditionalDisksBuilderList().get(0).getDiskSizeBuilder().clearDefaultSizeGb();
    validate(builder.build());
  }

  @Test
  public void invalidAdditionalDiskSize_multi() {
    expectIllegalArgumentException("A valid default disk size must be specified");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1)
        .getAdditionalDisksBuilderList().get(0)
        .getDiskSizeBuilder().clearDefaultSizeGb();
    validate(builder.build());
  }

  @Test
  public void invalidBootDiskDisplayLabel_single() {
    expectIllegalArgumentException("A valid disk display label must be specified");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getBootDiskBuilder().setDisplayLabel("");
    validate(builder.build());
  }

  @Test
  public void invalidBootDiskDisplayLabel_multi() {
    expectIllegalArgumentException("A valid disk display label must be specified");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getBootDiskBuilder().setDisplayLabel("");
    validate(builder.build());
  }

  @Test
  public void invalidAdditionalDiskDisplayLabel_single() {
    expectIllegalArgumentException("A valid disk display label must be specified");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getAdditionalDisksBuilderList().get(0).setDisplayLabel("");
    validate(builder.build());
  }

  @Test
  public void invalidAdditionalDiskDisplayLabel_multi() {
    expectIllegalArgumentException("A valid disk display label must be specified");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getAdditionalDisksBuilderList().get(0).setDisplayLabel("");
    validate(builder.build());
  }

  @Test
  public void validBootDisk_single() {
    validate(newSingleSpecWithDefaults().setBootDisk(validBootDiskSpec()).build());
  }

  @Test
  public void validBootDisk_multi() {
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(0).setBootDisk(validBootDiskSpec());
    builder.getTiersBuilder(1).setBootDisk(validBootDiskSpec());
    validate(builder.build());
  }

  @Test
  public void validAdditionalDisks_single() {
    SingleVmDeploymentPackageSpec.Builder spec = newSingleSpecWithDefaults();
    for (DiskSpec disk : validAdditionalDisksSpec()) {
      spec = spec.addAdditionalDisks(disk);
    }
    validate(spec.build());
  }

  @Test
  public void validAdditionalDisks_multi() {
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    for (DiskSpec disk : validAdditionalDisksSpec()) {
      builder.getTiersBuilder(0).addAdditionalDisks(disk);
      builder.getTiersBuilder(1).addAdditionalDisks(disk);
    }
    validate(builder.build());
  }

  @Test
  public void firewallRuleMustHaveValidProtocol_single() {
    expectIllegalArgumentException("A firewall rule must have a valid protocol");
    validate(newSingleSpecWithDefaults().addFirewallRules(firewallRuleWithoutProtocol()).build());
  }

  @Test
  public void firewallRuleMustHaveValidProtocol_multi() {
    expectIllegalArgumentException("A firewall rule must have a valid protocol");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(0).addFirewallRules(firewallRuleWithoutProtocol());
    builder.getTiersBuilder(1).addFirewallRules(firewallRuleWithoutProtocol());
    validate(builder.build());
  }

  @Test
  public void firewallRuleMustHaveValidSource_single() {
    expectIllegalArgumentException("A firewall rule must have a valid source");
    validate(
        newSingleSpecWithDefaults()
            .addFirewallRules(firewallRuleWithoutAllowedSource())
            .build());
  }

  @Test
  public void allFirewallRulesMustBeValid_single() {
    expectIllegalArgumentException("A firewall rule must have a valid protocol");
    validate(
        newSingleSpecWithDefaults()
            .addFirewallRules(validHttpFirewallRule())
            .addFirewallRules(firewallRuleWithoutProtocol())
            .build());
  }

  @Test
  public void validFirewallRules_single() {
    ImmutableList<FirewallRuleSpec> firewallRules =
        ImmutableList.of(
            validHttpFirewallRule(),
            validHttpsFirewallRule());
    validate(newSingleSpecWithDefaults().addAllFirewallRules(firewallRules).build());
  }

  @Test
  public void validFirewallRules_multi() {
    ImmutableList<FirewallRuleSpec> firewallRules =
        ImmutableList.of(
            validHttpFirewallRule(),
            validHttpsFirewallRule(),
            firewallRuleWithDeploymentSource());
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(0).addAllFirewallRules(firewallRules);
    builder.getTiersBuilder(1).addAllFirewallRules(firewallRules.reverse());
    validate(builder.build());
  }

  @Test
  public void passwordMustHaveMetadataKey_single() {
    expectIllegalArgumentException("Password must have a valid metadata key");
    validateSingleVmPasswords(ImmutableList.of(passwordSpecWithoutMetadataKey()));
  }

  @Test
  public void passwordMustHaveMetadataKey_multi() {
    expectIllegalArgumentException("Password must have a valid metadata key");
    validateMultiVmPasswords(ImmutableList.of(passwordSpecWithoutMetadataKey()));
  }

  @Test
  public void passwordMustHaveLength_single() {
    expectIllegalArgumentException("Password must have a valid length");
    validateSingleVmPasswords(ImmutableList.of(passwordSpecWithoutLength()));
  }

  @Test
  public void passwordMustHaveLength_multi() {
    expectIllegalArgumentException("Password must have a valid length");
    validateMultiVmPasswords(ImmutableList.of(passwordSpecWithoutLength()));
  }

  @Test
  public void passwordMustHaveDisplayLabel_single() {
    expectIllegalArgumentException("Password must have a valid display label");
    validateSingleVmPasswords(ImmutableList.of(passwordSpecWithoutDisplayLabel()));
  }

  @Test
  public void passwordMustHaveDisplayLabel_multi() {
    expectIllegalArgumentException("Password must have a valid display label");
    validateMultiVmPasswords(ImmutableList.of(passwordSpecWithoutDisplayLabel()));
  }

  @Test
  public void passwordsCannotShareMetadataKeys_single() {
    expectIllegalArgumentException("Metadata key 'password-key' is not unique");
    validate(
        newSingleSpecWithDefaults()
            .addPasswords(validPasswordSpec())
            .addPasswords(validPasswordSpec())
            .build());
  }

  @Test
  public void passwordsCannotShareMetadataKeys_multi() {
    expectIllegalArgumentException("Metadata key 'password-key' is not unique");
    validate(
        newMultiSpecWithDefaults()
            .addPasswords(validPasswordSpec())
            .addPasswords(validPasswordSpec())
            .build());
  }

  @Test
  public void passwordMetadataKeysCannotBeInBlacklist_single() {
    expectIllegalArgumentException(Matchers.startsWith("Password metadata key cannot be one of ["));
    List<PasswordSpec> passwords = ImmutableList.of(passwordSpecWithBlacklistedMetadataKey());
    validateSingleVmPasswords(passwords);
  }

  @Test
  public void passwordMetadataKeysCannotBeInBlacklist_multi() {
    expectIllegalArgumentException(Matchers.startsWith("Password metadata key cannot be one of ["));
    List<PasswordSpec> passwords = ImmutableList.of(passwordSpecWithBlacklistedMetadataKey());
    validateMultiVmPasswords(passwords);
  }

  @Test
  public void passwordGeneratedConditionallyMustHaveValidBooleanExpression_input_single() {
    expectIllegalArgumentException(
        "Boolean expression for deploy input field must specify the field's name");
    validate(
        newSingleSpecWithDefaults()
            .addPasswords(passwordSpecWithInvalidGenerateIfConditionForDeployInput())
            .build());
  }

  @Test
  public void passwordGeneratedConditionallyMustHaveValidBooleanExpression_input_multi() {
    expectIllegalArgumentException(
        "Boolean expression for deploy input field must specify the field's name");
    validate(
        newMultiSpecWithDefaults()
            .addPasswords(passwordSpecWithInvalidGenerateIfConditionForDeployInput())
            .build());
  }

  @Test
  public void passwordGeneratedConditionallyMustHaveValidBooleanExpression_externalIp_single() {
    expectIllegalArgumentException("Tier attribute must not be specified for has_external_ip "
        + "boolean expression in a single VM's spec");
    validate(
        newSingleSpecWithDefaults()
            .addPasswords(passwordSpecWithInvalidGenerateIfConditionForExternalIp_single())
            .build());
  }

  @Test
  public void passwordGeneratedConditionallyMustHaveValidBooleanExpression_externalIp_multi() {
    expectIllegalArgumentException("Tier attribute must be specified for has_external_ip "
        + "boolean expression in a multi VM's spec");
    validate(
        newMultiSpecWithDefaults()
            .addPasswords(passwordSpecWithInvalidGenerateIfConditionForExternalIp_multi())
            .build());
  }

  @Test
  public void validPasswords_single() {
    ImmutableList<PasswordSpec> passwordSpecs = ImmutableList.<PasswordSpec>builder()
        .addAll(commonValidPasswordSpecs())
        .add(passwordSpecWithValidGenerateIfConditionForExternalIp_single())
        .build();
    validateSingleVmPasswords(passwordSpecs);
  }

  @Test
  public void validPasswords_multi() {
    ImmutableList<PasswordSpec> passwordSpecs = ImmutableList.<PasswordSpec>builder()
        .addAll(commonValidPasswordSpecs())
        .add(passwordSpecWithValidGenerateIfConditionForExternalIp_multi())
        .build();
    validateMultiVmPasswords(passwordSpecs);
  }

  @Test
  public void gcpAuthScopeMustHaveValidScope_single() {
    expectIllegalArgumentException("The GcpAuthScopeSpec must have a valid scope value");
    validate(
        newSingleSpecWithDefaults()
            .addGcpAuthScopes(GcpAuthScopeSpec.getDefaultInstance())
            .build());
  }

  @Test
  public void gcpAuthScopeMustHaveValidScope_multi() {
    expectIllegalArgumentException("The GcpAuthScopeSpec must have a valid scope value");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).addGcpAuthScopes(GcpAuthScopeSpec.getDefaultInstance());
    validate(builder.build());
  }

  @Test
  public void gcpAuthScopeMustNotBeDuplicated_single() {
    expectIllegalArgumentException("Duplicate scope in GcpAuthScopeSpec list: COMPUTE");
    validate(
        newSingleSpecWithDefaults()
            .addGcpAuthScopes(computeGcpAuthScopeSpec())
            .addGcpAuthScopes(computeGcpAuthScopeSpec())
            .build());
  }

  @Test
  public void gcpAuthScopeMustNotBeDuplicated_multi() {
    expectIllegalArgumentException("Duplicate scope in GcpAuthScopeSpec list: COMPUTE");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder
        .getTiersBuilder(1)
        .addGcpAuthScopes(computeGcpAuthScopeSpec())
        .addGcpAuthScopes(computeGcpAuthScopeSpec());
    validate(builder.build());
  }

  @Test
  public void allGcpAuthScopesMustBeValid_single() {
    expectIllegalArgumentException("The GcpAuthScopeSpec must have a valid scope value");
    validate(
        newSingleSpecWithDefaults()
            .addGcpAuthScopes(computeGcpAuthScopeSpec())
            .addGcpAuthScopes(GcpAuthScopeSpec.getDefaultInstance())
            .build());
  }

  @Test
  public void allGcpAuthScopesMustBeValid_multi() {
    expectIllegalArgumentException("The GcpAuthScopeSpec must have a valid scope value");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder
        .getTiersBuilder(1)
        .addGcpAuthScopes(computeGcpAuthScopeSpec())
        .addGcpAuthScopes(GcpAuthScopeSpec.getDefaultInstance());
    validate(builder.build());
  }

  @Test
  public void validGcpAuthScopes() {
    ImmutableList<GcpAuthScopeSpec> authScopes =
        ImmutableList.of(
            computeGcpAuthScopeSpec(),
            computeReadGcpAuthScopeSpec());
    validate(newSingleSpecWithDefaults().addAllGcpAuthScopes(authScopes).build());

    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(0).addAllGcpAuthScopes(authScopes);
    builder.getTiersBuilder(1).addAllGcpAuthScopes(authScopes.reverse());
    validate(builder.build());
  }

  @Test
  public void adminUrlMustHaveScheme_single() {
    expectIllegalArgumentException("A scheme is required for Admin URL");
    validate(newSingleSpecWithDefaults().setAdminUrl(InstanceUrlSpec.getDefaultInstance()).build());
  }

  @Test
  public void adminUrlMustHaveScheme_multi() {
    expectIllegalArgumentException("A scheme is required for Admin URL");
    validate(newMultiSpecWithDefaults().setAdminUrl(InstanceUrlSpec.getDefaultInstance()).build());
  }

  @Test
  public void adminUrlMustSpecifyTierVmForMulti() {
    expectIllegalArgumentException("A tier VM is required for Admin URL");
    validate(newMultiSpecWithDefaults().setAdminUrl(validSingleInstanceUrl()).build());
  }

  @Test
  public void adminUrlTierVmMustHaveTier() {
    expectIllegalArgumentException("Tier VM must have a valid tier for Admin URL");
    validate(
        newMultiSpecWithDefaults()
            .setAdminUrl(multiInstanceUrlWithTierVmButNoTier())
            .build());
  }

  @Test
  public void validAdminUrl_single() {
    validate(newSingleSpecWithDefaults().setAdminUrl(validSingleInstanceUrl()).build());
  }

  @Test
  public void validAdminUrl_multi() {
    validate(newMultiSpecWithDefaults().setAdminUrl(validMultiInstanceUrl()).build());
  }

  @Test
  public void siteUrlMustHaveScheme_single() {
    expectIllegalArgumentException("A scheme is required for Site URL");
    validate(newSingleSpecWithDefaults().setSiteUrl(InstanceUrlSpec.getDefaultInstance()).build());
  }

  @Test
  public void siteUrlMustHaveScheme_multi() {
    expectIllegalArgumentException("A scheme is required for Site URL");
    validate(newMultiSpecWithDefaults().setSiteUrl(InstanceUrlSpec.getDefaultInstance()).build());
  }


  @Test
  public void siteUrlMustSpecifyTierVmForMulti() {
    expectIllegalArgumentException("A tier VM is required for Site URL");
    validate(newMultiSpecWithDefaults().setSiteUrl(validSingleInstanceUrl()).build());
  }

  @Test
  public void siteUrlTierVmMustHaveTier() {
    expectIllegalArgumentException("Tier VM must have a valid tier for Site URL");
    validate(
        newMultiSpecWithDefaults()
            .setSiteUrl(multiInstanceUrlWithTierVmButNoTier())
            .build());
  }

  @Test
  public void validSiteUrl_single() {
    validate(newSingleSpecWithDefaults().setSiteUrl(validSingleInstanceUrl()).build());
  }

  @Test
  public void validSiteUrl_multi() {
    validate(newMultiSpecWithDefaults().setSiteUrl(validMultiInstanceUrl()).build());
  }

  @Test
  public void connectButtonMustNotSpecifyTierVm_single() {
    expectIllegalArgumentException(
        "Tier VM must not be specified for a single vm spec");

    ConnectToInstanceSpec button = ConnectToInstanceSpec
        .newBuilder(validSingleInstanceConnectButton())
        .setTierVm(TierVmInstance.newBuilder().setTier("tier0"))
        .build();

    validate(newSingleSpecWithDefaults()
        .setPostDeploy(PostDeployInfo.newBuilder().setConnectButton(button))
        .build());
  }

  @Test
  public void connectButtonMustSpecifyTierVm_multi() {
    expectIllegalArgumentException(
        "Multi-vm connect button spec must specify valid tier name");

    ConnectToInstanceSpec button = ConnectToInstanceSpec.newBuilder().build();

    validate(newMultiSpecWithDefaults()
        .setPostDeploy(PostDeployInfo.newBuilder().setConnectButton(button))
        .build());
  }

  @Test
  public void connectButtonMustSpecifyNonEmptyTierVm_multi() {
    expectIllegalArgumentException(
        "Multi-vm connect button spec must specify valid tier name");

    ConnectToInstanceSpec button = ConnectToInstanceSpec.newBuilder()
        .setTierVm(TierVmInstance.newBuilder().setTier(""))
        .build();

    validate(newMultiSpecWithDefaults()
        .setPostDeploy(PostDeployInfo.newBuilder().setConnectButton(button))
        .build());
  }

  @Test
  public void validConnectButtonMayHaveEmptyDisplayLabel() {
    PostDeployInfo singleVmPostDeployInfo = buildPostDeployInfoWithConnectButton(
        validSingleInstanceConnectButton(""), "non-empty label");
    validate(newSingleSpecWithDefaults()
        .setPostDeploy(singleVmPostDeployInfo)
        .build());

    PostDeployInfo multiVmPostDeployInfo = buildPostDeployInfoWithConnectButton(
        validMultiInstanceConnectButton(""), "non-empty label");
    validate(newMultiSpecWithDefaults()
        .setPostDeploy(multiVmPostDeployInfo)
        .build());
  }

  @Test
  public void validConnectButtonMayHaveEmptyDeprecatedConnectButtonLabel() {
    PostDeployInfo singleVmPostDeployInfo = buildPostDeployInfoWithConnectButton(
        validSingleInstanceConnectButton("non-empty label"), "");
    validate(newSingleSpecWithDefaults()
        .setPostDeploy(singleVmPostDeployInfo)
        .build());

    PostDeployInfo multiVmPostDeployInfo = buildPostDeployInfoWithConnectButton(
        validMultiInstanceConnectButton("non-empty label"), "");
    validate(newMultiSpecWithDefaults()
        .setPostDeploy(multiVmPostDeployInfo)
        .build());
  }

  @Test
  public void connectButtonDisplayLabelsSpecsMustBeTheSameIfBothNotEmpty_single() {
    expectIllegalArgumentException(
        "At most only one of connect button's display_label and post deploy's connect_button_label "
            + "can be specified, or they must have the same value");

    PostDeployInfo postDeployInfo = buildPostDeployInfoWithConnectButton(
        validSingleInstanceConnectButton("First label"),
        "Second label");

    validate(newSingleSpecWithDefaults()
        .setPostDeploy(postDeployInfo)
        .build());
  }

  @Test
  public void connectButtonDisplayLabelsSpecsMustBeTheSameIfBothNotEmpty_multi() {
    expectIllegalArgumentException(
        "At most only one of connect button's display_label and post deploy's connect_button_label "
            + "can be specified, or they must have the same value");

    PostDeployInfo postDeployInfo = buildPostDeployInfoWithConnectButton(
        validMultiInstanceConnectButton("First label"),
        "Second label");

    validate(newMultiSpecWithDefaults()
        .setPostDeploy(postDeployInfo)
        .build());
  }

  @Test
  public void validConnectButton_single() {
    PostDeployInfo postDeployInfo = PostDeployInfo.newBuilder()
        .setConnectButton(validSingleInstanceConnectButton())
        .build();

    validate(newSingleSpecWithDefaults()
        .setPostDeploy(postDeployInfo)
        .build());
  }

  @Test
  public void validConnectButton_multi() {
    PostDeployInfo postDeployInfo = PostDeployInfo.newBuilder()
        .setConnectButton(validMultiInstanceConnectButton())
        .build();

    validate(newMultiSpecWithDefaults()
        .setPostDeploy(postDeployInfo)
        .build());
  }

  @Test
  public void applicationStatus_waiterSpecOnlyForWaiterType() {
    expectIllegalArgumentException(
        "Waiter spec should only be set for an application status of type WAITER");
    ApplicationStatusSpec.Builder appStatus = ApplicationStatusSpec.newBuilder();
    appStatus.getWaiterBuilder();
    validateApplicationStatus(appStatus.build());
  }

  @Test
  public void applicationStatus_waiterSpecExpectedForWaiterType() {
    expectIllegalArgumentException(
        "Application status of type WAITER must have a valid waiter spec");
    ApplicationStatusSpec.Builder appStatus =
        ApplicationStatusSpec.newBuilder().setType(ApplicationStatusSpec.StatusType.WAITER);
    validateApplicationStatus(appStatus.build());
  }

  @Test
  public void applicationStatus_waiterMustHaveValidTimeout() {
    expectIllegalArgumentException("Application status waiter must have a valid timeout");
    ApplicationStatusSpec.Builder appStatus =
        ApplicationStatusSpec.newBuilder().setType(ApplicationStatusSpec.StatusType.WAITER);
    appStatus.getWaiterBuilder();
    validateApplicationStatus(appStatus.build());
  }

  @Test
  public void applicationStatus_waiterScriptMustHaveValidTimeout() {
    expectIllegalArgumentException("Application status waiter script must have a valid timeout");
    ApplicationStatusSpec.Builder appStatus =
        ApplicationStatusSpec.newBuilder().setType(ApplicationStatusSpec.StatusType.WAITER);
    appStatus.getWaiterBuilder().setWaiterTimeoutSecs(300).getScriptBuilder();
    validateApplicationStatus(appStatus.build());
  }

  @Test
  public void applicationStatus_validWaiterWithScript() {
    ApplicationStatusSpec.Builder appStatusBuilder =
        ApplicationStatusSpec.newBuilder().setType(ApplicationStatusSpec.StatusType.WAITER);
    appStatusBuilder
        .getWaiterBuilder()
        .setWaiterTimeoutSecs(300)
        .getScriptBuilder()
        .setCheckTimeoutSecs(300)
        .setCheckScriptContent("bin bash");
    validateApplicationStatus(appStatusBuilder.build());
  }

  @Test
  public void postDeployActionItem_mustHaveHeading_single() {
    expectIllegalArgumentException("Post deploy action item must have a valid heading");
    PostDeployInfo postDeployInfo =
        PostDeployInfo.newBuilder().addActionItems(PostDeployInfo.ActionItem.newBuilder()).build();
    validateSingleVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployActionItem_mustHaveHeading_multi() {
    expectIllegalArgumentException("Post deploy action item must have a valid heading");
    PostDeployInfo postDeployInfo =
        PostDeployInfo.newBuilder().addActionItems(PostDeployInfo.ActionItem.newBuilder()).build();
    validateMultiVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployActionItem_mustHaveAtLeastDescriptionOrSnippet_single() {
    expectIllegalArgumentException(
        "Post deploy action item must have at least description or snippet");
    PostDeployInfo postDeployInfo =
        PostDeployInfo.newBuilder()
            .addActionItems(PostDeployInfo.ActionItem.newBuilder().setHeading("Action heading"))
            .build();
    validateSingleVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployActionItem_mustHaveAtLeastDescriptionOrSnippet_multi() {
    expectIllegalArgumentException(
        "Post deploy action item must have at least description or snippet");
    PostDeployInfo postDeployInfo =
        PostDeployInfo.newBuilder()
            .addActionItems(PostDeployInfo.ActionItem.newBuilder().setHeading("Action heading"))
            .build();
    validateMultiVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployActionItem_ifPresentShowIfExpressionMustBeValid_input_single() {
    expectIllegalArgumentException(
        "Boolean expression for deploy input field must specify the field's name");
    PostDeployInfo postDeployInfo = buildPostDeployInfoWithActionItemShowIfNotSpecifyingFieldName();
    validateSingleVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployActionItem_ifPresentShowIfExpressionMustBeValid_input_multi() {
    expectIllegalArgumentException(
        "Boolean expression for deploy input field must specify the field's name");
    PostDeployInfo postDeployInfo = buildPostDeployInfoWithActionItemShowIfNotSpecifyingFieldName();
    validateMultiVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployActionItem_ifPresentShowIfExpressionMustBeValid_externalIp_single() {
    expectIllegalArgumentException("Tier attribute must not be specified for has_external_ip "
        + "boolean expression in a single VM's spec");
    PostDeployInfo.ActionItem actionItem =
        PostDeployInfo.ActionItem.newBuilder()
            .setHeading("Action heading")
            .setDescription("Some description")
            .setShowIf(newBooleanExternalIpExpressionWithTierName())
            .build();
    PostDeployInfo postDeployInfo = PostDeployInfo.newBuilder().addActionItems(actionItem).build();
    validateSingleVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployActionItem_ifPresentShowIfExpressionMustBeValid_externalIp_multi() {
    expectIllegalArgumentException("Tier attribute must be specified for has_external_ip "
        + "boolean expression in a multi VM's spec");
    PostDeployInfo.ActionItem actionItem =
        PostDeployInfo.ActionItem.newBuilder()
            .setHeading("Action heading")
            .setDescription("Some description")
            .setShowIf(newBooleanExternalIpExpressionWithoutTierName())
            .build();
    PostDeployInfo postDeployInfo = PostDeployInfo.newBuilder().addActionItems(actionItem).build();
    validateMultiVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployActionItem_valid_single() {
    PostDeployInfo postDeployInfo = buildValidActionItemsInPostDeployInfo();
    validateSingleVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployActionItem_valid_multi() {
    PostDeployInfo postDeployInfo = buildValidActionItemsInPostDeployInfo();
    validateMultiVmPostDeployInfo(postDeployInfo);
  }

  private PostDeployInfo buildValidActionItemsInPostDeployInfo() {
    return PostDeployInfo.newBuilder()
            .addActionItems(
                PostDeployInfo.ActionItem.newBuilder()
                    .setHeading("Action heading")
                    .setDescription("description"))
            .addActionItems(
                PostDeployInfo.ActionItem.newBuilder()
                    .setHeading("Action heading")
                    .setSnippet("snippet"))
            .addActionItems(
                PostDeployInfo.ActionItem.newBuilder()
                    .setHeading("Action heading")
                    .setDescription("description")
                    .setSnippet("snippet"))
            .addActionItems(
                PostDeployInfo.ActionItem.newBuilder()
                    .setHeading("Action heading")
                    .setDescription("description")
                    .setSnippet("snippet")
                    .setShowIf(newValidBooleanDeployInputFieldExpression()))
            .build();
  }

  @Test
  public void postDeployInfoRow_ifPresentShowIfExpressionMustBeValid_input_single() {
    expectIllegalArgumentException(
        "Boolean expression for deploy input field must specify the field's name");
    PostDeployInfo postDeployInfo =
        buildInfoRowWithShowIfExpression(newInvalidBooleanDeployInputFieldExpression());
    validateSingleVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployInfoRow_ifPresentShowIfExpressionMustBeValid_input_multi() {
    expectIllegalArgumentException(
        "Boolean expression for deploy input field must specify the field's name");
    PostDeployInfo postDeployInfo =
        buildInfoRowWithShowIfExpression(newInvalidBooleanDeployInputFieldExpression());
    validateMultiVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployInfoRow_ifPresentShowIfExpressionMustBeValid_externalIp_single() {
    expectIllegalArgumentException("Tier attribute must not be specified for has_external_ip "
        + "boolean expression in a single VM's spec");
    PostDeployInfo postDeployInfo =
        buildInfoRowWithShowIfExpression(newBooleanExternalIpExpressionWithTierName());
    validateSingleVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployInfoRow_ifPresentShowIfExpressionMustBeValid_externalIp_multi() {
    expectIllegalArgumentException("Tier attribute must be specified for has_external_ip "
        + "boolean expression in a multi VM's spec");
    PostDeployInfo postDeployInfo =
        buildInfoRowWithShowIfExpression(newBooleanExternalIpExpressionWithoutTierName());
    validateMultiVmPostDeployInfo(postDeployInfo);
  }

  private PostDeployInfo buildInfoRowWithShowIfExpression(BooleanExpression expression) {
    PostDeployInfo.InfoRow infoRow =
        PostDeployInfo.InfoRow.newBuilder()
            .setLabel("My label")
            .setValue("Some value")
            .setShowIf(expression)
            .build();
    return PostDeployInfo.newBuilder().addInfoRows(infoRow).build();
  }

  @Test
  public void postDeployInfoRow_valueShouldNotExceedTheLimit_single() {
    expectIllegalArgumentException(String.format(
        "Info row value length cannot exceed the limit of %d characters",
        SpecValidations.INFO_ROW_MAX_LENGTH));

    PostDeployInfo postDeployInfo = buildInfoRowWithTooLongValue();
    validateSingleVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployInfoRow_valueShouldNotExceedTheLimit_multi() {
    expectIllegalArgumentException(String.format(
        "Info row value length cannot exceed the limit of %d characters",
        SpecValidations.INFO_ROW_MAX_LENGTH));

    PostDeployInfo postDeployInfo = buildInfoRowWithTooLongValue();
    validateMultiVmPostDeployInfo(postDeployInfo);
  }

  private PostDeployInfo buildInfoRowWithTooLongValue() {
    StringBuilder longString = new StringBuilder();
    for (int i = 0; i < 50; i++) {
      longString.append("abc ");
    }

    PostDeployInfo.InfoRow infoRow =
        PostDeployInfo.InfoRow.newBuilder()
            .setLabel("My label")
            .setValue(longString.toString())
            .build();

    return PostDeployInfo.newBuilder().addInfoRows(infoRow).build();
  }

  @Test
  public void postDeployInfoRow_valueFromDeployInputFieldShouldNotBeEmpty_single() {
    expectIllegalArgumentException(
        "Expected non-empty deploy input field name for info row value");
    PostDeployInfo postDeployInfo = buildInfoRowWithEmptyDeployInputField();
    validateSingleVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployInfoRow_valueFromDeployInputFieldShouldNotBeEmpty_multi() {
    expectIllegalArgumentException(
        "Expected non-empty deploy input field name for info row value");
    PostDeployInfo postDeployInfo = buildInfoRowWithEmptyDeployInputField();
    validateMultiVmPostDeployInfo(postDeployInfo);
  }

  private PostDeployInfo buildInfoRowWithEmptyDeployInputField() {
    PostDeployInfo.InfoRow infoRow =
        PostDeployInfo.InfoRow.newBuilder()
            .setLabel("My label")
            .setValueFromDeployInputField("")
            .build();
    return PostDeployInfo.newBuilder().addInfoRows(infoRow).build();
  }

  @Test
  public void postDeployInfoRow_shouldSpecifyValue_single() {
    expectIllegalArgumentException("Unexpected info row value");
    PostDeployInfo postDeployInfo = buildInfoRowWithoutValue();
    validateSingleVmPostDeployInfo(postDeployInfo);
  }

  @Test
  public void postDeployInfoRow_shouldSpecifyValue_multi() {
    expectIllegalArgumentException("Unexpected info row value");
    PostDeployInfo postDeployInfo = buildInfoRowWithoutValue();
    validateMultiVmPostDeployInfo(postDeployInfo);
  }

  private PostDeployInfo buildInfoRowWithoutValue() {
    PostDeployInfo.InfoRow infoRow =
        PostDeployInfo.InfoRow.newBuilder().setLabel("My label").build();
    return PostDeployInfo.newBuilder().addInfoRows(infoRow).build();
  }

  @Test
  public void gceMetadataItemsMustHaveValidKey() {
    expectIllegalArgumentException("GCE metadata item must have a valid key");
    GceMetadataItem metadataItem = GceMetadataItem.newBuilder().setValue("value").build();
    validateSingleVmGceMetadataItems(ImmutableList.of(metadataItem));
    validateMultiVmGceMetadataItems(ImmutableList.of(metadataItem));
  }

  @Test
  public void gceMetadataItemKeysCannotBeInBlacklist() {
    expectIllegalArgumentException(Matchers.startsWith("GCE metadata item key cannot be one of ["));
    GceMetadataItem metadataItem =
        GceMetadataItem.newBuilder().setKey("startup-script").setValue("value").build();
    validateSingleVmGceMetadataItems(ImmutableList.of(metadataItem));
    validateMultiVmGceMetadataItems(ImmutableList.of(metadataItem));
  }

  @Test
  public void gceMetadataItemsCannotShareKeys_single() {
    expectIllegalArgumentException("Metadata key 'key' is not unique");
    GceMetadataItem metadataItem =
        GceMetadataItem.newBuilder().setKey("key").setValue("value").build();
    validate(
        newSingleSpecWithDefaults()
            .addGceMetadataItems(metadataItem)
            .addGceMetadataItems(metadataItem)
            .build());
  }

  @Test
  public void gceMetadataItemsCannotShareKeys_multi() {
    expectIllegalArgumentException("Metadata key 'key' is not unique");
    GceMetadataItem metadataItem =
        GceMetadataItem.newBuilder().setKey("key").setValue("value").build();
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(0).addGceMetadataItems(metadataItem).addGceMetadataItems(metadataItem);
    validate(builder.build());
  }

  @Test
  public void gceMetadataItemsAcrossTiersCanShareKeys() {
    GceMetadataItem metadataItem =
        GceMetadataItem.newBuilder().setKey("key").setValue("value").build();
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.addTiersBuilder().mergeFrom(builder.getTiers(0)).setName("tier2");
    // These three define the same key but they are in separate tiers.
    // Three tiers can discover a bug that 2 tiers wouldn't catch!
    builder.getTiersBuilder(0).addGceMetadataItems(metadataItem);
    builder.getTiersBuilder(1).addGceMetadataItems(metadataItem);
    builder.getTiersBuilder(2).addGceMetadataItems(metadataItem);
    validate(builder.build());
  }

  @Test
  public void gceMetadataItemsMustHaveValidValue() {
    expectIllegalArgumentException("GCE metadata item must have a valid value");
    GceMetadataItem metadataItem = GceMetadataItem.newBuilder().setKey("key").build();
    validateSingleVmGceMetadataItems(ImmutableList.of(metadataItem));
    validateMultiVmGceMetadataItems(ImmutableList.of(metadataItem));
  }

  @Test
  public void gceMetadataItemsCannotUseTierVmForSingleVm() {
    expectIllegalArgumentException("GCE metadata item for single VM cannot specify tier VM");
    GceMetadataItem metadataItem =
        GceMetadataItem.newBuilder()
            .setKey("key")
            .setTierVmNames(TierVmNames.newBuilder().setTier("tier0"))
            .build();
    validateSingleVmGceMetadataItems(ImmutableList.of(metadataItem));
  }

  @Test
  public void validGceMetadataItemsWithValue() {
    GceMetadataItem metadataItem =
        GceMetadataItem.newBuilder().setKey("key").setValue("value").build();
    validateSingleVmGceMetadataItems(ImmutableList.of(metadataItem));
    validateMultiVmGceMetadataItems(ImmutableList.of(metadataItem));
  }

  @Test
  public void validGceMetadataItemsWithTierVm() {
    GceMetadataItem metadataItem =
        GceMetadataItem.newBuilder()
            .setKey("key")
            .setTierVmNames(TierVmNames.newBuilder().setTier("tier0"))
            .build();
    validateMultiVmGceMetadataItems(ImmutableList.of(metadataItem));
  }

  @Test
  public void metadataKeysCannotBeSharedAmongPasswordsAndGceMetadataItems_single() {
    expectIllegalArgumentException("Metadata key 'admin' is not unique");
    PasswordSpec passwordSpec =
        PasswordSpec.newBuilder()
            .setLength(8)
            .setMetadataKey("admin")
            .setDisplayLabel("Admin")
            .build();
    GceMetadataItem metadataItem =
        GceMetadataItem.newBuilder().setKey("admin").setValue("value").build();
    validate(
        newSingleSpecWithDefaults()
            .addPasswords(passwordSpec)
            .addGceMetadataItems(metadataItem)
            .build());
  }

  @Test
  public void metadataKeysCannotBeSharedAmongPasswordsAndGceMetadataItems_multi() {
    expectIllegalArgumentException("Metadata key 'admin' is not unique");
    PasswordSpec passwordSpec =
        PasswordSpec.newBuilder()
            .setLength(8)
            .setMetadataKey("admin")
            .setDisplayLabel("Admin")
            .build();
    GceMetadataItem metadataItem =
        GceMetadataItem.newBuilder().setKey("admin").setValue("value").build();
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.addPasswords(passwordSpec);
    builder.getTiersBuilder(0).addGceMetadataItems(metadataItem);
    validate(builder.build());
  }

  @Test
  public void validAcceleratorSpec() {
    AcceleratorSpec accelerator = AcceleratorSpec.newBuilder()
        .addTypes("nvidia-tesla-k80")
        .addTypes("nvidia-tesla-p100")
        .addTypes("nvidia-tesla-v100")
        .setDefaultCount(1)
        .build();
    validateAccelerators(ImmutableList.of(accelerator));
  }

  @Test
  public void invalidAcceleratorSpec_unsupportedTypes() {
    expectIllegalArgumentException("Unsupported accelerator types: [nvidia-tesla-k801]");
    AcceleratorSpec accelerator = AcceleratorSpec.newBuilder()
        .addTypes("nvidia-tesla-k80")
        .addTypes("nvidia-tesla-k801")
        .setDefaultCount(1)
        .build();
    validateAccelerators(ImmutableList.of(accelerator));
  }

  @Test
  public void invalidAcceleratorSpec_invalidDefaultType() {
    final List<String> types = Arrays.asList("nvidia-tesla-k80", "nvidia-tesla-v100");
    final String invalidDefaultType = "nvidia-tesla-p100";
    expectIllegalArgumentException(
        String.format("Default Accelerator Type must be one of %s", types));
    AcceleratorSpec accelerator =
        AcceleratorSpec.newBuilder()
            .addAllTypes(types)
            .setDefaultType(invalidDefaultType)
            .setDefaultCount(1)
            .build();
    validateAccelerators(ImmutableList.of(accelerator));
  }

  @Test
  public void deployInputSectionsCannotShareNames() {
    DeployInputField.Builder fieldTemplate = DeployInputField.newBuilder().setTitle("Field title");
    fieldTemplate.getBooleanCheckboxBuilder();
    DeployInputSection.Builder sectionTemplate = DeployInputSection.newBuilder()
        .setTitle("Section title").setPlacement(Placement.CUSTOM_BOTTOM);
    DeployInputSpec inputSpec =
        DeployInputSpec.newBuilder()
            .addSections(
                sectionTemplate.clone()
                    .setName("section0")
                    .addFields(fieldTemplate.clone().setName("f0")))
            .addSections(
                sectionTemplate.clone()
                    .setName("section0")
                    .addFields(fieldTemplate.clone().setName("f1")))
            .build();
    expectIllegalArgumentException("Deploy input sections with the same name: section0");
    validateDeployInput(inputSpec);
  }

  @Test
  public void deployInputFieldsCannotShareNamesEvenAcrossSections() {
    DeployInputField.Builder fieldTemplate = DeployInputField.newBuilder().setTitle("Field title");
    fieldTemplate.getBooleanCheckboxBuilder();
    DeployInputSection.Builder sectionTemplate = DeployInputSection.newBuilder()
        .setTitle("Section title").setPlacement(Placement.CUSTOM_BOTTOM);
    DeployInputSpec inputSpec =
        DeployInputSpec.newBuilder()
            .addSections(
                sectionTemplate.clone()
                    .setName("section0")
                    .addFields(fieldTemplate.clone().setName("f0")))
            .addSections(
                sectionTemplate.clone()
                    .setName("section1")
                    .addFields(fieldTemplate.clone().setName("f0")))
            .build();
    expectIllegalArgumentException("Deploy input fields with the same name: f0");
    validateDeployInput(inputSpec);
  }

  @Test
  public void deployInputDisplayGroupsCannotShareNamesEvenAcrossSections() {
    DeployInputField.Builder fieldTemplate = DeployInputField.newBuilder().setTitle("Field title");
    fieldTemplate.getGroupedBooleanCheckboxBuilder()
        .getDisplayGroupBuilder().setName("dg").setTitle("Display group");
    DeployInputSection.Builder sectionTemplate = DeployInputSection.newBuilder()
        .setTitle("Section title").setPlacement(Placement.CUSTOM_BOTTOM);
    DeployInputSpec inputSpec =
        DeployInputSpec.newBuilder()
            .addSections(
                sectionTemplate.clone()
                    .setName("section0")
                    .addFields(fieldTemplate.clone().setName("f0")))
            .addSections(
                sectionTemplate.clone()
                    .setName("section1")
                    .addFields(fieldTemplate.clone().setName("f1")))
            .build();
    expectIllegalArgumentException("Display groups with the same name: dg");
    validateDeployInput(inputSpec);
  }

  @Test
  public void deployInputGroupedBooleanCheckboxMustHaveDisplayGroup() {
    DeployInputField.Builder field =
        DeployInputField.newBuilder().setTitle("Field title").setName("f0");
    field.getGroupedBooleanCheckboxBuilder();
    DeployInputSpec inputSpec =
        DeployInputSpec.newBuilder()
            .addSections(
                DeployInputSection.newBuilder()
                    .setPlacement(Placement.CUSTOM_BOTTOM)
                    .setName("section0")
                    .setTitle("Section title")
                    .addFields(field))
        .build();
    expectIllegalArgumentException(
        "The first grouped boolean checkbox 'f0' must have a display group");
    validateDeployInput(inputSpec);
  }

  @Test
  public void validConsecutiveGroupBooleanCheckboxGroups() {
    DeployInputSection.Builder section = DeployInputSection.newBuilder()
        .setPlacement(Placement.CUSTOM_BOTTOM)
        .setName("section0")
        .setTitle("Section title");
    section.addFieldsBuilder()
        .setName("f0")
        .setTitle("Field 0")
        .getGroupedBooleanCheckboxBuilder()
        .getDisplayGroupBuilder()
        .setName("dg0")
        .setTitle("Display 0");
    section.addFieldsBuilder()
        .setName("f1")
        .setTitle("Field 1")
        .getGroupedBooleanCheckboxBuilder()
        .getDisplayGroupBuilder()
        .setName("dg1")
        .setTitle("Display 1");
    section.addFieldsBuilder()
        .setName("f2")
        .setTitle("Field 2")
        .getGroupedBooleanCheckboxBuilder();
    validateDeployInput(DeployInputSpec.newBuilder().addSections(section).build());
  }

  @Test
  public void externalIpMustNotHaveTierNameForSingleVm() {
    expectIllegalArgumentException("Tier attribute must not be specified for has_external_ip "
        + "boolean expression in a single VM's spec");
    ExternalIpAvailability externalIp = ExternalIpAvailability.newBuilder()
        .setTier("tier0")
        .setNegated(true)
        .build();
    BooleanExpression expression = BooleanExpression.newBuilder()
        .setHasExternalIp(externalIp)
        .build();
    validateSingleVmBooleanExpression(expression);
  }

  @Test
  public void externalIpMustHaveTierNameForMultiVm() {
    expectIllegalArgumentException(
        "Tier attribute must be specified for has_external_ip boolean expression in a multi VM");
    BooleanExpression expression = BooleanExpression.newBuilder()
        .setHasExternalIp(ExternalIpAvailability.newBuilder().setNegated(true).build())
        .build();
    validateMultiVmBooleanExpression(expression);
  }

  @Test
  public void gceStartupScriptMustSpecifyNonEmptyContent() {
    expectIllegalArgumentException(
        "Startup script must specify non-empty script content");
    validateStartupScript(
        GceStartupScriptSpec.newBuilder().build());
  }

  @Test
  public void validGceStartupScript() {
    GceStartupScriptSpec startupSpec = GceStartupScriptSpec.newBuilder()
        .setBashScriptContent("echo OK")
        .build();
    validateStartupScript(startupSpec);
  }

  @Test
  public void validRequiredEmailBoxShouldHaveTestDefaultValue() {
    EmailBox emailBox = EmailBox.newBuilder()
        .setTestDefaultValue("admin@example.com")
        .build();
    DeployInputField input = buildRequiredEmailBoxInputField(emailBox);

    validateDeployInputFields(
        Arrays.asList(input),
        new HashSet<String>(),
        new HashSet<String>());
  }

  @Test
  public void invalidRequiredEmailBoxShouldHaveTestDefaultValue() {
    expectIllegalArgumentException(
        "Field 'adminEmailAddress' is required - it should have testDefaultValue");

    EmailBox emailBox = EmailBox.newBuilder().build();
    DeployInputField input = buildRequiredEmailBoxInputField(emailBox);

    validateDeployInputFields(
        Arrays.asList(input),
        new HashSet<String>(),
        new HashSet<String>());
  }

  private static DeployInputField buildRequiredEmailBoxInputField(EmailBox emailBox) {
    return DeployInputField.newBuilder()
        .setName("adminEmailAddress")
        .setTitle("Administrator email address")
        .setRequired(true)
        .setEmailBox(emailBox)
        .build();
  }

  @Test
  public void externalIpShouldNotFailOnEphemeralDefaultType() {
    ExternalIpSpec externalIp = ExternalIpSpec.newBuilder()
        .setDefaultType(Type.EPHEMERAL)
        .build();
    validateExternalIp(externalIp);
  }

  @Test
  public void externalIpShouldNotFailOnNoneDefaultType() {
    ExternalIpSpec externalIp = ExternalIpSpec.newBuilder()
        .setDefaultType(Type.NONE)
        .build();
    validateExternalIp(externalIp);
  }

  @Test
  public void externalIpMustNotTakeUnspecifiedValue() {
    expectIllegalArgumentException("External IP default type must have a valid type");
    ExternalIpSpec externalIp = ExternalIpSpec.newBuilder()
        .setDefaultType(Type.TYPE_UNSPECIFIED)
        .build();
    validateExternalIp(externalIp);
  }

  @Test
  public void stackdriverSpecConnotBeEmpty() {
    expectIllegalArgumentException(
        "Invalid Stackdriver spec. At least one of logging or monitoring must be specified");
    validateStackdriver(StackdriverSpec.getDefaultInstance());
  }

  @Test
  public void networkInterfacesLabelsDoNotFailIfNotExceedMinCountPlus1_singleVm() {
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getNetworkInterfacesBuilder().setMinCount(2);
    builder.getNetworkInterfacesBuilder().addAllLabels(ImmutableList.of("1", "2", "3"));
    validate(builder.build());
  }

  @Test
  public void networkInterfacesLabelsMustNotExceedMinCountPlus1_singleVm() {
    expectIllegalArgumentException("The number of labels must not exceed min_count + 1.");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getNetworkInterfacesBuilder().setMinCount(2);
    builder.getNetworkInterfacesBuilder().addAllLabels(ImmutableList.of("1", "2", "3", "4"));
    validate(builder.build());
  }

  @Test
  public void networkInterfacesMustHaveMinCountGreaterThan0_singleVm() {
    expectIllegalArgumentException("Minimum number of Network interfaces must be greater than 0.");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getNetworkInterfacesBuilder().setMinCount(0);
    validate(builder.build());
  }

  @Test
  public void networkInterfacesMustNotHaveNegativeMinCount_singleVm() {
    expectIllegalArgumentException("Minimum number of Network interfaces must be greater than 0.");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getNetworkInterfacesBuilder().setMinCount(-1);
    validate(builder.build());
  }

  @Test
  public void networkInterfacesMustNotHaveMaxCountLowerThanMinCount_singleVm() {
    expectIllegalArgumentException(
        "Maxmium number of Network interfaces must be greater than minimum and at most 8.");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getNetworkInterfacesBuilder().setMinCount(2).setMaxCount(1);
    validate(builder.build());
  }

  @Test
  public void networkInterfacesMustNotHaveMaxCountGreaterThan8_singleVm() {
    expectIllegalArgumentException(
        "Maxmium number of Network interfaces must be greater than minimum and at most 8.");
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getNetworkInterfacesBuilder().setMinCount(2).setMaxCount(9);
    validate(builder.build());
  }

  @Test
  public void networkInterfacesDoNotFailWithCorrectMinAndMaxCount_singleVm() {
    validate(newSingleSpecWithDefaults().build());
  }

  @Test
  public void networkInterfacesDoNotFailWithCorrectMinAndMaxCountEqual_singleVm() {
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpecWithDefaults();
    builder.getNetworkInterfacesBuilder().setMinCount(3).setMaxCount(3);
    validate(builder.build());
  }

  @Test
  public void networkInterfacesLabelsMustNotExceedMinCountPlus1_multiVm() {
    expectIllegalArgumentException("The number of labels must not exceed min_count + 1.");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getNetworkInterfacesBuilder().setMinCount(2);
    builder
        .getTiersBuilder(1)
        .getNetworkInterfacesBuilder()
        .addAllLabels(ImmutableList.of("1", "2", "3", "4"));
    validate(builder.build());
  }

  @Test
  public void networkInterfacesLabelsDoNotFailIfNotExceedMinCountPlus1_multiVm() {
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getNetworkInterfacesBuilder().setMinCount(2);
    builder
        .getTiersBuilder(1)
        .getNetworkInterfacesBuilder()
        .addAllLabels(ImmutableList.of("1", "2", "3"));
    validate(builder.build());
  }

  @Test
  public void networkInterfacesMustHaveMinCountGreaterThan0_multiVm() {
    expectIllegalArgumentException("Minimum number of Network interfaces must be greater than 0.");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getNetworkInterfacesBuilder().setMinCount(0);
    validate(builder.build());
  }

  @Test
  public void networkInterfacesMustNotHaveNegativeMinCount_multiVm() {
    expectIllegalArgumentException("Minimum number of Network interfaces must be greater than 0.");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getNetworkInterfacesBuilder().setMinCount(-1);
    validate(builder.build());
  }

  @Test
  public void networkInterfacesMustNotHaveMaxCountLowerThanMinCount_multiVm() {
    expectIllegalArgumentException(
        "Maxmium number of Network interfaces must be greater than minimum and at most 8.");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getNetworkInterfacesBuilder().setMinCount(4).setMaxCount(1);
    validate(builder.build());
  }

  @Test
  public void networkInterfacesMustNotHaveMaxCountGreaterThan8_multiVm() {
    expectIllegalArgumentException(
        "Maxmium number of Network interfaces must be greater than minimum and at most 8.");
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getNetworkInterfacesBuilder().setMinCount(4).setMaxCount(10);
    validate(builder.build());
  }

  @Test
  public void networkInterfacesDoNotFailWithCorrectMinAndMaxCount_multiVm() {
    validate(newMultiSpecWithDefaults().build());
  }

  @Test
  public void networkInterfacesDoNotFailWithCorrectMinAndMaxCountEqual_multiVm() {
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpecWithDefaults();
    builder.getTiersBuilder(1).getNetworkInterfacesBuilder().setMinCount(1).setMaxCount(1);
    validate(builder.build());
  }

  private void expectIllegalArgumentException(String message) {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(message);
  }

  private void expectIllegalArgumentException(Matcher<String> matcher) {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(matcher);
  }

  private SingleVmDeploymentPackageSpec.Builder newSingleSpec() {
    return SingleVmDeploymentPackageSpec.newBuilder()
        .addImages(ImageSpec.newBuilder().setProject("image-project").setName("image-name"));
  }

  private SingleVmDeploymentPackageSpec.Builder newSingleSpecWithDefaults() {
    return newSingleSpec()
        .setNetworkInterfaces(
            NetworkInterfacesSpec.newBuilder()
                .setMinCount(2)
                .setMaxCount(6)
                .setExternalIp(ExternalIpSpec.newBuilder().setDefaultType(Type.EPHEMERAL)))
        .setMachineType(
            MachineTypeSpec.newBuilder()
                .setDefaultMachineType(MachineType.newBuilder().setGceMachineType("n1-standard-1")))
        .setBootDisk(
            DiskSpec.newBuilder()
                .setDiskType(DiskType.newBuilder().setDefaultType("pd-ssd"))
                .setDiskSize(DiskSize.newBuilder().setDefaultSizeGb(5))
                .setDisplayLabel("Boot disk"))
        .addAdditionalDisks(
            DiskSpec.newBuilder()
                .setDeviceNameSuffix(DeviceName.newBuilder().setName("data-disk"))
                .setDiskType(DiskType.newBuilder().setDefaultType("pd-ssd"))
                .setDiskSize(DiskSize.newBuilder().setDefaultSizeGb(5))
                .setDisplayLabel("Data Disk"));
  }

  private MultiVmDeploymentPackageSpec.Builder newMultiSpec() {
    MultiVmDeploymentPackageSpec.Builder builder = MultiVmDeploymentPackageSpec.newBuilder();
    builder
        .addTiersBuilder()
        .setNetworkInterfaces(
            NetworkInterfacesSpec.newBuilder()
                .setExternalIp(ExternalIpSpec.newBuilder().setDefaultType(Type.EPHEMERAL)))
        .setName("tier0")
        .setTitle("Tier 0")
        .addImages(ImageSpec.newBuilder().setProject("image-project").setName("image-name-0"));
    builder
        .addTiersBuilder()
        .setNetworkInterfaces(
            NetworkInterfacesSpec.newBuilder()
                .setMinCount(2)
                .setMaxCount(2)
                .setExternalIp(ExternalIpSpec.newBuilder().setDefaultType(Type.NONE)))
        .setName("tier1")
        .setTitle("Tier 1")
        .addImages(ImageSpec.newBuilder().setProject("image-project").setName("image-name-1"));
    return builder;
  }

  private MultiVmDeploymentPackageSpec.Builder newMultiSpecWithDefaults() {
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpec();
    for (VmTierSpec.Builder tier : builder.getTiersBuilderList()) {
      tier.setMachineType(
              MachineTypeSpec.newBuilder()
                  .setDefaultMachineType(
                      MachineType.newBuilder().setGceMachineType("n1-standard-1")))
          .setBootDisk(
              DiskSpec.newBuilder()
                  .setDiskType(DiskSpec.DiskType.newBuilder().setDefaultType("pd-ssd"))
                  .setDiskSize(DiskSpec.DiskSize.newBuilder().setDefaultSizeGb(5))
                  .setDisplayLabel("Boot disk"))
          .addAdditionalDisks(
              DiskSpec.newBuilder()
                  .setDeviceNameSuffix(DeviceName.newBuilder().setName("data-disk"))
                  .setDiskType(DiskType.newBuilder().setDefaultType("pd-ssd"))
                  .setDiskSize(DiskSize.newBuilder().setDefaultSizeGb(5))
                  .setDisplayLabel("Data Disk"))
          .setNetworkInterfaces(
              NetworkInterfacesSpec.newBuilder()
                  .setMinCount(2)
                  .setMaxCount(6)
                  .setExternalIp(ExternalIpSpec.newBuilder().setDefaultType(Type.EPHEMERAL)));
    }
    return builder;
  }

  private static MachineTypeSpec invalidMachineTypeSpec() {
    return MachineTypeSpec.newBuilder().setDefaultMachineType(MachineType.getDefaultInstance()).build();
  }

  private static MachineTypeSpec validMachineTypeSpec() {
    return MachineTypeSpec.newBuilder()
        .setDefaultMachineType(MachineType.newBuilder().setGceMachineType("boom-boom-boom"))
        .build();
  }

  private static MachineTypeSpec machineTypeSpecWithNegativeCpu() {
    return MachineTypeSpec.newBuilder()
        .setDefaultMachineType(MachineType.newBuilder().setGceMachineType("boom-boom-boom"))
        .setMinimum(MachineTypeConstraint.newBuilder().setCpu(-1))
        .build();
  }

  private static MachineTypeSpec machineTypeSpecWithNegativeRam() {
    return MachineTypeSpec.newBuilder()
        .setDefaultMachineType(MachineType.newBuilder().setGceMachineType("boom-boom-boom"))
        .setMinimum(MachineTypeConstraint.newBuilder().setRamGb(-1))
        .build();
  }

  private static MachineTypeSpec machineTypeSpecWithEmptyConstraint() {
    return MachineTypeSpec.newBuilder()
        .setDefaultMachineType(MachineType.newBuilder().setGceMachineType("boom-boom-boom"))
        .setMinimum(MachineTypeConstraint.newBuilder())
        .build();
  }

  private static MachineTypeSpec machineTypeSpecWithZeroes() {
    return MachineTypeSpec.newBuilder()
        .setDefaultMachineType(MachineType.newBuilder().setGceMachineType("boom-boom-boom"))
        .setMinimum(MachineTypeConstraint.newBuilder().setCpu(0).setRamGb(0))
        .build();
  }

  private static MachineTypeSpec machineTypeSpecWithValidMinimums() {
    return MachineTypeSpec.newBuilder()
        .setDefaultMachineType(MachineType.newBuilder().setGceMachineType("boom-boom-boom"))
        .setMinimum(MachineTypeConstraint.newBuilder().setCpu(4).setRamGb(8))
        .build();
  }

  private static DiskSpec validBootDiskSpec() {
    return DiskSpec.newBuilder()
        .setDiskType(DiskSpec.DiskType.newBuilder().setDefaultType("pd-super"))
        .setDiskSize(DiskSpec.DiskSize.newBuilder().setDefaultSizeGb(5).setMinSizeGb(5))
        .setDisplayLabel("Boot Disk")
        .build();
  }

  private static List<DiskSpec> validAdditionalDisksSpec() {
    return Arrays.asList(
        DiskSpec.newBuilder()
            .setDeviceNameSuffix(DeviceName.newBuilder().setName("disk1"))
            .setDiskType(DiskType.newBuilder().setDefaultType("ps-super"))
            .setDiskSize(DiskSize.newBuilder().setDefaultSizeGb(3).setMinSizeGb(3))
            .setDisplayLabel("Data disk")
            .build(),
        DiskSpec.newBuilder()
            .setDeviceNameSuffix(DeviceName.newBuilder().setName("disk2"))
            .setDiskType(DiskSpec.DiskType.newBuilder().setDefaultType("ps-super"))
            .setDiskSize(DiskSpec.DiskSize.newBuilder().setDefaultSizeGb(5).setMinSizeGb(2))
            .setDisplayLabel("Extra disk")
            .build()
    );
  }

  private static FirewallRuleSpec firewallRuleWithoutProtocol() {
    return FirewallRuleSpec.newBuilder()
        .setPort("8080")
        .setAllowedSource(TrafficSource.PUBLIC)
        .build();
  }

  private static FirewallRuleSpec firewallRuleWithoutAllowedSource() {
    return FirewallRuleSpec.newBuilder()
        .setProtocol(FirewallRuleSpec.Protocol.TCP)
        .setPort("8080")
        .build();
  }

  private static FirewallRuleSpec validHttpFirewallRule() {
    return FirewallRuleSpec.newBuilder()
        .setProtocol(FirewallRuleSpec.Protocol.TCP)
        .setPort("80")
        .setAllowedSource(TrafficSource.PUBLIC)
        .build();
  }

  private static FirewallRuleSpec validHttpsFirewallRule() {
    return FirewallRuleSpec.newBuilder()
        .setProtocol(FirewallRuleSpec.Protocol.TCP)
        .setPort("443")
        .setAllowedSource(TrafficSource.PUBLIC)
        .build();
  }

  private static FirewallRuleSpec firewallRuleWithDeploymentSource() {
    return FirewallRuleSpec.newBuilder()
        .setProtocol(FirewallRuleSpec.Protocol.ICMP)
        .setPort("443")
        .setAllowedSource(TrafficSource.DEPLOYMENT)
        .build();
  }

  private static PasswordSpec passwordSpecWithoutMetadataKey() {
    return PasswordSpec.newBuilder().setLength(8).build();
  }

  private static PasswordSpec passwordSpecWithoutLength() {
    return PasswordSpec.newBuilder().setMetadataKey("password-key").build();
  }

  private static PasswordSpec passwordSpecWithoutDisplayLabel() {
    return PasswordSpec.newBuilder().setLength(8).setMetadataKey("password-key").build();
  }

  private static PasswordSpec validPasswordSpec() {
    return PasswordSpec.newBuilder()
        .setLength(8)
        .setMetadataKey("password-key")
        .setDisplayLabel("Admin")
        .build();
  }

  private static PasswordSpec validPasswordSpec1() {
    return PasswordSpec.newBuilder()
        .setLength(8)
        .setMetadataKey("password-key-1")
        .setDisplayLabel("Admin")
        .build();
  }

  private static PasswordSpec passwordSpecWithBlacklistedMetadataKey() {
    return PasswordSpec.newBuilder()
        .setLength(8)
        .setMetadataKey("startup-script")
        .setDisplayLabel("Admin")
        .build();
  }

  private static PasswordSpec passwordSpecWithValidGenerateIfConditionForInput() {
    return PasswordSpec.newBuilder(validPasswordSpec())
        .setGenerateIf(newValidBooleanDeployInputFieldExpression())
        .build();
  }

  private ImmutableList<PasswordSpec> commonValidPasswordSpecs() {
    return ImmutableList.of(
        validPasswordSpec(),
        validPasswordSpec1(),
        passwordSpecWithValidGenerateIfConditionForInput()
    );
  }

  private static PasswordSpec passwordSpecWithValidGenerateIfConditionForExternalIp_single() {
    return PasswordSpec.newBuilder(validPasswordSpec())
        .setGenerateIf(newBooleanExternalIpExpressionWithoutTierName())
        .build();
  }

  private static PasswordSpec passwordSpecWithValidGenerateIfConditionForExternalIp_multi() {
    return PasswordSpec.newBuilder(validPasswordSpec())
        .setGenerateIf(newBooleanExternalIpExpressionWithTierName())
        .build();
  }

  private static PasswordSpec passwordSpecWithInvalidGenerateIfConditionForDeployInput() {
    return PasswordSpec.newBuilder(validPasswordSpec())
        .setGenerateIf(newInvalidBooleanDeployInputFieldExpression())
        .build();
  }

  private static PasswordSpec passwordSpecWithInvalidGenerateIfConditionForExternalIp_single() {
    return PasswordSpec.newBuilder(validPasswordSpec())
        .setGenerateIf(newBooleanExternalIpExpressionWithTierName())
        .build();
  }

  private static PasswordSpec passwordSpecWithInvalidGenerateIfConditionForExternalIp_multi() {
    return PasswordSpec.newBuilder(validPasswordSpec())
        .setGenerateIf(newBooleanExternalIpExpressionWithoutTierName())
        .build();
  }

  private static GcpAuthScopeSpec computeGcpAuthScopeSpec() {
    return GcpAuthScopeSpec.newBuilder().setScope(GcpAuthScopeSpec.Scope.COMPUTE).build();
  }

  private static GcpAuthScopeSpec computeReadGcpAuthScopeSpec() {
    return GcpAuthScopeSpec.newBuilder()
        .setScope(GcpAuthScopeSpec.Scope.COMPUTE_READONLY)
        .build();
  }

  private InstanceUrlSpec validSingleInstanceUrl() {
    return InstanceUrlSpec.newBuilder().setScheme(InstanceUrlSpec.Scheme.HTTP).build();
  }

  private InstanceUrlSpec validMultiInstanceUrl() {
    return InstanceUrlSpec.newBuilder()
        .setScheme(InstanceUrlSpec.Scheme.HTTP)
        .setTierVm(TierVmInstance.newBuilder().setTier("tier0"))
        .build();
  }

  private InstanceUrlSpec multiInstanceUrlWithTierVmButNoTier() {
    return InstanceUrlSpec.newBuilder()
        .setScheme(InstanceUrlSpec.Scheme.HTTP)
        .setTierVm(TierVmInstance.newBuilder())
        .build();
  }

  private ConnectToInstanceSpec validSingleInstanceConnectButton() {
    return validSingleInstanceConnectButton("SSH");
  }

  private ConnectToInstanceSpec validSingleInstanceConnectButton(String displayLabel) {
    return ConnectToInstanceSpec.newBuilder()
        .setDisplayLabel(displayLabel)
        .build();
  }

  private ConnectToInstanceSpec validMultiInstanceConnectButton() {
    return validMultiInstanceConnectButton("SSH");
  }

  private ConnectToInstanceSpec validMultiInstanceConnectButton(String displayLabel) {
    return ConnectToInstanceSpec.newBuilder()
        .setTierVm(TierVmInstance.newBuilder().setTier("tier0"))
        .setDisplayLabel(displayLabel)
        .build();
  }

  private static BooleanExpression newValidBooleanDeployInputFieldExpression() {
    BooleanDeployInputField checkboxReference = BooleanDeployInputField.newBuilder()
        .setName("input_someCheckbox")
        .build();
    return BooleanExpression.newBuilder()
        .setBooleanDeployInputField(checkboxReference)
        .build();
  }

  private static BooleanExpression newInvalidBooleanDeployInputFieldExpression() {
    //Omit setting the field's name:
    BooleanDeployInputField checkboxReference = BooleanDeployInputField.newBuilder().build();
    return BooleanExpression.newBuilder()
        .setBooleanDeployInputField(checkboxReference)
        .build();
  }

  private static BooleanExpression newBooleanExternalIpExpressionWithTierName() {
    ExternalIpAvailability externalIp = ExternalIpAvailability.newBuilder()
        .setTier("tier0")
        .build();
    return BooleanExpression.newBuilder()
        .setHasExternalIp(externalIp)
        .build();
  }

  private static BooleanExpression newBooleanExternalIpExpressionWithoutTierName() {
    ExternalIpAvailability externalIp = ExternalIpAvailability.newBuilder().build();
    return BooleanExpression.newBuilder()
        .setHasExternalIp(externalIp)
        .build();
  }

  private PostDeployInfo buildPostDeployInfoWithConnectButton(
      ConnectToInstanceSpec button, String deprecatedLabel) {
    return PostDeployInfo.newBuilder()
        .setConnectButton(button)
        .setConnectButtonLabel(deprecatedLabel)
        .build();
  }

  private PostDeployInfo buildPostDeployInfoWithActionItemShowIfNotSpecifyingFieldName() {
    PostDeployInfo.ActionItem actionItem =
        PostDeployInfo.ActionItem.newBuilder()
            .setHeading("Action heading")
            .setDescription("Some description")
            .setShowIf(newInvalidBooleanDeployInputFieldExpression())
            .build();
    return PostDeployInfo.newBuilder().addActionItems(actionItem).build();
  }
}
