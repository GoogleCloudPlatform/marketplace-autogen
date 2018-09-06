# Deployment Manager Autogen [![Build Status](https://travis-ci.com/GoogleCloudPlatform/deploymentmanager-autogen.svg?branch=master)](https://travis-ci.com/GoogleCloudPlatform/deploymentmanager-autogen)

*Deployment Manager Autogen* is a Java-based tool for generating fully functional [Deployment Manager](https://cloud.google.com/deployment-manager/docs/) templates from simplified YAML, JSON or Prototext configuration files.

It currently supports single and multi VM configurations.

## Prerequisites

* **bazel**: Install [here](https://docs.bazel.build/versions/master/install.html).
* **Java**: Check [bazel docs](https://docs.bazel.build/versions/master/tutorial/java.html) on how to install and setup a Java environment.

## Build

To build all the artifacts, run the following `bazel` command:

```shell
bazel build java/com/google/cloud/deploymentmanager/autogen/...:all
```

If you want, you can also build AutogenCli_deploy.jar target, to run the jar as a standalone application
```shell
bazel build java/com/google/cloud/deploymentmanager/autogen/cli:AutogenCli_deploy.jar
```

## Run tests

```shell
bazel test javatests/com/google/cloud/deploymentmanager/autogen:all
```

## Running Autogen through CLI

With AutogenCli, you will be able to generate your solution package through command line by providing
your configuration in a JSON, YAML or Prototext file.

Your configuration file should follow the schema described in the DeploymentPackageInput proto, if you want
to generate deployment package for a single solution, or BatchInput proto for multiple solutions. Both specs
can be found in [autogen.proto](java/com/google/cloud/deploymentmanager/autogen/autogen.proto).

### AutogenCli parameters

* `--single_input`

  Option to indicate that Autogen will be run on a configuration that contains a DeploymentPackageInput spec. If an argument is specified, AutogenCli will interpret it as a path to a file to read the spec from. If empty, AutogenCli will read from stdin. (either --single_input or --batch_input needs to be provided).

* `--batch_input`

  Option to indicate that Autogen will be run on a configuration that contains a BatchInput spec. If an argument is specified, AutogenCli will interpret it as a path to a file to read the spec from. If empty, AutogenCli will read from stdin. (either --single_input or --batch_input needs to be provided).

* `--input_type` (optional, defaults to `PROTOTEXT`)

  Indicates the format of the spec that AutogenCli will read. Available options are: `YAML`, `JSON`, `PROTOTEXT` or `WIRE` (binary prototext).

* `--output_type` (optional, defaults to `PROTOTEXT`)

  Indicates the format of the spec that AutogenCli will write. Available options are: `YAML`, `JSON`, `PROTOTEXT`, `WIRE` or `PACKAGE`.

* `--output` (optional)

  If --output_type is `PACKAGE`, the argument of this option will be interpreted as a destination folder for the deployment files to be written to. If left empty or not provided, it will use the current directory that the binary is being run.\
  For all other --output_type options, the argument of this option will be interpreted as a path to file to write the output to. If empty or not provided, it will defaults to stdout.

* `--exclude_shared_support_files` (optional)

  If provided, Autogen will NOT include the shared support files used for deployment in this solution (look [here](java/com/google/cloud/deploymentmanager/autogen/templates/sharedsupport/common) for those files). By default Autogen always include those files (recommended).

You can also see those options by providing `--help` as the argument for AutogenCli

```shell
bazel-bin/java/com/google/cloud/deploymentmanager/autogen/cli/AutogenCli --help
```

### Examples

We have provided a full featured example in the [example-config](example-config/) folder.

Please refer to the comments inside the config example and the proto files ([autogen.proto](java/com/google/cloud/deploymentmanager/autogen/autogen.proto), [deployment_package_autogen_spec.proto](java/com/google/cloud/deploymentmanager/autogen/deployment_package_autogen_spec.proto) and [marketing_info.proto](java/com/google/cloud/deploymentmanager/autogen/marketing_info.proto)) for more information about the fields and which of those are required and optional.

You can then change the fields to reflect the configuration of your solution and run Autogen:

```shell
bazel-bin/java/com/google/cloud/deploymentmanager/autogen/cli/AutogenCli \
  --input_type YAML \
  --single_input example-config/solution.yaml \
  --output_type PACKAGE \
  --output solution_folder
```

Then compress the solution_folder folder and use partner portal to upload your deployment package: https://console.cloud.google.com/partner

Other sample configurations are also available in [testdata](javatests/com/google/cloud/deploymentmanager/autogen/testdata) folder.

#### Other examples

To run a sample test proto definition transformation with printing the deployment package in JSON
format, run the following command:

```shell
bazel-bin/java/com/google/cloud/deploymentmanager/autogen/cli/AutogenCli \
  --input_type PROTOTEXT \
  --single_input javatests/com/google/cloud/deploymentmanager/autogen/testdata/singlevm/full_features/input.prototext \
  --output_type JSON
```
