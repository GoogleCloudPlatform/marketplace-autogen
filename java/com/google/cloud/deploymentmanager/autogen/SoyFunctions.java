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

import static com.google.cloud.deploymentmanager.autogen.proto.GceMetadataItem.ValueSpecCase.VALUE_FROM_DEPLOY_INPUT_FIELD;
import static com.google.cloud.deploymentmanager.autogen.proto.LocalSsdSpec.CountSpecCase.COUNT_FROM_DEPLOY_INPUT_FIELD;
import static com.google.template.soy.data.SoyValueConverter.markAsSoyMap;

import com.google.cloud.deploymentmanager.autogen.proto.BooleanExpression;
import com.google.cloud.deploymentmanager.autogen.proto.BooleanExpression.BooleanDeployInputField;
import com.google.cloud.deploymentmanager.autogen.proto.BooleanExpression.ExternalIpAvailability;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputField;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSection;
import com.google.cloud.deploymentmanager.autogen.proto.DeployInputSpec;
import com.google.cloud.deploymentmanager.autogen.proto.DiskSpec;
import com.google.cloud.deploymentmanager.autogen.proto.DiskSpec.DeviceName.DeviceNameCase;
import com.google.cloud.deploymentmanager.autogen.proto.ExternalIpSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ExternalIpSpec.Type;
import com.google.cloud.deploymentmanager.autogen.proto.GceMetadataItem;
import com.google.cloud.deploymentmanager.autogen.proto.MultiVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.SingleVmDeploymentPackageSpec;
import com.google.cloud.deploymentmanager.autogen.proto.VmTierSpec;
import com.google.cloud.deploymentmanager.autogen.proto.ZoneSpec;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.protobuf.Message;
import com.google.template.soy.data.SoyList;
import com.google.template.soy.data.SoyProtoValue;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.SoyValueConverter;
import com.google.template.soy.data.restricted.NullData;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.shared.restricted.Signature;
import com.google.template.soy.shared.restricted.SoyFunction;
import com.google.template.soy.shared.restricted.SoyFunctionSignature;
import com.google.template.soy.shared.restricted.SoyJavaFunction;
import com.google.template.soy.shared.restricted.TypedSoyFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Additional soy functions to support our templating.
 */
final class SoyFunctions {

  static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      Multibinder<SoyFunction> soyFunctionSetBinder =
          Multibinder.newSetBinder(binder(), SoyFunction.class);
      soyFunctionSetBinder.addBinding().to(DependentTiers.class);
      soyFunctionSetBinder.addBinding().to(DeployInputFieldIsString.class);
      soyFunctionSetBinder.addBinding().to(DeployInputFieldName.class);
      soyFunctionSetBinder.addBinding().to(FindDeployInputField.class);
      soyFunctionSetBinder.addBinding().to(FindInputsWithTestDefaultValues.class);
      soyFunctionSetBinder.addBinding().to(FindInputTestDefaultValue.class);
      soyFunctionSetBinder.addBinding().to(FindDisplayGroup.class);
      soyFunctionSetBinder.addBinding().to(FindVmTier.class);
      soyFunctionSetBinder.addBinding().to(FieldValueLabelMap.class);
      soyFunctionSetBinder.addBinding().to(TierPrefixed.class);
      soyFunctionSetBinder.addBinding().to(TierTemplateName.class);
      soyFunctionSetBinder.addBinding().to(BooleanExpressionDisplayCondition.class);
      soyFunctionSetBinder.addBinding().to(BooleanExpressionJinjaExpression.class);
      soyFunctionSetBinder.addBinding().to(AdditionalDiskTypePropertyName.class);
      soyFunctionSetBinder.addBinding().to(AdditionalDiskSizePropertyName.class);
      soyFunctionSetBinder.addBinding().to(ListDeployInputFields.class);
      soyFunctionSetBinder.addBinding().to(ExternalIpTypeName.class);
      soyFunctionSetBinder.addBinding().to(SolutionHasGpus.class);
      soyFunctionSetBinder.addBinding().to(GetTestConfigDefaultValues.class);
    }
  }

  /**
   * Returns all a tiers that the tier specified in the first argument depends upon.
   */
  @VisibleForTesting
  @Singleton
  @SoyFunctionSignature(
      name = "dependentTiers",
      value = {
        @Signature(
            parameterTypes = {
              "cloud.deploymentmanager.autogen.VmTierSpec",
              "cloud.deploymentmanager.autogen.MultiVmDeploymentPackageSpec"
            },
            returnType = "list<cloud.deploymentmanager.autogen.VmTierSpec>")
      })
  static final class DependentTiers extends TypedSoyFunction implements SoyJavaFunction {
    @Inject SoyValueConverter converter;

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      VmTierSpec tier = (VmTierSpec) ((SoyProtoValue) args.get(0)).getProto();
      List<VmTierSpec> tierList;
      try {
        tierList = extractTierList(args.get(1));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Unexpected 2nd arg type for dependentTiers", e);
      }

      Set<Integer> dependentIndices = new HashSet<>();
      for (GceMetadataItem item : tier.getGceMetadataItemsList()) {
        if (item.hasTierVmNames()) {
          dependentIndices.add(findTier(item.getTierVmNames().getTier(), tierList));
        }
      }

      // List dependents in the order they appear in the tier list.
      List<VmTierSpec> dependents = new ArrayList<>(dependentIndices.size());
      for (int i = 0; i < tierList.size(); i++) {
        if (dependentIndices.contains(i)) {
          dependents.add(tierList.get(i));
        }
      }
      return converter.convert(dependents).resolve();
    }

    private int findTier(String name, List<VmTierSpec> tierList) {
      for (int i = 0; i < tierList.size(); i++) {
        if (name.equals(tierList.get(i).getName())) {
          return i;
        }
      }
      throw new IllegalArgumentException("Unable to find tier with name: " + name);
    }
  }

  @Singleton
  @SoyFunctionSignature(
      name = "deployInputFieldIsString",
      value = {
        @Signature(
            parameterTypes = {"cloud.deploymentmanager.autogen.DeployInputField"},
            returnType = "bool")
      })
  private static final class DeployInputFieldIsString extends TypedSoyFunction
      implements SoyJavaFunction {
    @Inject SoyValueConverter converter;

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      DeployInputField field = (DeployInputField) ((SoyProtoValue) args.get(0)).getProto();
      switch (field.getTypeCase()) {
        case STRING_BOX:
        case EMAIL_BOX:
        case STRING_DROPDOWN:
        case ZONE_DROPDOWN:
          return converter.convert(true).resolve();
        default:
          return converter.convert(false).resolve();
      }
    }
  }

  /**
   * Derives the name for a deploy input field.
   *
   * <p>While we can take a string instead, forcing a proper field definition prevents the mistake
   * of entering an invalid field name. {@link FindDeployInputField} can be used to look up the
   * field definition from a field name, which will ensure that any invalid field name will fail.
   */
  @VisibleForTesting
  @Singleton
  @SoyFunctionSignature(
      name = "deployInputFieldName",
      value = {
        @Signature(
            parameterTypes = {"cloud.deploymentmanager.autogen.DeployInputField"},
            returnType = "string")
      })
  static final class DeployInputFieldName extends TypedSoyFunction implements SoyJavaFunction {
    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      DeployInputField field = (DeployInputField) ((SoyProtoValue) args.get(0)).getProto();
      return StringData.forValue(formatFieldName(field.getName()));
    }

    public static String formatFieldName(String fieldSpecName) {
      return String.format("input_%s", fieldSpecName);
    }
  }

  /**
   * Finds the input field definition from the field name.
   */
  @VisibleForTesting
  @Singleton
  @SoyFunctionSignature(
      name = "findDeployInputField",
      value = {
        @Signature(
            parameterTypes = {"string", "cloud.deploymentmanager.autogen.DeployInputSpec|null"},
            returnType = "cloud.deploymentmanager.autogen.DeployInputField")
      })
  static final class FindDeployInputField extends TypedSoyFunction implements SoyJavaFunction {
    @Inject SoyValueConverter converter;

    @Override
    public SoyValue computeForJava(List<SoyValue> list) {
      String name = list.get(0).stringValue();
      DeployInputSpec deployInputSpec = (DeployInputSpec) ((SoyProtoValue) list.get(1)).getProto();
      for (DeployInputSection section : deployInputSpec.getSectionsList()) {
        for (DeployInputField field : section.getFieldsList()) {
          if (name.equals(field.getName())) {
            return converter.convert(field).resolve();
          }
        }
      }
      throw new IllegalArgumentException("Unable to find deploy input field named: " + name);
    }
  }

  /**
   * Finds the collection of required input fields that have no defaultValue.
   */
  @VisibleForTesting
  @Singleton
  @SoyFunctionSignature(
      name = "findInputsWithTestDefaultValues",
      value = {
        @Signature(
            parameterTypes = {"cloud.deploymentmanager.autogen.DeployInputSpec"},
            returnType = "list<cloud.deploymentmanager.autogen.DeployInputField>")
      })
  static final class FindInputsWithTestDefaultValues extends TypedSoyFunction
      implements SoyJavaFunction {
    @Inject SoyValueConverter converter;

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      DeployInputSpec deployInputSpec = (DeployInputSpec) ((SoyProtoValue) args.get(0)).getProto();
      List<DeployInputField> filteredFields = new ArrayList<>();
      for (DeployInputSection section : deployInputSpec.getSectionsList()) {
        for (DeployInputField field : section.getFieldsList()) {
          if (hasTestDefaultValue(field)) {
            filteredFields.add(field);
          }
        }
      }
      return converter.convert(filteredFields).resolve();
    }

    private boolean hasTestDefaultValue(DeployInputField field) {
      switch (field.getTypeCase()) {
        case STRING_BOX:
          return !Strings.isNullOrEmpty(field.getStringBox().getTestDefaultValue());
        case EMAIL_BOX:
          return !field.getEmailBox().getTestDefaultValue().isEmpty();
        case INTEGER_BOX:
          return field.getIntegerBox().hasTestDefaultValue();
        default:
          return false;
      }
    }
  }

  /**
   * Finds the default test value for given input field.
   */
  @VisibleForTesting
  @Singleton
  @SoyFunctionSignature(
      name = "findInputTestDefaultValue",
      value = {
        @Signature(
            parameterTypes = {"cloud.deploymentmanager.autogen.DeployInputField"},
            returnType = "string|int")
      })
  static final class FindInputTestDefaultValue extends TypedSoyFunction implements SoyJavaFunction {
    @Inject SoyValueConverter converter;

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      DeployInputField field = (DeployInputField) ((SoyProtoValue) args.get(0)).getProto();
      Object value;
      switch (field.getTypeCase()) {
        case STRING_BOX:
          value = field.getStringBox().getTestDefaultValue();
          break;
        case EMAIL_BOX:
          value = field.getEmailBox().getTestDefaultValue();
          break;
        case INTEGER_BOX:
          value = field.getIntegerBox().getTestDefaultValue().getValue();
          break;
        default:
          throw new IllegalArgumentException(
              String.format(
                  "Unexpected field type '%s' for field '%s')",
                  field.getTypeCase(),
                  field.getName()));
      }
      return converter.convert(value).resolve();
    }
  }

  @VisibleForTesting
  @Singleton
  @SoyFunctionSignature(
      name = "getTestConfigDefaultValues",
      value = {
        @Signature(
            parameterTypes = {
              "cloud.deploymentmanager.autogen.MultiVmDeploymentPackageSpec|cloud.deploymentmanager.autogen.SingleVmDeploymentPackageSpec"
            },
            returnType = "map<string, string>")
      })
  static final class GetTestConfigDefaultValues extends TypedSoyFunction
      implements SoyJavaFunction {
    @Inject SoyValueConverter converter;

    private static final String DEFAULT_ZONE_PROP_NAME = "zone";
    private static final String DEFAULT_ZONE = "us-central1-f";

    private static Map<String, String> getZoneDefaultValue(ZoneSpec zoneSpec) {
      ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
      if (zoneSpec.getDefaultZone().isEmpty()) {
        result.put(DEFAULT_ZONE_PROP_NAME, DEFAULT_ZONE);
      }
      return result.build();
    }

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      Message message = ((SoyProtoValue) args.get(0)).getProto();
      if (message instanceof SingleVmDeploymentPackageSpec) {
        return converter
            .convert(
                markAsSoyMap(
                    getZoneDefaultValue(((SingleVmDeploymentPackageSpec) message).getZone())))
            .resolve();
      }

      return converter
          .convert(
              markAsSoyMap(getZoneDefaultValue(((MultiVmDeploymentPackageSpec) message).getZone())))
          .resolve();
    }
  }

  /**
   * Finds the display group for a grouped boolean checkbox field.
   */
  @VisibleForTesting
  @Singleton
  @SoyFunctionSignature(
      name = "findDisplayGroup",
      value = {
        @Signature(
            parameterTypes = {
              "cloud.deploymentmanager.autogen.DeployInputField",
              "cloud.deploymentmanager.autogen.DeployInputSection"
            },
            returnType =
                "cloud.deploymentmanager.autogen.DeployInputField.GroupedBooleanCheckbox.DisplayGroup")
      })
  static final class FindDisplayGroup extends TypedSoyFunction implements SoyJavaFunction {
    @Inject SoyValueConverter converter;

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      DeployInputField targetField = (DeployInputField) ((SoyProtoValue) args.get(0)).getProto();
      Preconditions.checkArgument(targetField.hasGroupedBooleanCheckbox());
      DeployInputSection section = (DeployInputSection) ((SoyProtoValue) args.get(1)).getProto();
      boolean foundField = false;
      for (DeployInputField field : Lists.reverse(section.getFieldsList())) {
        if (!foundField) {
          if (targetField.getName().equals(field.getName())) {
            foundField = true;
          }
        }
        if (foundField) {
          if (!field.hasGroupedBooleanCheckbox()) {
            throw new RuntimeException(
                "No preceding grouped boolean checkbox field with a display group");
          }
          if (field.getGroupedBooleanCheckbox().hasDisplayGroup()) {
            return converter.convert(field.getGroupedBooleanCheckbox().getDisplayGroup()).resolve();
          }
        }
      }
      throw new RuntimeException(
          "Unable to locate display group for field " + targetField.getName());
    }
  }

  /** Finds the corresponding {@code VmTierSpec} given its name. */
  @VisibleForTesting
  @Singleton
  @SoyFunctionSignature(
      name = "findVmTier",
      value = {
        @Signature(
            parameterTypes = {"string", "list<cloud.deploymentmanager.autogen.VmTierSpec>|null"},
            returnType = "cloud.deploymentmanager.autogen.VmTierSpec")
      })
  static final class FindVmTier extends TypedSoyFunction implements SoyJavaFunction {
    @Inject SoyValueConverter converter;

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      String tierName = args.get(0).stringValue();
      List<VmTierSpec> tierList;
      try {
        tierList = extractTierList(args.get(1));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Unexpected 2nd arg type for findVmTier function", e);
      }
      for (VmTierSpec tier : tierList) {
        if (tierName.equals(tier.getName())) {
          return converter.convert(tier).resolve();
        }
      }
      throw new RuntimeException("Unable to locate tier with name " + tierName);
    }
  }

  /** Extracts the map of value labels for a deploy input field if any. */
  @Singleton
  @SoyFunctionSignature(
      name = "fieldValueLabelMap",
      value = {
        @Signature(
            parameterTypes = {"cloud.deploymentmanager.autogen.DeployInputField"},
            returnType = "map<string, string>")
      })
  private static final class FieldValueLabelMap extends TypedSoyFunction
      implements SoyJavaFunction {
    @Inject SoyValueConverter soyValueConverter;

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      DeployInputField field = (DeployInputField) ((SoyProtoValue) args.get(0)).getProto();
      if (field.hasIntegerDropdown()) {
        if (!field.getIntegerDropdown().getValueLabelsMap().isEmpty()) {
          return soyValueConverter
              .convert(toStringMap(field.getIntegerDropdown().getValueLabelsMap()))
              .resolve();
        }
      } else if (field.hasStringDropdown()) {
        if (!field.getStringDropdown().getValueLabelsMap().isEmpty()) {
          return soyValueConverter.convert(field.getStringDropdown().getValueLabelsMap()).resolve();
        }
      }
      return NullData.INSTANCE;
    }

    private <K, V> ImmutableMap<String, String> toStringMap(Map<K, V> from) {
      ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
      for (Map.Entry<K, V> entry : from.entrySet()) {
        result.put(entry.getKey().toString(), entry.getValue().toString());
      }
      return result.build();
    }
  }

  /** Prefixes a string with a tier name. */
  @VisibleForTesting
  @Singleton
  @SoyFunctionSignature(
      name = "tierPrefixed",
      value = {
        @Signature(
            parameterTypes = {"string", "cloud.deploymentmanager.autogen.VmTierSpec|null"},
            returnType = "string"),
        @Signature(
            parameterTypes = {
              "string",
              "cloud.deploymentmanager.autogen.VmTierSpec|null",
              "string"
            },
            returnType = "string")
      })
  static final class TierPrefixed extends TypedSoyFunction implements SoyJavaFunction {
    @Inject SoyDirectives.TierPrefixed directive;

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      return directive.applyForJava(args.get(0), args.subList(1, args.size()));
    }
  }

  @Singleton
  @SoyFunctionSignature(
      name = "tierTemplateName",
      value = {
        @Signature(
            parameterTypes = {"cloud.deploymentmanager.autogen.VmTierSpec"},
            returnType = "string")
      })
  static final class TierTemplateName extends TypedSoyFunction implements SoyJavaFunction {
    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      VmTierSpec spec = (VmTierSpec) ((SoyProtoValue) args.get(0)).getProto();
      return StringData.forValue(apply(spec));
    }

    public static String apply(VmTierSpec spec) {
      return String.format("%s_tier.jinja", spec.getName());
    }
  }

  @Singleton
  @SoyFunctionSignature(
      name = "booleanExpressionDisplayCondition",
      value = {
        @Signature(
            parameterTypes = {"cloud.deploymentmanager.autogen.BooleanExpression"},
            returnType = "string"),
        @Signature(
            parameterTypes = {
              "cloud.deploymentmanager.autogen.BooleanExpression",
              "list<cloud.deploymentmanager.autogen.VmTierSpec>|null"
            },
            returnType = "string")
      })
  static final class BooleanExpressionDisplayCondition extends TypedSoyFunction
      implements SoyJavaFunction {
    @Inject
    protected SoyFunctions.TierPrefixed tierPrefixedFunction;

    @Inject
    protected SoyFunctions.FindVmTier findVmTierFunction;

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      BooleanExpression spec = (BooleanExpression) ((SoyProtoValue) args.get(0)).getProto();
      SoyValue tiersList = NullData.INSTANCE;
      if (args.size() > 1) {
        tiersList = args.get(1);
      }
      String expression = apply(spec, tiersList, tierPrefixedFunction, findVmTierFunction);
      return StringData.forValue(expression);
    }

    public static String apply(BooleanExpression spec, SoyValue tiersList,
        SoyFunctions.TierPrefixed tierPrefixedFunction,
        SoyFunctions.FindVmTier findVmTierFunction) {
      switch (spec.getExpressionCase()) {
        case BOOLEAN_DEPLOY_INPUT_FIELD:
          BooleanDeployInputField field = spec.getBooleanDeployInputField();
          String fieldName = DeployInputFieldName.formatFieldName(field.getName());
          String propertyExpression = String.format("properties().%s", fieldName);
          if (field.getNegated()) {
            propertyExpression = "!" + propertyExpression;
          }
          return propertyExpression;
        case HAS_EXTERNAL_IP:
          ExternalIpAvailability externalIp = spec.getHasExternalIp();
          String noneType = Type.NONE.name();
          SoyValue tierName = StringData.forValue(externalIp.getTier());
          SoyValue tierSpec = NullData.INSTANCE;
          if (!Strings.isNullOrEmpty(externalIp.getTier())) {
            tierSpec = findVmTierFunction.computeForJava(ImmutableList.of(tierName, tiersList));
          }
          String externalIpProperty = tierPrefixedFunction
              .computeForJava(ImmutableList.of(StringData.forValue("externalIP"), tierSpec))
              .stringValue();
          if (externalIp.getNegated()) {
            return String.format("properties().%s == \"%s\"", externalIpProperty, noneType);
          } else {
            return String.format("properties().%s != \"%s\"", externalIpProperty, noneType);
          }
        default:
          throw new IllegalArgumentException(
              "BooleanExpression must have a valid expression choice");
      }
    }
  }

  @Singleton
  @SoyFunctionSignature(
      name = "booleanExpressionJinjaExpression",
      value = {
        @Signature(
            parameterTypes = {"cloud.deploymentmanager.autogen.BooleanExpression"},
            returnType = "string")
      })
  static final class BooleanExpressionJinjaExpression extends TypedSoyFunction
      implements SoyJavaFunction {
    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      BooleanExpression spec = (BooleanExpression) ((SoyProtoValue) args.get(0)).getProto();
      return StringData.forValue(apply(spec));
    }

    public static String apply(BooleanExpression spec) {
      switch (spec.getExpressionCase()) {
        case BOOLEAN_DEPLOY_INPUT_FIELD:
          BooleanDeployInputField field = spec.getBooleanDeployInputField();
          String fieldName = DeployInputFieldName.formatFieldName(field.getName());
          String propertyExpression = String.format("properties[\"%s\"]", fieldName);
          if (field.getNegated()) {
            propertyExpression = "!" + propertyExpression;
          }
          return propertyExpression;
        default:
          throw new IllegalArgumentException(
              "BooleanExpression must have a valid expression choice");
      }
    }
  }

  /**
   * Examines a (MultiVm|SingleVm)DeploymentPackageSpec and tells whether it contains an {@code
   * AcceleratorSpec}
   */
  @Singleton
  @SoyFunctionSignature(
      name = "solutionHasGpus",
      value = {
        @Signature(
            parameterTypes = {
              "cloud.deploymentmanager.autogen.MultiVmDeploymentPackageSpec|cloud.deploymentmanager.autogen.SingleVmDeploymentPackageSpec"
            },
            returnType = "bool")
      })
  static final class SolutionHasGpus extends TypedSoyFunction implements SoyJavaFunction {
    @Inject SoyValueConverter converter;

    /**
     * Argument can be either a {@code SingleVmDeploymentPackageSpec} or a {@code
     * MultiVmDeploymentPackageSpec} instance
     */
    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      Message value = ((SoyProtoValue) args.get(0)).getProto();
      if (value instanceof SingleVmDeploymentPackageSpec) {
        return converter
            .convert(!((SingleVmDeploymentPackageSpec) value).getAcceleratorsList().isEmpty())
            .resolve();
      }

      List<VmTierSpec> tierList;
      try {
        tierList = extractTierList(args.get(0));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Unexpected 2nd arg type for dependentTiers", e);
      }

      for (VmTierSpec tier : tierList) {
        if (!tier.getAcceleratorsList().isEmpty()) {
          return converter.convert(true).resolve();
        }
      }

      return converter.convert(false).resolve();
    }
  }

  /**
   * Extracts as a flattened list of fields from {@code DeployInputSpec}.
   * This function can take an additional argument of type {@code VmTierSpec}, in which case it
   * will return only fields that are used by the specified tier.
   */
  @Singleton
  @SoyFunctionSignature(
      name = "listDeployInputFields",
      value = {
        @Signature(
            parameterTypes = {"cloud.deploymentmanager.autogen.DeployInputSpec"},
            returnType = "list<cloud.deploymentmanager.autogen.DeployInputField>"),
        @Signature(
            parameterTypes = {
              "cloud.deploymentmanager.autogen.DeployInputSpec",
              "cloud.deploymentmanager.autogen.VmTierSpec"
            },
            returnType = "list<cloud.deploymentmanager.autogen.DeployInputField>")
      })
  static final class ListDeployInputFields extends TypedSoyFunction implements SoyJavaFunction {
    @Inject SoyValueConverter converter;

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      DeployInputSpec inputSpec = (DeployInputSpec) ((SoyProtoValue) args.get(0)).getProto();
      if (args.size() == 1) {
        return converter.convert(apply(inputSpec)).resolve();
      } else {
        VmTierSpec tierSpec = (VmTierSpec) ((SoyProtoValue) args.get(1)).getProto();
        return converter.convert(apply(inputSpec, tierSpec)).resolve();
      }
    }

    @VisibleForTesting
    static List<DeployInputField> apply(DeployInputSpec inputSpec) {
      List<DeployInputField> fields = new ArrayList<>();
      for (DeployInputSection section : inputSpec.getSectionsList()) {
        fields.addAll(section.getFieldsList());
      }
      return fields;
    }

    @VisibleForTesting
    static List<DeployInputField> apply(DeployInputSpec inputSpec, VmTierSpec tierSpec) {
      Set<String> referencedFields = buildReferencedFieldsSet(tierSpec);
      List<DeployInputField> fields = new ArrayList<>();
      for (DeployInputSection section : inputSpec.getSectionsList()) {
        for (DeployInputField field : section.getFieldsList()) {
          if (referencedFields.contains(field.getName())) {
            fields.add(field);
          }
        }
      }
      return fields;
    }

    private static Set<String> buildReferencedFieldsSet(VmTierSpec tierSpec) {
      List<GceMetadataItem> metadataItems = tierSpec.getGceMetadataItemsList();
      Set<String> fields = new HashSet<>();
      for (GceMetadataItem metadataItem : metadataItems) {
        if (metadataItem.getValueSpecCase() == VALUE_FROM_DEPLOY_INPUT_FIELD) {
          fields.add(metadataItem.getValueFromDeployInputField());
        }
      }
      if (tierSpec.hasLocalSsds()
          && tierSpec.getLocalSsds().getCountSpecCase() == COUNT_FROM_DEPLOY_INPUT_FIELD) {
        fields.add(tierSpec.getLocalSsds().getCountFromDeployInputField());
      }
      for (DiskSpec disk : tierSpec.getAdditionalDisksList()) {
        if (disk.getDeviceNameSuffix().getDeviceNameCase()
            == DeviceNameCase.NAME_FROM_DEPLOY_INPUT_FIELD) {
          fields.add(disk.getDeviceNameSuffix().getNameFromDeployInputField());
        }
      }
      return fields;
    }
  }

  private static List<VmTierSpec> extractTierList(SoyValue tiersArg) {
    if (tiersArg instanceof SoyList) {
      List<? extends SoyValue> list = ((SoyList) tiersArg).asResolvedJavaList();
      return Lists.transform(list, new Function<SoyValue, VmTierSpec>() {
        @Override
        public VmTierSpec apply(SoyValue soyValue) {
          return (VmTierSpec) ((SoyProtoValue) soyValue).getProto();
        }
      });
    } else if (tiersArg instanceof SoyProtoValue) {
      return ((MultiVmDeploymentPackageSpec) ((SoyProtoValue) tiersArg).getProto()).getTiersList();
    } else {
      throw new IllegalArgumentException("Unable to extract tier list from argument");
    }
  }

  abstract static class AbstractDiskPropertyName extends TypedSoyFunction
      implements SoyJavaFunction {
    @Inject
    protected SoyFunctions.TierPrefixed tierPrefixedFunction;

    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      // Index is starting with 0 for the first element, we want to start with 1
      int diskPosition = args.get(0).integerValue() + 1;
      SoyValue baseName = StringData.forValue(getPropertyBaseName(diskPosition));
      SoyValue tierSpec = NullData.INSTANCE;
      if (args.size() > 1) {
        tierSpec = args.get(1);
      }
      return tierPrefixedFunction.computeForJava(Arrays.asList(baseName, tierSpec));
    }

    protected abstract String getPropertyBaseName(int diskPosition);
  }

  @Singleton
  @SoyFunctionSignature(
      name = "diskTypePropertyName",
      value = {
        @Signature(
            parameterTypes = {"int"},
            returnType = "string"),
        @Signature(
            parameterTypes = {"int", "cloud.deploymentmanager.autogen.VmTierSpec|null"},
            returnType = "string")
      })
  static final class AdditionalDiskTypePropertyName extends AbstractDiskPropertyName {
    @Override
    protected String getPropertyBaseName(int diskPosition) {
      return String.format("%s_type", additionalDiskPropertyName(diskPosition));
    }
  }

  @Singleton
  @SoyFunctionSignature(
      name = "diskSizePropertyName",
      value = {
        @Signature(
            parameterTypes = {"int"},
            returnType = "string"),
        @Signature(
            parameterTypes = {"int", "cloud.deploymentmanager.autogen.VmTierSpec|null"},
            returnType = "string")
      })
  static final class AdditionalDiskSizePropertyName extends AbstractDiskPropertyName {
    @Override
    protected String getPropertyBaseName(int diskPosition) {
      return String.format("%s_sizeGb", additionalDiskPropertyName(diskPosition));
    }
  }

  @Singleton
  @SoyFunctionSignature(
      name = "externalIpTypeName",
      value = {
        @Signature(
            parameterTypes = {"cloud.deploymentmanager.autogen.ExternalIpSpec.Type"},
            returnType = "string")
      })
  static final class ExternalIpTypeName extends TypedSoyFunction implements SoyJavaFunction {
    @Override
    public SoyValue computeForJava(List<SoyValue> args) {
      ExternalIpSpec.Type type = ExternalIpSpec.Type.forNumber(args.get(0).integerValue());
      return StringData.forValue(type.name());
    }
  }

  private static String additionalDiskPropertyName(int diskPosition) {
    return String.format("disk%d", diskPosition);
  }

  private SoyFunctions() {}
}
