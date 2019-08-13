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

import com.google.cloud.deploymentmanager.autogen.proto.AcceleratorSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ApplicationStatusSpec;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.TypeCase;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSection;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSpec;
import com.google.cloud.deploymentmanager.autogen.proto.DiskSpec;
import com.google.cloud.deploymentmanager.autogen.proto.DiskSpec.DeviceName;
import com.google.cloud.deploymentmanager.autogen.proto.ExternalIpSpec;
import com.google.cloud.deploymentmanager.autogen.proto.FirewallRuleSpec;
import com.google.cloud.deploymentmanager.autogen.proto.FirewallRuleSpec.TrafficSource;
import com.google.cloud.deploymentmanager.autogen.proto.MachineTypeSpec;
import com.google.cloud.deploymentmanager.autogen.proto.MachineTypeSpec.MachineType;
import com.google.cloud.deploymentmanager.autogen.proto.MultiVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.MultiVmDeploymentPackageSpecOrBuilder;
import com.google.cloud.deploymentmanager.autogen.proto.NetworkInterfacesSpec;
import com.google.cloud.deploymentmanager.autogen.proto.PasswordSpec;
import com.google.cloud.deploymentmanager.autogen.proto.PostDeployInfo;
import com.google.cloud.deploymentmanager.autogen.proto.PostDeployInfo.ConnectToInstanceSpec;
import com.google.cloud.deploymentmanager.autogen.proto.SingleVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.TierVmInstance;
import com.google.cloud.deploymentmanager.autogen.proto.VmTierSpec;
import com.google.cloud.deploymentmanager.autogen.proto.VmTierSpec.TierInstanceCount;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.List;

/**
 * Fills in missing fields with defaults where appropriate to make complete specs. Some required
 * fields cannot use defaults--validations of such are in {@link SpecValidations}.
 *
 * @see SpecValidations
 */
final class SpecDefaults {

  private static final String DEFAULT_MACHINE_TYPE = "f1-micro";

  private static final int DEFAULT_BOOT_DISK_SIZE_GB = 10;
  private static final String DEFAULT_BOOT_DISK_TYPE = "pd-standard";

  private static final int DEFAULT_ADDITIONAL_DISK_SIZE_GB = 10;
  private static final String DEFAULT_ADDITIONAL_DISK_TYPE = "pd-standard";

  private static final String DEFAULT_PASSWORD_LABEL = "Admin";

  private static final String DEFAULT_CONNECT_BUTTON_LABEL = "SSH";

  private static final String DEFAULT_BOOT_DISK_DISPLAY_LABEL = "Boot disk";
  private static final String DEFAULT_ADDITIONAL_DISK_DISPLAY_LABEL = "Data disk";

  private static final String DEFAULT_INSTANCE_COUNT_RANGE_TOOLTIP =
      "Specify a value between %d and %d.";

  /**
   * Fills in the default values for missing optional fields, and validates that the spec is
   * reasonable, throwing {@link IllegalArgumentException} if not.
   */
  public static SingleVmDeploymentPackageSpec.Builder fillInMissingDefaults(
      SingleVmDeploymentPackageSpec.Builder input) {
    setMachineTypeDefaults(input.getMachineTypeBuilder());
    setAceleratorsDefaults(input.getAcceleratorsBuilderList());
    setBootDiskDefaults(input.getBootDiskBuilder());
    setAdditionalDisksDefaults(input.getAdditionalDisksBuilderList());
    setPasswordDefaults(input.getPasswordsBuilderList());

    // We keep the external IP spec to be backwards compatible to old solutions. If present, we will
    // set it as the default external IP spec of the NetworkInterfacesSpec
    ExternalIpSpec deprecatedExternalIp = input.hasExternalIp() ? input.getExternalIp() : null;
    setNetworkInterfacesDefault(input.getNetworkInterfacesBuilder(), deprecatedExternalIp);

    if (input.hasApplicationStatus()) {
      // Fill in defaults only when the application status is actually desired (i.e. set by user).
      // Note that .get*Builder() will blindly set the field, so we need the if.
      setApplicationStatusDefaults(input.getApplicationStatusBuilder());
    }
    setConnectButtonDefaults_singlevm(input.getPostDeployBuilder());
    setDeployInputFieldsDefaults(input.getDeployInputBuilder());
    setFirewallDefaults(input.getFirewallRulesBuilderList());
    return input;
  }

  public static MultiVmDeploymentPackageSpec.Builder fillInMissingDefaults(
      MultiVmDeploymentPackageSpec.Builder input) {
    for (VmTierSpec.Builder tier : input.getTiersBuilderList()) {
      setInstanceCountDefaults(tier.getInstanceCountBuilder());
      setMachineTypeDefaults(tier.getMachineTypeBuilder());
      setAceleratorsDefaults(tier.getAcceleratorsBuilderList());
      setBootDiskDefaults(tier.getBootDiskBuilder());
      setAdditionalDisksDefaults(tier.getAdditionalDisksBuilderList());

      // We keep the external IP spec to be backwards compatible to old solutions. If present, we
      // will set it as the default external IP spec of the NetworkInterfacesSpec
      ExternalIpSpec deprecatedExternalIp = tier.hasExternalIp() ? tier.getExternalIp() : null;
      setNetworkInterfacesDefault(tier.getNetworkInterfacesBuilder(), deprecatedExternalIp);

      if (tier.hasApplicationStatus()) {
        // Fill in defaults only when the application status is actually desired (i.e. set by user).
        // Note that .get*Builder() will blindly set the field, so we need the if.
        setApplicationStatusDefaults(tier.getApplicationStatusBuilder());
      }
      setFirewallDefaults(tier.getFirewallRulesBuilderList());
    }
    setPasswordDefaults(input.getPasswordsBuilderList());
    setConnectButtonDefaults_multivm(input.getPostDeployBuilder(), input);
    setDeployInputFieldsDefaults(input.getDeployInputBuilder());
    return input;
  }

  private static void setConnectButtonDefaults_singlevm(PostDeployInfo.Builder postDeploy) {
    setConnectButtonDefaults_common(postDeploy);
  }

  private static void setConnectButtonDefaults_multivm(PostDeployInfo.Builder postDeploy,
      MultiVmDeploymentPackageSpecOrBuilder multiVmSpec) {
    if (!postDeploy.hasConnectButton() && multiVmSpec.getTiersCount() > 1) {
      return;
    }
    setConnectButtonDefaults_common(postDeploy);
    ConnectToInstanceSpec.Builder connectButton = postDeploy.getConnectButtonBuilder();
    // If connect button does not specify the vm instance, but the spec contains only one tier,
    // use the first instance of this tier in the connect button.
    if (!connectButton.hasTierVm() && multiVmSpec.getTiersCount() == 1) {
      TierVmInstance tier = TierVmInstance.newBuilder()
          .setTier(multiVmSpec.getTiers(0).getName())
          .setIndex(0)
          .build();
      connectButton.setTierVm(tier);
    }
  }

  private static void setConnectButtonDefaults_common(PostDeployInfo.Builder postDeploy) {
    ConnectToInstanceSpec.Builder button = postDeploy.getConnectButtonBuilder();

    if (Strings.isNullOrEmpty(button.getDisplayLabel())
        && Strings.isNullOrEmpty(postDeploy.getConnectButtonLabel())) {
      // Ideally we should allow empty button label, in which case the widget should automatically
      // determine the right label (SSH for Linux, RDP for Windows). We need to change the UI and
      // config validation in order to support this. Once that happens, this default can be dropped.
      button.setDisplayLabel(DEFAULT_CONNECT_BUTTON_LABEL);
    }

    // TODO(khajduczenia) The following copy action should be removed as soon as FE is migrated.
    // Support for deprecated connect_button_label field in PostDeployInfo:
    if (Strings.isNullOrEmpty(button.getDisplayLabel())
        && !Strings.isNullOrEmpty(postDeploy.getConnectButtonLabel())) {
      button.setDisplayLabel(postDeploy.getConnectButtonLabel());
    }
  }

  private static void setInstanceCountDefaults(TierInstanceCount.Builder input) {
    if (Strings.isNullOrEmpty(input.getTooltip()) && input.hasRange()) {
      String tooltip = String.format(DEFAULT_INSTANCE_COUNT_RANGE_TOOLTIP,
          input.getRange().getStartValue(),
          input.getRange().getEndValue());
      input.setTooltip(tooltip);
    }
  }

  private static void setNetworkInterfacesDefault(
      NetworkInterfacesSpec.Builder input, ExternalIpSpec deprecatedExternalIp) {
    if (input.getMinCount() < 1) {
      input.setMinCount(1);
    }

    if (input.getMaxCount() < 1) {
      input.setMaxCount(input.getMinCount());
    }

    if (!input.hasExternalIp() && deprecatedExternalIp != null) {
      input.setExternalIp(deprecatedExternalIp);
    }

    setExternalIpDefaults(input.getExternalIpBuilder());
  }

  private static void setMachineTypeDefaults(MachineTypeSpec.Builder input) {
    if (!input.hasDefaultMachineType()) {
      input.setDefaultMachineType(
          MachineType.newBuilder().setGceMachineType(DEFAULT_MACHINE_TYPE));
    }
  }

  private static void setAceleratorsDefaults(List<AcceleratorSpec.Builder> input) {
    for (AcceleratorSpec.Builder accelerator : input) {
      if (accelerator.getDefaultType().isEmpty()) {
        accelerator.setDefaultType(accelerator.getTypes(0));
      }
    }
  }

  private static void setDiskSizeDefaults(DiskSpec.Builder input,
      int defaultSizeGb, int defaultMinSizeGb) {
    if (!input.hasDiskSize()) {
      input.getDiskSizeBuilder()
          .setDefaultSizeGb(defaultSizeGb)
          .setMinSizeGb(defaultMinSizeGb);
    }
  }

  private static void setDiskTypeDefaults(DiskSpec.Builder input, String defaultType) {
    if (!input.hasDiskType()) {
      input.getDiskTypeBuilder().setDefaultType(defaultType);
    }
  }

  private static void setBootDiskDefaults(DiskSpec.Builder input) {
    final int diskSizeGb = DEFAULT_BOOT_DISK_SIZE_GB;
    setDiskSizeDefaults(input, diskSizeGb, diskSizeGb);
    setDiskTypeDefaults(input, DEFAULT_BOOT_DISK_TYPE);

    if (Strings.isNullOrEmpty(input.getDisplayLabel())) {
      input.setDisplayLabel(DEFAULT_BOOT_DISK_DISPLAY_LABEL);
    }
  }

  private static void setAdditionalDisksDefaults(List<DiskSpec.Builder> input) {
    final int diskSizeGb = DEFAULT_ADDITIONAL_DISK_SIZE_GB;
    int diskNumber = 1;
    for (DiskSpec.Builder disk : input) {
      setDiskSizeDefaults(disk, diskSizeGb, diskSizeGb);
      setDiskTypeDefaults(disk, DEFAULT_ADDITIONAL_DISK_TYPE);
      if (!disk.hasDeviceNameSuffix()) {
        DeviceName name = DeviceName.newBuilder()
            .setName("disk" + diskNumber)
            .build();
        disk.setDeviceNameSuffix(name);
      }
      diskNumber++;
    }

    if (input.size() == 1) {
      DiskSpec.Builder firstDisk = input.get(0);
      if (Strings.isNullOrEmpty(firstDisk.getDisplayLabel())) {
        firstDisk.setDisplayLabel(DEFAULT_ADDITIONAL_DISK_DISPLAY_LABEL);
      }
    }
  }

  private static void setExternalIpDefaults(ExternalIpSpec.Builder input) {
    if (input.getDefaultType().equals(ExternalIpSpec.Type.TYPE_UNSPECIFIED)) {
      input.setDefaultType(ExternalIpSpec.Type.EPHEMERAL);
    }
  }

  private static void setApplicationStatusDefaults(ApplicationStatusSpec.Builder input) {
    if (input.hasWaiter()) {
      ApplicationStatusSpec.WaiterSpec.Builder waiter = input.getWaiterBuilder();
      if (waiter.hasScript() && waiter.getScript().getCheckTimeoutSecs() == 0) {
        waiter.getScriptBuilder().setCheckTimeoutSecs(waiter.getWaiterTimeoutSecs());
      }
    }
  }

  private static void setPasswordDefaults(List<PasswordSpec.Builder> passwords) {
    if (passwords.size() != 1) {
      return;
    }
    PasswordSpec.Builder onlyPassword = Iterables.getOnlyElement(passwords);
    if (onlyPassword.getDisplayLabel().isEmpty()) {
      onlyPassword.setDisplayLabel(DEFAULT_PASSWORD_LABEL);
    }
  }

  private static void setFirewallDefaults(List<FirewallRuleSpec.Builder> rules) {
    for (FirewallRuleSpec.Builder rule : rules) {
      if (rule.getAllowedSource().equals(TrafficSource.SOURCE_UNSPECIFIED)) {
        rule.setAllowedSource(TrafficSource.PUBLIC);
      }
    }
  }

  private static void setDeployInputFieldsDefaults(DeployInputSpec.Builder deployInput) {
    for (DeployInputSection.Builder section : deployInput.getSectionsBuilderList()) {
      for (DeployInputField.Builder field : section.getFieldsBuilderList()) {
        if (field.getTypeCase() == TypeCase.EMAIL_BOX) {
          setDeployInputFieldsDefaultsToEmailbox(field);
        }
      }
    }
  }

  private static void setDeployInputFieldsDefaultsToEmailbox(DeployInputField.Builder field) {
    if (field.getRequired()) {
      // Override test_default_value, if is not present.
      if (field.getEmailBox().getTestDefaultValue().isEmpty()) {
        if (field.getEmailBox().getDefaultValue().isEmpty()) {
          // Set default, if default_value is not present.
          field.getEmailBoxBuilder().setTestDefaultValue("default-user@example.com");
        } else {
          // Use default_value property to override test_default_value.
          field.getEmailBoxBuilder().setTestDefaultValue(field.getEmailBox().getDefaultValue());
        }
      }
    }

    if (field.getEmailBox().getPlaceholder().isEmpty()) {
      field.getEmailBoxBuilder().setPlaceholder("user@example.com");
    }

    if (field.getEmailBox().getValidation().getDescription().isEmpty()) {
      field.getEmailBoxBuilder()
          .getValidationBuilder()
          .setDescription("Please enter a valid email address");
    }
  }

  private SpecDefaults() {}
}
