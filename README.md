# Deployment Manager Autogen

*Deployment Manager Autogen* is a Java-based tool for generating fully functional [Deployment Manager](https://cloud.google.com/deployment-manager/docs/) templates from simplified YAML configuration files.

It currently supports single and multi VM configurations.

## Build

To build all the artifacts, run the following `bazel` command:

```shell
bazel build java/com/google/cloud/deploymentmanager/autogen:all
```

## Run tests

```shell
bazel test javatests/com/google/cloud/deploymentmanager/autogen:all
```

## Examples

Sample configurations are available in tests data
[directory](https://github.com/GoogleCloudPlatform/deploymentmanager-autogen/tree/master/javatests/com/google/cloud/deploymentmanager/autogen/testdata).

### Run a sample transformation

Build the BatchAutogenApp target - we will use its deployment package:

```shell
bazel build java/com/google/cloud/deploymentmanager/autogen:BatchAutogenApp_deploy.jar
```

To run a sample test proto definition transformation with printing the deployment package in JSON
format, run the following command:

```shell
java -jar bazel-bin/java/com/google/cloud/deploymentmanager/autogen/BatchAutogenApp_deploy.jar \
  --mode SINGLE \
  --input_type PROTOTEXT \
  --input javatests/com/google/cloud/deploymentmanager/autogen/testdata/singlevm/full_features/input.prototext \
  --output_type JSON \
  --include_shared_support_files true
```

To get more information about all the supported options of BatchAutogenApp, use:

```shell
java -jar bazel-bin/java/com/google/cloud/deploymentmanager/autogen/BatchAutogenApp_deploy.jar --help
```
