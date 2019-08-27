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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.cloud.deploymentmanager.autogen.proto.AcceleratorSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ApplicationStatusSpec;
import com.google.cloud.deploymentmanager.autogen.proto.BooleanExpression;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.EmailBox;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.GroupedBooleanCheckbox;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.IntegerBox;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.IntegerDropdown;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.StringBox;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.StringDropdown;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField.TypeCase;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSection;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSection.Placement;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSpec;
import com.google.cloud.deploymentmanager.autogen.proto.DiskSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ExternalIpSpec;
import com.google.cloud.deploymentmanager.autogen.proto.FirewallRuleSpec;
import com.google.cloud.deploymentmanager.autogen.proto.FirewallRuleSpec.TrafficSource;
import com.google.cloud.deploymentmanager.autogen.proto.GceMetadataItem;
import com.google.cloud.deploymentmanager.autogen.proto.GceMetadataItem.ValueSpecCase;
import com.google.cloud.deploymentmanager.autogen.proto.GceStartupScriptSpec;
import com.google.cloud.deploymentmanager.autogen.proto.GcpAuthScopeSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ImageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.InstanceUrlSpec;
import com.google.cloud.deploymentmanager.autogen.proto.MachineTypeSpec;
import com.google.cloud.deploymentmanager.autogen.proto.MultiVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.NetworkInterfacesSpec;
import com.google.cloud.deploymentmanager.autogen.proto.PasswordSpec;
import com.google.cloud.deploymentmanager.autogen.proto.PostDeployInfo;
import com.google.cloud.deploymentmanager.autogen.proto.PostDeployInfo.ConnectToInstanceSpec;
import com.google.cloud.deploymentmanager.autogen.proto.SingleVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.StackdriverSpec;
import com.google.cloud.deploymentmanager.autogen.proto.VmTierSpec;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * Validates complete specs.
 *
 * @see SpecDefaults
 */
final class SpecValidations {

  protected static final int INFO_ROW_MAX_LENGTH = 128;

  private static final ImmutableSet<String> METADATA_KEY_BLACKLIST = ImmutableSet.of(
      "status-config-url",
      "status-uptime-deadline",
      "status-variable-path",
      "startup-script",
      "startup-script-url");
  private static final ImmutableSet<Integer> VALID_GPU_COUNTS = ImmutableSet.of(0, 1, 2, 4, 8);

  private static final Pattern TIER_NAME_REGEX = Pattern.compile("[a-z0-9]+");

  private static final ImmutableSet<String> SUPPORTED_ACCELERATOR_TYPES =
      ImmutableSet.of(
          "nvidia-tesla-k80",
          "nvidia-tesla-p100",
          "nvidia-tesla-v100",
          "nvidia-tesla-p100-vws",
          "nvidia-tesla-p4",
          "nvidia-tesla-p4-vws",
          "nvidia-tesla-t4");

  private static final int MAX_NICS = 8;

  /**
   * Validates that a spec is complete and reasonable.
   *
   * @throws IllegalArgumentException
   */
  public static void validate(SingleVmDeploymentPackageSpec input) {
    validateImages(input.getImagesList());
    validateBootDisk(input.getBootDisk());
    validateAdditionalDisks(input.getAdditionalDisksList());
    validateMachineType(input.getMachineType());
    validateNetworkInterfaces(input.getNetworkInterfaces());
    validateSingleVmFirewallRules(input.getFirewallRulesList());
    validateSingleVmPasswords(input.getPasswordsList());
    if (input.hasAdminUrl()) {
      validateSingleVmInstanceUrl(input.getAdminUrl(), "Admin URL");
    }
    if (input.hasSiteUrl()) {
      validateSingleVmInstanceUrl(input.getSiteUrl(), "Site URL");
    }
    validateGcpAuthScopes(input.getGcpAuthScopesList());
    if (input.hasGceStartupScript()) {
      validateStartupScript(input.getGceStartupScript());
    }
    if (input.hasApplicationStatus()) {
      validateApplicationStatus(input.getApplicationStatus());
    }
    if (input.hasPostDeploy()) {
      validateSingleVmPostDeployInfo(input.getPostDeploy());
    }
    if (input.hasDeployInput()) {
      validateDeployInput(input.getDeployInput());
    }
    if (input.hasStackdriver()) {
      validateStackdriver(input.getStackdriver());
    }
    validateSingleVmGceMetadataItems(input.getGceMetadataItemsList());
    validateMetadataKeyUniqueness(input);
    validateAccelerators(input.getAcceleratorsList());
  }

  /**
   * Validates that a spec is complete and reasonable.
   *
   * @throws IllegalArgumentException
   */
  public static void validate(MultiVmDeploymentPackageSpec input) {
    checkArgument(input.getTiersCount() > 0, "At least one tier must be specified");
    for (VmTierSpec tier : input.getTiersList()) {
      checkArgument(
          TIER_NAME_REGEX.matcher(tier.getName()).matches(),
          "Tier must have a valid lowercased name");
      checkArgument(tier.getTitle().length() > 0, "Tier must have a valid title");
      validateImages(tier.getImagesList());
      validateBootDisk(tier.getBootDisk());
      validateAdditionalDisks(tier.getAdditionalDisksList());
      validateMachineType(tier.getMachineType());
      validateNetworkInterfaces(tier.getNetworkInterfaces());
      validateMultiVmFirewallRules(tier.getFirewallRulesList());
      validateGcpAuthScopes(tier.getGcpAuthScopesList());
      if (tier.hasGceStartupScript()) {
        validateStartupScript(tier.getGceStartupScript());
      }
      if (tier.hasApplicationStatus()) {
        validateApplicationStatus(tier.getApplicationStatus());
      }
      validateMultiVmGceMetadataItems(tier.getGceMetadataItemsList());
      validateAccelerators(tier.getAcceleratorsList());
    }
    validateMultiVmPasswords(input.getPasswordsList());
    if (input.hasAdminUrl()) {
      validateMultiVmInstanceUrl(input.getAdminUrl(), "Admin URL");
    }
    if (input.hasSiteUrl()) {
      validateMultiVmInstanceUrl(input.getSiteUrl(), "Site URL");
    }
    if (input.hasPostDeploy()) {
      validateMultiVmPostDeployInfo(input.getPostDeploy());
    }
    if (input.hasDeployInput()) {
      validateDeployInput(input.getDeployInput());
    }
    if (input.hasStackdriver()) {
      validateStackdriver(input.getStackdriver());
    }
    validateMetadataKeyUniqueness(input);
  }

  private static void validateCommonDiskOptions(DiskSpec input) {
    checkArgument(input.hasDiskSize(), "Disk size must be specified");
    checkArgument(
        input.getDiskSize().getDefaultSizeGb() > 0,
        "A valid default disk size must be specified");
    checkArgument(input.hasDiskType(), "Disk type must be specified");
    checkArgument(
        !input.getDiskType().getDefaultType().isEmpty(),
        "A valid default disk type must be specified");
    checkArgument(
        !input.getDisplayLabel().isEmpty(),
        "A valid disk display label must be specified");
  }

  private static void validateBootDisk(DiskSpec input) {
    validateCommonDiskOptions(input);
  }

  private static void validateAdditionalDisks(List<DiskSpec> input) {
    for (DiskSpec disk : input) {
      validateCommonDiskOptions(disk);
      checkArgument(disk.hasDeviceNameSuffix(),
          "Disk must have a device name specified");
    }
  }

  private static void validateSingleVmFirewallRules(List<FirewallRuleSpec> firewallRules) {
    validateCommonFirewallRules(firewallRules);
    for (FirewallRuleSpec rule : firewallRules) {
      checkArgument(
          rule.getAllowedSource() == TrafficSource.PUBLIC,
          "SingleVM only supports PUBLIC firewall rules");
    }
  }

  private static void validateMultiVmFirewallRules(List<FirewallRuleSpec> firewallRules) {
    validateCommonFirewallRules(firewallRules);
  }

  private static void validateCommonFirewallRules(List<FirewallRuleSpec> firewallRules) {
    for (FirewallRuleSpec rule : firewallRules) {
      checkArgument(
          rule.getAllowedSource() != TrafficSource.SOURCE_UNSPECIFIED,
          "A firewall rule must have a valid source");
      checkArgument(
          rule.getProtocol() != FirewallRuleSpec.Protocol.PROTOCOL_UNSPECIFIED,
          "A firewall rule must have a valid protocol");
    }
  }

  private static void validateGcpAuthScopes(List<GcpAuthScopeSpec> gcpAuthScopes) {
    Set<GcpAuthScopeSpec.Scope> seenScopes = new HashSet<>();
    for (GcpAuthScopeSpec spec : gcpAuthScopes) {
      checkArgument(
          spec.getScope() != GcpAuthScopeSpec.Scope.SCOPE_UNSPECIFIED,
          "The GcpAuthScopeSpec must have a valid scope value");
      checkArgument(
          seenScopes.add(spec.getScope()),
          "Duplicate scope in GcpAuthScopeSpec list: " + spec.getScope());
    }
  }

  private static void validateImages(List<ImageSpec> imageSpecs) {
    checkArgument(imageSpecs.size() >= 1, "At least one image must be specified");
    for (ImageSpec imageSpec : imageSpecs) {
      validateImage(imageSpec);
    }
  }

  private static void validateImage(ImageSpec imageSpec) {
    checkArgument(!imageSpec.getName().isEmpty(), "Image must have a valid name");
    checkArgument(!imageSpec.getProject().isEmpty(), "Image must have a valid project");
  }

  private static void validateSingleVmInstanceUrl(@Nullable InstanceUrlSpec spec, String urlName) {
    validateCommonInstanceUrl(spec, urlName);
  }

  private static void validateMultiVmInstanceUrl(@Nullable InstanceUrlSpec spec, String urlName) {
    if (spec == null) {
      return;
    }
    validateCommonInstanceUrl(spec, urlName);
    checkArgument(
        spec.hasTierVm(),
        "A tier VM is required for " + urlName);
    checkArgument(
        spec.getTierVm().getTier().length() > 0,
        "Tier VM must have a valid tier for " + urlName);
  }

  private static void validateCommonInstanceUrl(@Nullable InstanceUrlSpec spec, String urlName) {
    if (spec == null) {
      return;
    }
    checkArgument(
        spec.getScheme() != InstanceUrlSpec.Scheme.SCHEME_UNSPECIFIED,
        "A scheme is required for %s",
        urlName);
  }

  private static void validateSingleVmInstanceConnectSpec(
      ConnectToInstanceSpec spec, PostDeployInfo postDeployInfo) {
    validateCommonInstanceConnectSpec(spec, postDeployInfo);

    checkArgument(
        !spec.hasTierVm(),
        "Tier VM must not be specified for a single vm spec");
  }

  private static void validateMultiVmInstanceConnectSpec(
      ConnectToInstanceSpec spec, PostDeployInfo postDeployInfo) {
    validateCommonInstanceConnectSpec(spec, postDeployInfo);

    checkArgument(
        spec.hasTierVm() && !Strings.isNullOrEmpty(spec.getTierVm().getTier()),
        "Multi-vm connect button spec must specify valid tier name"
    );
  }

  private static void validateCommonInstanceConnectSpec(
      ConnectToInstanceSpec spec, PostDeployInfo postDeployInfo) {
    // Keep support for deprecated attribute of connect_button_label.
    // If the Autogen spec has different values specified for both, the display_label of
    // the connect button and connect_button_label of post deploy info, it should fail.
    checkArgument(
        Strings.isNullOrEmpty(spec.getDisplayLabel())
        || Strings.isNullOrEmpty(postDeployInfo.getConnectButtonLabel())
        || spec.getDisplayLabel().equals(postDeployInfo.getConnectButtonLabel()),
        "At most only one of connect button's display_label and post deploy's "
            + "connect_button_label can be specified, or they must have the same value");
  }

  private static void validateMachineType(MachineTypeSpec machineType) {
    checkArgument(
        !machineType.getDefaultMachineType().getGceMachineType().isEmpty(),
        "Default machine type name must be valid");
    if (machineType.hasMinimum()) {
      // If unset, these values will be zero (which is allowed)
      checkArgument(machineType.getMinimum().getCpu() >= 0, "Minimum CPUs must be nonnegative");
      checkArgument(machineType.getMinimum().getRamGb() >= 0, "Minimum RAM must be nonnegative");
    }
  }

  private static void validateNetworkInterfaces(NetworkInterfacesSpec spec) {
    int min = spec.getMinCount();
    int max = spec.getMaxCount();
    checkArgument(min > 0, "Minimum number of Network interfaces must be greater than 0.");
    checkArgument(
        max >= min && max <= MAX_NICS,
        String.format(
            "Maxmium number of Network interfaces must be greater than minimum and at most %d.",
            MAX_NICS));
    checkArgument(
        spec.getLabelsCount() <= spec.getMinCount() + 1,
        "The number of labels must not exceed min_count + 1.");
    validateExternalIp(spec.getExternalIp());
  }

  @VisibleForTesting
  static void validateExternalIp(ExternalIpSpec externalIp) {
    checkArgument(
        externalIp.getDefaultType() != ExternalIpSpec.Type.TYPE_UNSPECIFIED,
        "External IP default type must have a valid type");
  }

  @VisibleForTesting
  static void validateSingleVmPasswords(List<PasswordSpec> passwords) {
    for (PasswordSpec password : passwords) {
      validateCommonPassword(password);
      if (password.hasGenerateIf()) {
        validateSingleVmBooleanExpression(password.getGenerateIf());
      }
    }
  }

  @VisibleForTesting
  static void validateMultiVmPasswords(List<PasswordSpec> passwords) {
    for (PasswordSpec password : passwords) {
      validateCommonPassword(password);
      if (password.hasGenerateIf()) {
        validateMultiVmBooleanExpression(password.getGenerateIf());
      }
    }
  }

  private static void validateCommonPassword(PasswordSpec password) {
    checkArgument(
        !password.getMetadataKey().isEmpty(), "Password must have a valid metadata key");
    checkArgument(password.getLength() > 0, "Password must have a valid length");
    checkArgument(
        !password.getDisplayLabel().isEmpty(), "Password must have a valid display label");
    checkArgument(
        !METADATA_KEY_BLACKLIST.contains(password.getMetadataKey()),
        "Password metadata key cannot be one of " + METADATA_KEY_BLACKLIST);
  }

  @VisibleForTesting
  static void validateApplicationStatus(ApplicationStatusSpec appStatus) {
    if (appStatus.getType() == ApplicationStatusSpec.StatusType.WAITER) {
      checkArgument(
          appStatus.hasWaiter(), "Application status of type WAITER must have a valid waiter spec");
      checkArgument(
          appStatus.getWaiter().getWaiterTimeoutSecs() > 0,
          "Application status waiter must have a valid timeout");
      if (appStatus.getWaiter().hasScript()) {
        checkArgument(
            appStatus.getWaiter().getScript().getCheckTimeoutSecs() > 0,
            "Application status waiter script must have a valid timeout");
      }
    } else {
      checkArgument(
          !appStatus.hasWaiter(),
          "Waiter spec should only be set for an application status of type WAITER");
    }
  }

  @VisibleForTesting
  static void validateSingleVmPostDeployInfo(PostDeployInfo postDeployInfo) {
    for (PostDeployInfo.ActionItem item : postDeployInfo.getActionItemsList()) {
      validateCommonActionItem(item);
      if (item.hasShowIf()) {
        validateSingleVmBooleanExpression(item.getShowIf());
      }
    }

    for (PostDeployInfo.InfoRow row : postDeployInfo.getInfoRowsList()) {
      validateCommonInfoRow(row);
      if (row.hasShowIf()) {
        validateSingleVmBooleanExpression(row.getShowIf());
      }
    }

    if (postDeployInfo.hasConnectButton()) {
      validateSingleVmInstanceConnectSpec(postDeployInfo.getConnectButton(), postDeployInfo);
    }
  }

  @VisibleForTesting
  static void validateMultiVmPostDeployInfo(PostDeployInfo postDeployInfo) {
    for (PostDeployInfo.ActionItem item : postDeployInfo.getActionItemsList()) {
      validateCommonActionItem(item);
      if (item.hasShowIf()) {
        validateMultiVmBooleanExpression(item.getShowIf());
      }
    }

    for (PostDeployInfo.InfoRow row : postDeployInfo.getInfoRowsList()) {
      validateCommonInfoRow(row);
      if (row.hasShowIf()) {
        validateMultiVmBooleanExpression(row.getShowIf());
      }
    }

    if (postDeployInfo.hasConnectButton()) {
      validateMultiVmInstanceConnectSpec(postDeployInfo.getConnectButton(), postDeployInfo);
    }
  }

  private static void validateCommonActionItem(PostDeployInfo.ActionItem item) {
    checkArgument(
        !item.getHeading().isEmpty(), "Post deploy action item must have a valid heading");
    checkArgument(
        !item.getDescription().isEmpty() || !item.getSnippet().isEmpty(),
        "Post deploy action item must have at least description or snippet");
  }

  private static void validateCommonInfoRow(PostDeployInfo.InfoRow row) {
    switch (row.getValueSpecCase()) {
      case VALUE:
        checkArgument(
            row.getValue().length() <= INFO_ROW_MAX_LENGTH,
            String.format(
                "Info row value length cannot exceed the limit of %d characters",
                INFO_ROW_MAX_LENGTH));
        break;
      case VALUE_FROM_DEPLOY_INPUT_FIELD:
        checkArgument(
            !Strings.isNullOrEmpty(row.getValueFromDeployInputField()),
            "Expected non-empty deploy input field name for info row value");
        break;
      default:
        throw new IllegalArgumentException("Unexpected info row value");
    }
  }

  @VisibleForTesting
  static void validateSingleVmBooleanExpression(BooleanExpression spec) {
    validateCommonBooleanExpression(spec);
    if (spec.hasHasExternalIp()) {
      checkArgument(
          Strings.isNullOrEmpty(spec.getHasExternalIp().getTier()),
          "Tier attribute must not be specified for has_external_ip boolean "
              + "expression in a single VM's spec");
    }
  }

  @VisibleForTesting
  static void validateMultiVmBooleanExpression(BooleanExpression spec) {
    validateCommonBooleanExpression(spec);
    if (spec.hasHasExternalIp()) {
      checkArgument(
          !Strings.isNullOrEmpty(spec.getHasExternalIp().getTier()),
          "Tier attribute must be specified for has_external_ip boolean expression "
              + "in a multi VM's spec");
    }
  }

  private static void validateCommonBooleanExpression(BooleanExpression spec) {
    switch (spec.getExpressionCase()) {
      case BOOLEAN_DEPLOY_INPUT_FIELD:
        checkArgument(
            !Strings.isNullOrEmpty(spec.getBooleanDeployInputField().getName()),
            "Boolean expression for deploy input field must specify the field's name");
        break;
      case HAS_EXTERNAL_IP:
        // No additional validation rules.
        break;
      default:
        throw new IllegalArgumentException("Unknown configuration for BooleanExpression");
    }
  }

  @VisibleForTesting
  static void validateSingleVmGceMetadataItems(List<GceMetadataItem> items) {
    for (GceMetadataItem item : items) {
      checkArgument(!item.getKey().isEmpty(), "GCE metadata item must have a valid key");
      checkArgument(
          !METADATA_KEY_BLACKLIST.contains(item.getKey()),
          "GCE metadata item key cannot be one of " + METADATA_KEY_BLACKLIST);
      checkArgument(
          item.getValueSpecCase() != ValueSpecCase.VALUESPEC_NOT_SET,
          "GCE metadata item must have a valid value");
      checkArgument(
          item.getValueSpecCase() != ValueSpecCase.TIER_VM_NAMES,
          "GCE metadata item for single VM cannot specify tier VM");
    }
  }

  @VisibleForTesting
  static void validateMultiVmGceMetadataItems(List<GceMetadataItem> items) {
    for (GceMetadataItem item : items) {
      checkArgument(!item.getKey().isEmpty(), "GCE metadata item must have a valid key");
      checkArgument(
          !METADATA_KEY_BLACKLIST.contains(item.getKey()),
          "GCE metadata item key cannot be one of " + METADATA_KEY_BLACKLIST);
      checkArgument(
          item.getValueSpecCase() != ValueSpecCase.VALUESPEC_NOT_SET,
          "GCE metadata item must have a valid value");
    }
  }

  private static void validateMetadataKeyUniqueness(SingleVmDeploymentPackageSpec spec) {
    // Ensures that metadata keys are unique.
    Multiset<String> metadataKeyCounts = HashMultiset.create();
    for (PasswordSpec password : spec.getPasswordsList()) {
      metadataKeyCounts.add(password.getMetadataKey());
    }
    for (GceMetadataItem metadataItem : spec.getGceMetadataItemsList()) {
      metadataKeyCounts.add(metadataItem.getKey());
    }
    for (Multiset.Entry<String> entry : metadataKeyCounts.entrySet()) {
      if (entry.getCount() > 1) {
        throw new IllegalArgumentException(
            String.format("Metadata key '%s' is not unique", entry.getElement()));
      }
    }
  }

  private static void validateMetadataKeyUniqueness(MultiVmDeploymentPackageSpec spec) {
    // Ensures that metadata keys are unique.
    Multiset<String> metadataKeyCounts = HashMultiset.create();
    for (PasswordSpec password : spec.getPasswordsList()) {
      metadataKeyCounts.add(password.getMetadataKey());
    }
    for (VmTierSpec tier : spec.getTiersList()) {
      Multiset<String> perTier = HashMultiset.create(metadataKeyCounts);
      for (GceMetadataItem metadataItem : tier.getGceMetadataItemsList()) {
        perTier.add(metadataItem.getKey());
      }
      for (Multiset.Entry<String> entry : perTier.entrySet()) {
        if (entry.getCount() > 1) {
          throw new IllegalArgumentException(
              String.format("Metadata key '%s' is not unique", entry.getElement()));
        }
      }
    }
  }

  @VisibleForTesting
  static void validateAccelerators(List<AcceleratorSpec> accelerators) {
    if (accelerators.isEmpty()) {
      return;
    }
    checkArgument(accelerators.size() <= 1, "At most one accelerator allowed.");

    AcceleratorSpec accelerator = accelerators.get(0);
    checkArgument(accelerator.getTypesList().size() >= 1,
        "Accelerators must have at least one type.");
    Set<String> gpuTypes = ImmutableSet.copyOf(accelerator.getTypesList());
    Set<String> unsupportedTypes = Sets.difference(gpuTypes, SUPPORTED_ACCELERATOR_TYPES);
    checkArgument(unsupportedTypes.isEmpty(),
        "Unsupported accelerator types: %s", unsupportedTypes);
    checkArgument(accelerator.getMinCount() >= 0, "Accelerator min count must not be negative.");
    checkArgument(VALID_GPU_COUNTS.contains(accelerator.getMinCount()),
        "Accelerator min count must be one of: %s", VALID_GPU_COUNTS.toString());
    if (accelerator.getMaxCount() != 0) {
      checkArgument(accelerator.getMaxCount() > 0, "Accelerator max count must be greater than 0.");
      checkArgument(accelerator.getMinCount() <= accelerator.getMaxCount(),
          "Accelerator min count must not be greater than max count.");
      checkArgument(VALID_GPU_COUNTS.contains(accelerator.getMaxCount()),
          "Accelerator max count must be one of: %s", VALID_GPU_COUNTS.toString());
    }
    if (accelerator.getDefaultCount() != 0) {
      checkArgument(accelerator.getDefaultCount() >= 0,
          "Accelerator default count must not be negative.");
      checkArgument(accelerator.getDefaultCount() >= accelerator.getMinCount(),
          "Accelerator default count must not be less than min count.");
      if (accelerator.getMaxCount() != 0) {
        checkArgument(accelerator.getDefaultCount() <= accelerator.getMaxCount(),
            "Accelerator default count must not be greater than max count.");
      }
      checkArgument(VALID_GPU_COUNTS.contains(accelerator.getDefaultCount()),
          "Accelerator default count must be one of: %s", VALID_GPU_COUNTS.toString());
    }
    if (!accelerator.getDefaultType().isEmpty()) {
      checkArgument(
          gpuTypes.contains(accelerator.getDefaultType()),
          "Default Accelerator Type must be one of %s",
          accelerator.getTypesList());
    }
  }

  @VisibleForTesting
  static void validateDeployInput(DeployInputSpec spec) {
    Set<String> runningSectionNames = new HashSet<>();
    Set<String> runningFieldNames = new HashSet<>();
    Set<String> runningDisplayGroups = new HashSet<>();
    for (DeployInputSection section : spec.getSectionsList()) {
      checkArgument(section.getPlacement() != Placement.PLACEMENT_UNSPECIFIED,
          "Deploy input section must have a valid placement");
      switch (section.getPlacement()) {
        case MAIN:
          break;
        case TIER:
          checkArgument(
              section.getTier().length() > 0, "Tier input section must have a valid tier name");
          break;
        default:
          checkArgument(
              !section.getName().isEmpty(), "Custom deploy input section must have valid name");
          if (!runningSectionNames.add(section.getName())) {
            throw new IllegalArgumentException(
                "Deploy input sections with the same name: " + section.getName());
          }
          checkArgument(
              !section.getTitle().isEmpty(), "Custom deploy input section must have a valid title");
      }
      checkArgument(section.getFieldsCount() > 0,
          "Deploy input section must have at least 1 field");
      validateDeployInputFields(section.getFieldsList(), runningFieldNames, runningDisplayGroups);
    }
  }

  @VisibleForTesting
  static void validateStartupScript(GceStartupScriptSpec startupScript) {
    checkArgument(
        !Strings.isNullOrEmpty(startupScript.getBashScriptContent()),
        "Startup script must specify non-empty script content");
  }

  @VisibleForTesting
  static void validateDeployInputFields(
      List<DeployInputField> fields,
      Set<String> runningFieldNames,
      Set<String> runningDisplayGroups) {
    boolean isInBooleanGroup = false;
    for (DeployInputField field : fields) {
      checkArgument(!field.getName().isEmpty(), "Deploy input field must have a valid name");
      if (!runningFieldNames.add(field.getName())) {
        throw new IllegalArgumentException(
            "Deploy input fields with the same name: " + field.getName());
      }
      checkArgument(!field.getTitle().isEmpty(), "Deploy input field must have a valid title");

      switch (field.getTypeCase()) {
        case BOOLEAN_CHECKBOX:
          break;
        case GROUPED_BOOLEAN_CHECKBOX: {
          GroupedBooleanCheckbox checkbox = field.getGroupedBooleanCheckbox();
          if (!isInBooleanGroup) {
            checkArgument(checkbox.hasDisplayGroup(),
                String.format("The first grouped boolean checkbox '%s' must have a display group",
                    field.getName()));
          }
          if (checkbox.hasDisplayGroup()) {
            GroupedBooleanCheckbox.DisplayGroup displayGroup = checkbox.getDisplayGroup();
            checkArgument(!displayGroup.getName().isEmpty(),
                String.format("Field '%s' has a display group without a name", field.getName()));
            if (!runningDisplayGroups.add(displayGroup.getName())) {
              throw new IllegalArgumentException(
                  "Display groups with the same name: " + displayGroup.getName());
            }
            checkArgument(!displayGroup.getTitle().isEmpty(),
                String.format("Display group '%s' must have a title", displayGroup.getName()));
          }
          break;
        }
        case INTEGER_BOX: {
          if (field.getRequired()) {
            IntegerBox box = field.getIntegerBox();
            checkArgument(box.hasDefaultValue() || box.hasTestDefaultValue(),
                String.format(
                    "Field '%s' is required - it should have defaultValue or testDefaultValue",
                    field.getName()));
          }
          break;
        }
        case INTEGER_DROPDOWN: {
          IntegerDropdown dropdown = field.getIntegerDropdown();
          checkArgument(dropdown.getValuesCount() > 0,
              String.format("Field '%s' must have at least 1 value", field.getName()));
          if (dropdown.hasDefaultValueIndex()) {
            checkArgument(dropdown.getDefaultValueIndex().getValue() < dropdown.getValuesCount(),
                String.format(
                    "Invalid default index %d while there are only %d values in field '%s'",
                    dropdown.getDefaultValueIndex().getValue(),
                    dropdown.getValuesCount(),
                    field.getName()));
          }
          break;
        }
        case STRING_BOX: {
          if (field.getRequired()) {
            StringBox box = field.getStringBox();
            boolean hasDefaultValue = !Strings.isNullOrEmpty(box.getDefaultValue());
            boolean hasTestDefaultValue = !Strings.isNullOrEmpty(box.getTestDefaultValue());
            checkArgument(hasDefaultValue || hasTestDefaultValue,
                String.format(
                    "Field '%s' is required - it should have defaultValue or testDefaultValue",
                    field.getName()));
          }
          break;
        }
        case STRING_DROPDOWN: {
          StringDropdown dropdown = field.getStringDropdown();
          checkArgument(dropdown.getValuesCount() > 0,
              String.format("Field '%s' must have at least 1 value", field.getName()));
          if (dropdown.hasDefaultValueIndex()) {
            checkArgument(dropdown.getDefaultValueIndex().getValue() < dropdown.getValuesCount(),
                String.format(
                    "Invalid default index %d while there are only %d values in field '%s'",
                    dropdown.getDefaultValueIndex().getValue(),
                    dropdown.getValuesCount(),
                    field.getName()));
          }
          break;
        }
        case ZONE_DROPDOWN:
          break;
        case EMAIL_BOX:
          if (field.getRequired()) {
            EmailBox box = field.getEmailBox();
            // EmailBox should always have a testDefaultValue.
            // It's either being specified by defaultValue,
            // explicitly by testDefaultValue, or being filled in by defaults.
            boolean hasTestDefaultValue = !box.getTestDefaultValue().isEmpty();
            checkArgument(
                hasTestDefaultValue,
                "Field '%s' is required - it should have testDefaultValue",
                field.getName());
          }
          break;
        case TYPE_NOT_SET:
          throw new IllegalArgumentException("Deploy input field must be of exactly one type");
      }

      isInBooleanGroup = field.getTypeCase() == TypeCase.GROUPED_BOOLEAN_CHECKBOX;
    }
  }

  @VisibleForTesting
  static void validateStackdriver(StackdriverSpec stackdriver) {
    checkArgument(
        stackdriver.hasMonitoring() || stackdriver.hasLogging(),
        "Invalid Stackdriver spec. At least one of logging or monitoring must be specified");
  }

  private SpecValidations() {}
}
