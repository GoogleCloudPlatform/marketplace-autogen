load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
# proto_library, cc_proto_library, and java_proto_library rules implicitly
# depend on @com_google_protobuf for protoc and proto runtimes.
# This statement defines the @com_google_protobuf repo.
http_archive(
    name = "com_google_protobuf",
    sha256 = "9510dd2afc29e7245e9e884336f848c8a6600a14ae726adb6befdb4f786f0be2",
    strip_prefix = "protobuf-3.6.1.3",
    urls = ["https://github.com/google/protobuf/archive/v3.6.1.3.zip"],
)

maven_jar(
    name = "javax_inject",
    artifact = "javax.inject:javax.inject:1",
)

maven_jar(
    name = "aopalliance_aopalliance",
    artifact = "aopalliance:aopalliance:1.0",
)

maven_jar(
    name = "commons_cli_commons_cli",
    artifact = "commons-cli:commons-cli:1.3.1",
)

maven_jar(
    name = "com_google_auto_value_auto_value",
    artifact = "com.google.auto.value:auto-value:1.5.4",
)

maven_jar(
    name = "com_google_code_findbugs_jsr305",
    artifact = "com.google.code.findbugs:jsr305:3.0.2",
)

maven_jar(
    name = "com_google_code_gson",
    artifact = "com.google.code.gson:gson:2.8.5",
)

maven_jar(
    name = "com_google_common_html_types_types",
    artifact = "com.google.common.html.types:types:1.0.8",
)

maven_jar(
    name = "com_google_guava_guava",
    artifact = "com.google.guava:guava:24.0-jre",
)

maven_jar(
    name = "com_google_inject_guice",
    artifact = "com.google.inject:guice:4.2.0",
)

maven_jar(
    name = "com_google_protobuf_protobuf_java",
    artifact = "com.google.protobuf:protobuf-java:3.5.1",
)

maven_jar(
    name = "com_google_protobuf_protobuf_java_util",
    artifact = "com.google.protobuf:protobuf-java-util:3.5.1",
)

maven_jar(
    name = "com_google_template_soy",
    artifact = "com.google.template:soy:2018-03-14",
)

maven_jar(
    name = "com_google_truth_truth",
    artifact = "com.google.truth:truth:0.39",
)

maven_jar(
    name = "com_ibm_icu_icu4j",
    artifact = "com.ibm/icu:icu4j:60.2",
)

maven_jar(
    name = "junit_junit",
    artifact = "junit:junit:4.12",
)

maven_jar(
    name = "org_apache_commons_commons_lang3",
    artifact = "org.apache.commons:commons-lang3:3.6",
)

maven_jar(
    name = "org_hamcrest_hamcrest_core",
    artifact = "org.hamcrest:hamcrest-core:1.3",
)

maven_jar(
    name = "org_hamcrest_hamcrest_library",
    artifact = "org.hamcrest:hamcrest-library:1.3",
)

maven_jar(
    name = "org_ow2_asm_asm_all",
    artifact = "org.ow2.asm:asm-all:6.0_BETA",
)

maven_jar(
    name = "org_yaml_snakeyaml",
    artifact = "org.yaml:snakeyaml:1.19",
)

http_archive(
    name = "io_bazel_rules_docker",
    sha256 = "29d109605e0d6f9c892584f07275b8c9260803bf0c6fcb7de2623b2bedc910bd",
    strip_prefix = "rules_docker-0.5.1",
    urls = ["https://github.com/bazelbuild/rules_docker/archive/v0.5.1.tar.gz"],
)

load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_pull",
    container_repositories = "repositories",
)
load(
    "@io_bazel_rules_docker//java:image.bzl",
    _java_image_repos = "repositories",
)

_java_image_repos()
