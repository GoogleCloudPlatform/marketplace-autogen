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

import static com.google.cloud.deploymentmanager.autogen.SpecDefaults.fillInMissingDefaults;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;

import com.google.cloud.deploymentmanager.autogen.proto.AcceleratorSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ApplicationStatusSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ApplicationStatusSpec.WaiterSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ApplicationStatusSpec.WaiterSpec.ScriptSpec;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.EmailBox;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.EmailBox.Validation;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSection;
import com.google.cloud.deploymentmanager.autogen.proto.DiskSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ExternalIpSpec;
import com.google.cloud.deploymentmanager.autogen.proto.FirewallRuleSpec;
import com.google.cloud.deploymentmanager.autogen.proto.FirewallRuleSpec.Protocol;
import com.google.cloud.deploymentmanager.autogen.proto.FirewallRuleSpec.TrafficSource;
import com.google.cloud.deploymentmanager.autogen.proto.ImageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.MachineTypeSpec;
import com.google.cloud.deploymentmanager.autogen.proto.MachineTypeSpec.MachineType;
import com.google.cloud.deploymentmanager.autogen.proto.MultiVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.NetworkInterfacesSpec;
import com.google.cloud.deploymentmanager.autogen.proto.PasswordSpec;
import com.google.cloud.deploymentmanager.autogen.proto.SingleVmDeploymentPackageSpec;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests {@link SpecDefaults}. */
@RunWith(JUnit4.class)
public class SpecDefaultsTest {

  @Test
  public void shouldUseDefaultsAndReturnTheSameBuilder_single() {
    SingleVmDeploymentPackageSpec.Builder builder = newSingleSpec();
    SingleVmDeploymentPackageSpec.Builder returned = fillInMissingDefaults(builder);
    assertThat(returned).isSameInstanceAs(builder);
    assertThat(builder.hasMachineType()).isTrue();
    assertThat(builder.hasBootDisk()).isTrue();
    assertThat(builder.hasNetworkInterfaces()).isTrue();
  }

  @Test
  public void shouldUseDefaultsAndReturnTheSameBuilder_multi() {
    MultiVmDeploymentPackageSpec.Builder builder = newMultiSpec();
    MultiVmDeploymentPackageSpec.Builder returned = fillInMissingDefaults(builder);
    assertThat(returned).isSameInstanceAs(builder);
    assertThat(builder.getTiersList()).hasSize(2);
    assertThat(builder.getTiers(0).hasMachineType()).isTrue();
    assertThat(builder.getTiers(0).hasBootDisk()).isTrue();
    assertThat(builder.getTiers(0).hasNetworkInterfaces()).isTrue();
    assertThat(builder.getTiers(1).hasMachineType()).isTrue();
    assertThat(builder.getTiers(1).hasBootDisk()).isTrue();
    assertThat(builder.getTiers(1).hasNetworkInterfaces()).isTrue();
  }

  @Test
  public void shouldNotOverwriteInputMachineType() {
    MachineTypeSpec machineTypeSpec =
        MachineTypeSpec.newBuilder()
            .setDefaultMachineType(MachineType.newBuilder().setGceMachineType("boom-boom-boom"))
            .build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec().setMachineType(machineTypeSpec));
    assertThat(single.getMachineType()).isEqualTo(machineTypeSpec);

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).setMachineType(machineTypeSpec);
    fillInMissingDefaults(multi);
    assertThat(multi.getTiers(0).getMachineType()).isEqualTo(machineTypeSpec);
  }

  @Test
  public void shouldNotOverwriteBootDisk() {
    DiskSpec diskSpec =
        DiskSpec.newBuilder()
            .setDiskType(DiskSpec.DiskType.newBuilder().setDefaultType("pd-boom-boom"))
            .setDiskSize(DiskSpec.DiskSize.newBuilder().setDefaultSizeGb(100))
            .setDisplayLabel("The Booter")
            .build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec().setBootDisk(diskSpec));
    assertThat(single.getBootDisk()).isEqualTo(diskSpec);

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).setBootDisk(diskSpec);
    fillInMissingDefaults(multi);
    assertThat(multi.getTiers(0).getBootDisk()).isEqualTo(diskSpec);
  }

  @Test
  public void shouldAssignDefaultNameForAdditionalDisk_single() {
    DiskSpec diskSpec = DiskSpec.newBuilder().build();
    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec().addAdditionalDisks(diskSpec));
    assertThat(single.getAdditionalDisks(0).getDeviceNameSuffix().getName()).isNotEmpty();
  }

  @Test
  public void shouldAssignDefaultNameForAdditionalDisk_multi() {
    DiskSpec diskSpec = DiskSpec.newBuilder().build();
    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).addAdditionalDisks(diskSpec);
    fillInMissingDefaults(multi);
    assertThat(multi.getTiers(0)
        .getAdditionalDisks(0)
        .getDeviceNameSuffix()
        .getName())
        .isNotEmpty();
  }

  @Test
  public void shouldAssignUniqueDefaultNamesForAdditionalDisks_single() {
    DiskSpec diskSpec1 = DiskSpec.newBuilder().build();
    DiskSpec diskSpec2 = DiskSpec.newBuilder().build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec()
            .addAdditionalDisks(diskSpec1)
            .addAdditionalDisks(diskSpec2));

    assertThat(single.getAdditionalDisks(0).getDeviceNameSuffix().getName())
        .isNotEqualTo(single.getAdditionalDisks(1).getDeviceNameSuffix().getName());
  }

  @Test
  public void shouldAssignUniqueDefaultNamesForAdditionalDisks_multi() {
    DiskSpec diskSpec1 = DiskSpec.newBuilder().build();
    DiskSpec diskSpec2 = DiskSpec.newBuilder().build();

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0)
        .addAdditionalDisks(diskSpec1)
        .addAdditionalDisks(diskSpec2);
    fillInMissingDefaults(multi);
    assertThat(
        multi.getTiers(0).getAdditionalDisks(0).getDeviceNameSuffix().getName())
    .isNotEqualTo(
        multi.getTiers(0).getAdditionalDisks(1).getDeviceNameSuffix().getName());
  }

  @Test
  public void shouldAssignDefaultBootDiskDisplayLabel_single() {
    DiskSpec diskSpec =
        DiskSpec.newBuilder()
            .setDiskType(DiskSpec.DiskType.newBuilder().setDefaultType("pd-boom-boom"))
            .setDiskSize(DiskSpec.DiskSize.newBuilder().setDefaultSizeGb(100))
            .build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec().setBootDisk(diskSpec));
    assertThat(single.getBootDisk().getDisplayLabel()).isNotEmpty();
  }

  @Test
  public void shouldAssignDefaultBootDiskDisplayLabel_multi() {
    DiskSpec diskSpec =
        DiskSpec.newBuilder()
            .setDiskType(DiskSpec.DiskType.newBuilder().setDefaultType("pd-boom-boom"))
            .setDiskSize(DiskSpec.DiskSize.newBuilder().setDefaultSizeGb(100))
            .build();

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).setBootDisk(diskSpec);
    fillInMissingDefaults(multi);
    assertThat(multi.getTiers(0).getBootDisk().getDisplayLabel()).isNotEmpty();
  }

  @Test
  public void shouldAssignDefaultDisplayNameForSingleAdditionalDisk_single() {
    DiskSpec diskSpec = DiskSpec.newBuilder().build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec().addAdditionalDisks(diskSpec));
    assertThat(single.getAdditionalDisks(0).getDisplayLabel()).isNotEmpty();
  }

  @Test
  public void shouldAssignDefaultDisplayNameForSingleAdditionalDisk_multi() {
    DiskSpec diskSpec = DiskSpec.newBuilder().build();

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).addAdditionalDisks(diskSpec);
    fillInMissingDefaults(multi);
    assertThat(multi.getTiers(0).getAdditionalDisks(0).getDisplayLabel())
        .isNotEmpty();
  }

  @Test
  public void shouldNotAssignDefaultDisplayLabelsForMoreThanOneAdditionalDisks_single() {
    DiskSpec diskSpec1 = DiskSpec.newBuilder().build();
    DiskSpec diskSpec2 = DiskSpec.newBuilder().build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec()
            .addAdditionalDisks(diskSpec1)
            .addAdditionalDisks(diskSpec2));
    assertThat(single.getAdditionalDisks(0).getDisplayLabel()).isEmpty();
    assertThat(single.getAdditionalDisks(1).getDisplayLabel()).isEmpty();
  }

  @Test
  public void shouldNotAssignDefaultDisplayLabelsForMoreThanOneAdditionalDisks_multi() {
    DiskSpec diskSpec1 = DiskSpec.newBuilder().build();
    DiskSpec diskSpec2 = DiskSpec.newBuilder().build();

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0)
        .addAdditionalDisks(diskSpec1)
        .addAdditionalDisks(diskSpec2);
    fillInMissingDefaults(multi);
    assertThat(multi.getTiers(0).getAdditionalDisks(0).getDisplayLabel()).isEmpty();
    assertThat(multi.getTiers(0).getAdditionalDisks(0).getDisplayLabel()).isEmpty();
  }

  @Test
  public void shouldDefaultToEphemeralExternalIp() {
    SingleVmDeploymentPackageSpec.Builder single = fillInMissingDefaults(newSingleSpec());
    NetworkInterfacesSpec nicSpec = single.getNetworkInterfaces();
    assertThat(nicSpec.hasExternalIp()).isTrue();
    assertThat(nicSpec.getExternalIp().getDefaultType()).isEqualTo(ExternalIpSpec.Type.EPHEMERAL);

    MultiVmDeploymentPackageSpec.Builder multi = fillInMissingDefaults(newMultiSpec());
    NetworkInterfacesSpec tier0NicSpec = multi.getTiers(0).getNetworkInterfaces();
    assertThat(tier0NicSpec.hasExternalIp()).isTrue();
    assertThat(tier0NicSpec.getExternalIp().getDefaultType())
        .isEqualTo(ExternalIpSpec.Type.EPHEMERAL);

    NetworkInterfacesSpec tier1NicSpec = multi.getTiers(1).getNetworkInterfaces();
    assertThat(tier1NicSpec.hasExternalIp()).isTrue();
    assertThat(tier1NicSpec.getExternalIp().getDefaultType())
        .isEqualTo(ExternalIpSpec.Type.EPHEMERAL);
  }

  @Test
  public void shouldNotOverwriteExternalIp() {
    ExternalIpSpec externalIpSpec =
        ExternalIpSpec.newBuilder().setDefaultType(ExternalIpSpec.Type.NONE).build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(
            newSingleSpec()
                .setNetworkInterfaces(
                    NetworkInterfacesSpec.newBuilder().setExternalIp(externalIpSpec)));
    NetworkInterfacesSpec nicSpec = single.getNetworkInterfaces();
    assertThat(nicSpec.getExternalIp()).isEqualTo(externalIpSpec);

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    NetworkInterfacesSpec.Builder tier0NicSpecBuilder =
        multi.getTiersBuilder(0).getNetworkInterfacesBuilder();
    tier0NicSpecBuilder.setExternalIp(externalIpSpec);
    fillInMissingDefaults(multi);
    assertThat(multi.getTiers(0).getNetworkInterfaces().getExternalIp()).isEqualTo(externalIpSpec);
  }

  @Test
  public void shouldSetDeprecatedExternalIpSpecInNetworkInterfaceSpec() {
    ExternalIpSpec deprecatedExternalIpSpec =
        ExternalIpSpec.newBuilder().setDefaultType(ExternalIpSpec.Type.NONE).build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec().setExternalIp(deprecatedExternalIpSpec));
    assertThat(single.getNetworkInterfaces().getExternalIp()).isEqualTo(deprecatedExternalIpSpec);
  }

  @Test
  public void shouldNotOverrideExternalIpSpecInNetworkInterfaceSpec() {
    ExternalIpSpec deprecatedExternalIpSpec =
        ExternalIpSpec.newBuilder().setNotConfigurable(true).build();
    ExternalIpSpec expected =
        ExternalIpSpec.newBuilder().setDefaultType(ExternalIpSpec.Type.NONE).build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(
            newSingleSpec()
                .setExternalIp(deprecatedExternalIpSpec)
                .setNetworkInterfaces(NetworkInterfacesSpec.newBuilder().setExternalIp(expected)));

    assertThat(single.getNetworkInterfaces().getExternalIp()).isEqualTo(expected);
  }

  @Test
  public void shouldSetDeprecatedExternalIpSpecInNetworkInterfaceSpecMultiVm() {
    ExternalIpSpec deprecatedExternalIpSpec =
        ExternalIpSpec.newBuilder().setDefaultType(ExternalIpSpec.Type.NONE).build();

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).setExternalIp(deprecatedExternalIpSpec);
    fillInMissingDefaults(multi);

    assertThat(multi.getTiers(0).getNetworkInterfaces().getExternalIp())
        .isEqualTo(deprecatedExternalIpSpec);
  }

  @Test
  public void shouldNotOverrideExternalIpSpecInNetworkInterfaceSpecMultiVm() {
    ExternalIpSpec deprecatedExternalIpSpec =
        ExternalIpSpec.newBuilder()
            .setDefaultType(ExternalIpSpec.Type.EPHEMERAL)
            .setNotConfigurable(true)
            .build();
    ExternalIpSpec expected =
        ExternalIpSpec.newBuilder().setDefaultType(ExternalIpSpec.Type.NONE).build();

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).setExternalIp(deprecatedExternalIpSpec);
    multi.getTiersBuilder(1).setExternalIp(deprecatedExternalIpSpec);
    multi.getTiersBuilder(1).getNetworkInterfacesBuilder().setExternalIp(expected);
    fillInMissingDefaults(multi);

    assertThat(multi.getTiers(0).getNetworkInterfaces().getExternalIp())
        .isEqualTo(deprecatedExternalIpSpec);
    assertThat(multi.getTiers(1).getNetworkInterfaces().getExternalIp()).isEqualTo(expected);
  }

  @Test
  public void shouldFillMinCount_networkInterfacesSingleVm() {
    SingleVmDeploymentPackageSpec.Builder single = fillInMissingDefaults(newSingleSpec());

    NetworkInterfacesSpec nicSpec = single.getNetworkInterfaces();
    assertThat(nicSpec.getMinCount()).isEqualTo(1);
  }

  @Test
  public void shouldFillMaxCount_networkInterfacesSingleVm() {
    int min = 3;
    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(
            newSingleSpec()
                .setNetworkInterfaces(NetworkInterfacesSpec.newBuilder().setMinCount(min)));

    NetworkInterfacesSpec nicSpec = single.getNetworkInterfaces();
    assertThat(nicSpec.getMaxCount()).isEqualTo(min);
  }

  @Test
  public void shouldNotOverwriteMinAndMaxCount_networkInterfacesSingleVm() {
    int min = 2;
    int max = 4;
    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(
            newSingleSpec()
                .setNetworkInterfaces(
                    NetworkInterfacesSpec.newBuilder().setMinCount(min).setMaxCount(max)));

    NetworkInterfacesSpec expected =
        NetworkInterfacesSpec.newBuilder().setMinCount(min).setMaxCount(max).build();
    assertThat(single.getNetworkInterfaces()).comparingExpectedFieldsOnly().isEqualTo(expected);
  }

  @Test
  public void shouldNotOverwriteMinAndMaxCount_networkInterfacesMultiVm() {
    int min = 3;
    int max = 3;
    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).getNetworkInterfacesBuilder().setMinCount(min).setMaxCount(max);
    fillInMissingDefaults(multi);

    NetworkInterfacesSpec expected =
        NetworkInterfacesSpec.newBuilder().setMinCount(min).setMaxCount(max).build();
    assertThat(multi.getTiers(0).getNetworkInterfaces())
        .comparingExpectedFieldsOnly()
        .isEqualTo(expected);
  }

  @Test
  public void shouldFillMinCount_networkInterfacesMultiVm() {
    MultiVmDeploymentPackageSpec.Builder multi = fillInMissingDefaults(newMultiSpec());

    NetworkInterfacesSpec nicSpec = multi.getTiers(0).getNetworkInterfaces();
    assertThat(nicSpec.getMinCount()).isEqualTo(1);
  }

  @Test
  public void shouldFillMaxCount_networkInterfacesMultiVm() {
    int min = 4;
    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi
        .getTiersBuilder(0)
        .setNetworkInterfaces(NetworkInterfacesSpec.newBuilder().setMinCount(min));
    fillInMissingDefaults(multi);

    NetworkInterfacesSpec nicSpec = multi.getTiers(0).getNetworkInterfaces();
    assertThat(nicSpec.getMaxCount()).isEqualTo(min);
  }

  @Test
  public void waiterScriptTimeoutShouldUseWaiterTimeoutIfNotSet() {
    ApplicationStatusSpec.Builder appStatus =
        ApplicationStatusSpec.newBuilder()
            .setType(ApplicationStatusSpec.StatusType.WAITER)
            .setWaiter(
                WaiterSpec.newBuilder()
                    .setWaiterTimeoutSecs(300)
                    .setScript(ScriptSpec.newBuilder()));
    ApplicationStatusSpec.Builder expectedAppStatusBuilder = appStatus.clone();
    expectedAppStatusBuilder.getWaiterBuilder().getScriptBuilder().setCheckTimeoutSecs(300);
    ApplicationStatusSpec expectedAppStatus = expectedAppStatusBuilder.build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec().setApplicationStatus(appStatus));
    assertThat(single.getApplicationStatus()).isEqualTo(expectedAppStatus);

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).setApplicationStatus(appStatus);
    fillInMissingDefaults(multi);
    assertThat(multi.getTiers(0).getApplicationStatus()).isEqualTo(expectedAppStatus);
  }

  @Test
  public void waiterScriptTimeoutShouldBePreserved() {
    ApplicationStatusSpec appStatus =
        ApplicationStatusSpec.newBuilder()
            .setType(ApplicationStatusSpec.StatusType.WAITER)
            .setWaiter(
                WaiterSpec.newBuilder()
                    .setWaiterTimeoutSecs(300)
                    .setScript(ScriptSpec.newBuilder().setCheckTimeoutSecs(200)))
            .build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec().setApplicationStatus(appStatus));
    assertThat(single.getApplicationStatus()).isEqualTo(appStatus);

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).setApplicationStatus(appStatus);
    fillInMissingDefaults(multi);
    assertThat(multi.getTiers(0).getApplicationStatus()).isEqualTo(appStatus);
  }

  @Test
  public void defaultLabelShouldApplyForASinglePassword() {
    PasswordSpec password =
        PasswordSpec.newBuilder()
            .setMetadataKey("metadata-pw-key")
            .setLength(10)
            .setUsername("admin")
            .build();

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec().addPasswords(password));
    assertThat(single.getPasswordsList()).hasSize(1);
    assertThat(single.getPasswords(0)).isEqualTo(
        password.toBuilder().setDisplayLabel("Admin").build());

    MultiVmDeploymentPackageSpec.Builder multi =
        fillInMissingDefaults(newMultiSpec().addPasswords(password));
    assertThat(multi.getPasswordsList()).hasSize(1);
    assertThat(multi.getPasswords(0)).isEqualTo(
        password.toBuilder().setDisplayLabel("Admin").build());
  }

  @Test
  public void defaultLabelShouldNotApplyForMoreThanOnePasswords() {
    ImmutableList<PasswordSpec> passwords =
        ImmutableList.of(
            PasswordSpec.newBuilder()
                .setMetadataKey("metadata-pw-key-0")
                .setLength(10)
                .setUsername("admin")
                .build(),
            PasswordSpec.newBuilder()
                .setMetadataKey("metadata-pw-key-1")
                .setLength(8)
                .setUsername("user")
                .build());

    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec().addAllPasswords(passwords));
    assertThat(single.getPasswordsList()).hasSize(2);
    assertThat(single.getPasswordsList()).containsAtLeastElementsIn(passwords).inOrder();

    MultiVmDeploymentPackageSpec.Builder multi =
        fillInMissingDefaults(newMultiSpec().addAllPasswords(passwords));
    assertThat(multi.getPasswordsList()).hasSize(2);
    assertThat(multi.getPasswordsList()).containsAtLeastElementsIn(passwords).inOrder();
  }

  @Test
  public void defaultLabelShouldNotApplyForNoPasswords() {
    SingleVmDeploymentPackageSpec.Builder single = fillInMissingDefaults(newSingleSpec());
    assertThat(single.getPasswordsList()).isEmpty();

    MultiVmDeploymentPackageSpec.Builder multi = fillInMissingDefaults(newMultiSpec());
    assertThat(multi.getPasswordsList()).isEmpty();
  }

  private EmailBox newSingleVmEmailBox(DeployInputSection section) {
    SingleVmDeploymentPackageSpec.Builder single = newSingleSpec();
    single.getDeployInputBuilder().addSections(section);
    fillInMissingDefaults(single);
    return single.getDeployInput()
        .getSections(0)
        .getFields(0)
        .getEmailBox();
  }

  private EmailBox newMultiVmEmailBox(DeployInputSection section) {
    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getDeployInputBuilder().addSections(section);
    fillInMissingDefaults(multi);
    return multi.getDeployInput()
        .getSections(0)
        .getFields(0)
        .getEmailBox();
  }

  @Test
  public void shouldAssignDefaultTestDefaultValueForDeployInputEmailBox() {
    EmailBox emailBox = EmailBox.newBuilder().build();
    DeployInputSection section = DeployInputSection
        .newBuilder()
        .addFields(DeployInputField.newBuilder().setRequired(true).setEmailBox(emailBox))
        .build();

    assertThat(newSingleVmEmailBox(section).getTestDefaultValue())
        .isEqualTo("default-user@example.com");

    assertThat(newMultiVmEmailBox(section).getTestDefaultValue())
        .isEqualTo("default-user@example.com");
  }

  @Test
  public void shouldAssignDefaultPlaceholderForDeployInputEmailBox() {
    EmailBox emailBox = EmailBox.newBuilder().build();
    DeployInputSection section = DeployInputSection
        .newBuilder()
        .addFields(DeployInputField.newBuilder().setEmailBox(emailBox))
        .build();

    assertThat(newSingleVmEmailBox(section).getPlaceholder())
        .isEqualTo("user@example.com");

    assertThat(newMultiVmEmailBox(section).getPlaceholder())
        .isEqualTo("user@example.com");
  }

  @Test
  public void shouldAssignDefaultDescriptionForDeployInputEmailBox() {
    EmailBox emailBox = EmailBox.newBuilder().build();
    DeployInputSection section = DeployInputSection
        .newBuilder()
        .addFields(DeployInputField.newBuilder().setEmailBox(emailBox))
        .build();

    assertThat(newSingleVmEmailBox(section).getValidation().getDescription())
        .isEqualTo("Please enter a valid email address");

    assertThat(newMultiVmEmailBox(section).getValidation().getDescription())
        .isEqualTo("Please enter a valid email address");
  }

  @Test
  public void shouldOverwritenTestDefaultValueWithDefaultValueForDeployInputEmailBox() {
    EmailBox emailBox = EmailBox.newBuilder()
        .setDefaultValue("qd68mj2y8n@example.com")
        .build();
    DeployInputSection section = DeployInputSection
        .newBuilder()
        .addFields(DeployInputField.newBuilder().setRequired(true).setEmailBox(emailBox))
        .build();

    assertThat(newSingleVmEmailBox(section).getTestDefaultValue())
        .isEqualTo("qd68mj2y8n@example.com");

    assertThat(newMultiVmEmailBox(section).getTestDefaultValue())
        .isEqualTo("qd68mj2y8n@example.com");
  }

  @Test
  public void shouldNotOverwritenPlaceholderWithDefaultValueForDeployInputEmailBox() {
    EmailBox emailBox = EmailBox.newBuilder()
        .setPlaceholder("pruvkvewoq@example.com")
        .build();
    DeployInputSection section = DeployInputSection
        .newBuilder()
        .addFields(DeployInputField.newBuilder().setEmailBox(emailBox))
        .build();

    assertThat(newSingleVmEmailBox(section).getPlaceholder())
        .isEqualTo("pruvkvewoq@example.com");

    assertThat(newMultiVmEmailBox(section).getPlaceholder())
        .isEqualTo("pruvkvewoq@example.com");
  }

  @Test
  public void shouldNotOverwritenDescriptionWithDefaultValueForDeployInputEmailBox() {
    Validation validation = Validation.newBuilder()
        .setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
        .build();
    EmailBox emailBox = EmailBox.newBuilder()
        .setValidation(validation)
        .build();
    DeployInputSection section = DeployInputSection
        .newBuilder()
        .addFields(DeployInputField.newBuilder().setEmailBox(emailBox))
        .build();

    assertThat(newSingleVmEmailBox(section).getValidation().getDescription())
        .isEqualTo("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");

    assertThat(newMultiVmEmailBox(section).getValidation().getDescription())
        .isEqualTo("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
  }

  @Test
  public void shouldAssignRegexForDeployInputEmailBox() {
    Validation validation = Validation.newBuilder()
        .setRegex(".?")
        .build();
    EmailBox emailBox = EmailBox.newBuilder()
        .setValidation(validation)
        .build();
    DeployInputSection section = DeployInputSection
        .newBuilder()
        .addFields(DeployInputField.newBuilder().setEmailBox(emailBox))
        .build();

    assertThat(newSingleVmEmailBox(section).getValidation().getRegex())
        .isEqualTo(".?");

    assertThat(newMultiVmEmailBox(section).getValidation().getRegex())
        .isEqualTo(".?");
  }

  private SingleVmDeploymentPackageSpec.Builder newSingleSpec() {
    return SingleVmDeploymentPackageSpec.newBuilder()
        .addImages(ImageSpec.newBuilder().setProject("image-project").setName("image-name"));
  }

  private MultiVmDeploymentPackageSpec.Builder newMultiSpec() {
    MultiVmDeploymentPackageSpec.Builder builder = MultiVmDeploymentPackageSpec.newBuilder();
    builder
        .addTiersBuilder()
        .setName("tier0")
        .addImages(ImageSpec.newBuilder().setProject("image-project").setName("image-name-0"));
    builder
        .addTiersBuilder()
        .setName("tier1")
        .addImages(ImageSpec.newBuilder().setProject("image-project").setName("image-name-1"));
    return builder;
  }

  @Test
  public void defaultFirewallShouldBePublic() {
    ImmutableList<FirewallRuleSpec> firewallRules =
        ImmutableList.of(
            FirewallRuleSpec.newBuilder()
                .setProtocol(Protocol.TCP)
                .setPort("80")
                .build(),
            FirewallRuleSpec.newBuilder()
                .setProtocol(Protocol.ICMP)
                .build());


    SingleVmDeploymentPackageSpec.Builder single =
        fillInMissingDefaults(newSingleSpec().addAllFirewallRules(firewallRules));
    assertThat(
        single.getFirewallRulesList().stream()
            .filter(f -> TrafficSource.PUBLIC.equals(f.getAllowedSource())).count() == 2).isTrue();

    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).addAllFirewallRules(firewallRules);
    fillInMissingDefaults(multi);
    assertThat(
        multi.getTiers(0).getFirewallRulesList().stream()
            .filter(f -> TrafficSource.PUBLIC.equals(f.getAllowedSource())).count() == 2).isTrue();
  }

  @Test
  public void defaultAccelatorTypeMultiVmIsFirstInList() {
    List<String> types = Arrays.asList("nvidia-tesla-p100", "nvidia-tesla-k80");
    AcceleratorSpec acceleratorSpec = AcceleratorSpec.newBuilder().addAllTypes(types).build();
    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).addAccelerators(acceleratorSpec);
    fillInMissingDefaults(multi);
    assertThat(multi.getTiers(0).getAccelerators(0).getDefaultType()).isEqualTo(types.get(0));
  }

  @Test
  public void defaultAccelatorTypeMultiVmIsDefaultType() {
    final String defaultType = "nvidia-tesla-k80";
    List<String> types = Arrays.asList("nvidia-tesla-p100", defaultType);
    AcceleratorSpec acceleratorSpec =
        AcceleratorSpec.newBuilder().addAllTypes(types).setDefaultType(defaultType).build();
    MultiVmDeploymentPackageSpec.Builder multi = newMultiSpec();
    multi.getTiersBuilder(0).addAccelerators(acceleratorSpec);
    fillInMissingDefaults(multi);
    assertThat(multi.getTiers(0).getAccelerators(0).getDefaultType()).isEqualTo(defaultType);
  }

  @Test
  public void defaultAccelatorTypeSingleVmIsFirstInList() {
    List<String> types = Arrays.asList("nvidia-tesla-p100", "nvidia-tesla-k80");
    AcceleratorSpec acceleratorSpec = AcceleratorSpec.newBuilder().addAllTypes(types).build();
    SingleVmDeploymentPackageSpec.Builder single = newSingleSpec();
    single.addAccelerators(acceleratorSpec);
    fillInMissingDefaults(single);
    assertThat(single.getAccelerators(0).getDefaultType()).isEqualTo(types.get(0));
  }

  @Test
  public void defaultAccelatorTypeSingleVmIsDefaultType() {
    final String defaultType = "nvidia-tesla-k80";
    List<String> types = Arrays.asList("nvidia-tesla-p100", defaultType);
    AcceleratorSpec acceleratorSpec =
        AcceleratorSpec.newBuilder().addAllTypes(types).setDefaultType(defaultType).build();
    SingleVmDeploymentPackageSpec.Builder single = newSingleSpec();
    single.addAccelerators(acceleratorSpec);
    fillInMissingDefaults(single);
    assertThat(single.getAccelerators(0).getDefaultType()).isEqualTo(defaultType);
  }
}
