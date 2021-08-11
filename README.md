# Deployment Manager Autogen [![Build Status](https://travis-ci.com/GoogleCloudPlatform/deploymentmanager-autogen.svg?branch=master)](https://travis-ci.com/GoogleCloudPlatform/deploymentmanager-autogen)

*Deployment Manager Autogen* is a Java-based tool for generating fully functional [Deployment Manager](https://cloud.google.com/deployment-manager/docs/) templates from simplified YAML, JSON, or Prototext configuration files.

It currently supports single- and multi-VM configurations.

## Running Autogen through CLI

With AutogenCli, you can generate a solution package through the command line, by providing
your configuration in a JSON, YAML, or Prototext file.

If you want to generate a deployment package for a single solution, your configuration file should follow
the schema described in the DeploymentPackageInput proto. If you want to generate deployment packages for
multiple solutions, your configuration file should follow the schema described in the BatchInput proto.
Both specs can be found in
[autogen.proto](java/com/google/cloud/deploymentmanager/autogen/autogen.proto).

### AutogenCli parameters

To review these options at any time, provide `--help` as the argument for AutogenCli.

* `--single_input`

  This parameter is used to indicate that Autogen will be run on a configuration that contains a DeploymentPackageInput spec. If an argument is specified, AutogenCli will interpret it as the path to the file to read the spec from; if it is empty, AutogenCli will read from stdin. Either `--single_input` or `--batch_input` must be provided as a parameter.

* `--batch_input`

  This parameter is used to indicate that Autogen will be run on a configuration that contains a BatchInput spec. If an argument is specified, AutogenCli will interpret it as the path to the file to read the spec from; if it is empty, AutogenCli will read from stdin. Either `--single_input` or `--batch_input` must be provided as a parameter.

* `--input_type` (optional, defaults to `PROTOTEXT`)

  This indicates the format of the spec that AutogenCli will read in. The available options for this parameter are: `YAML`, `JSON`, `PROTOTEXT`, and `WIRE` (binary prototext).

* `--output_type` (optional, defaults to `PROTOTEXT`)

  This indicates the format of the spec that AutogenCli will write out. The available options for this parameter are: `YAML`, `JSON`, `PROTOTEXT`, `WIRE`, and `PACKAGE`.

* `--output` (optional)

  If the `--output_type` is `PACKAGE`, then the argument of this parameter will be interpreted as a destination folder for the deployment files to be written to. If it is left empty or nothing is provided, Autogen will use the current directory iun which the binary is being run.\
  For all other `--output_type` options, the argument of this parameter will be interpreted as a path to where the file should be output to. If it is left empty or nothing is provided, Autogen will default to stdout.

* `--exclude_shared_support_files` (optional)

  If this parameter is provided, Autogen will NOT include the shared support files used for deployment in this solution (look [here](java/com/google/cloud/deploymentmanager/autogen/templates/sharedsupport/common) for those files). By default, Autogen always includes these files, which is the recommended option.

### Example configurations

We have provided a full featured example configuration in the [example-config](example-config/) folder.

Please refer to the comments inside the example configuration and the proto files ([autogen.proto](java/com/google/cloud/deploymentmanager/autogen/autogen.proto), [deployment_package_autogen_spec.proto](java/com/google/cloud/deploymentmanager/autogen/deployment_package_autogen_spec.proto) and [marketing_info.proto](java/com/google/cloud/deploymentmanager/autogen/marketing_info.proto)) for more information about their fields, including which of them are required and which are optional.

To create your deployment package, change the fields of the provided examples to reflect the configuration of your solution, and then run Autogen.

### Example commands

It's easiest to use our released docker images. If you want to build and run the tool from source, see the [Development](#development) section below.

#### Before you begin

* Make sure that [docker](https://www.docker.com/) has been installed.

* Set up the following alias, to keep your commands clean:<sup>1</sup>

```shell
alias autogen='docker run \
  --rm \
  --workdir /mounted \
  --mount type=bind,source="$(pwd)",target=/mounted \
  --user $(id -u):$(id -g) \
  gcr.io/cloud-marketplace-tools/dm/autogen'

autogen --help
```

#### Generating the package

The command below creates a deployment package according to the spec in `example-config/solution.yaml`, and then outputs all of the resulting files into `solution_folder`:

```shell
mkdir solution_folder

autogen \
  --input_type YAML \
  --single_input example-config/solution.yaml \
  --output_type PACKAGE \
  --output solution_folder
```

After the command has completed, you can then compress the `solution_folder` folder, and
[use Partner Portal to upload it as your deployment package](https://console.cloud.google.com/partner).

**NOTE**: Only files in the current working folder can be seen and manipulated by the tool, so you must make sure to pass in the input and output file parameters using relative paths.

Other sample configurations are also available in the [testdata](javatests/com/google/cloud/deploymentmanager/autogen/testdata) folder.

The command below runs a sample test proto definition transformation, outputting the deployment package in the JSON
format:

```shell
autogen \
  --input_type PROTOTEXT \
  --single_input javatests/com/google/cloud/deploymentmanager/autogen/testdata/singlevm/full_features/input.prototext \
  --output_type JSON
```

<sup>1</sup>A few notes about the alias:
- Since we're running the tool from within a docker container, we need to make the local filesystem available to the container. We do this by `--mount`ing the current working directory. Within the container runtime, the content is available at `/mounted`.
- We additionally set `--workdir` to `/mounted` for the running container, so that file parameters like `--single_input` can use relative paths.
- We add `--user` to instruct docker to run as the current user, instead of `root`.

## Development

### Prerequisites

* **bazel (version 0.28.1 or up)**: Install [here](https://docs.bazel.build/versions/master/install.html).
* **Java**: Check the [bazel docs](https://docs.bazel.build/versions/master/tutorial/java.html) on how to install and setup a Java environment.

### Build

To build all of the artifacts, run the following `bazel` command:

```shell
bazel build java/com/google/cloud/deploymentmanager/autogen/...:all
```

If you want to, you can also build AutogenCli_deploy.jar target, to run the jar as a standalone application:

```shell
bazel build java/com/google/cloud/deploymentmanager/autogen/cli:AutogenCli_deploy.jar
```

### Run the CLI tool

An example:

```shell
bazel-bin/java/com/google/cloud/deploymentmanager/autogen/cli/AutogenCli \
  --input_type YAML \
  --single_input example-config/solution.yaml \
  --output_type PACKAGE \
  --output solution_folder
```

### Run tests

```shell
bazel test javatests/com/google/cloud/deploymentmanager/autogen:all
```

### Troubleshooting

If you see errors when trying to build Autogen using bazel, try to run the following command, and then try again:

```shell
bazel clean --expunge
```
