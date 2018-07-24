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

