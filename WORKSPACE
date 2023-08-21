load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "4.2"
RULES_JVM_EXTERNAL_SHA = "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

# proto_library, cc_proto_library, and java_proto_library rules implicitly
# depend on @com_google_protobuf for protoc and proto runtimes.
# This statement defines the @com_google_protobuf repo.
http_archive(
    name = "com_google_protobuf",
    sha256 = "cf6b0458b280081a07b236278abf48321bac0a268bc14ef3510da624ea243993",
    strip_prefix = "protobuf-21.8",
    urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v21.8/protobuf-all-21.8.zip"]
)

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

http_archive(
    name = "io_bazel_rules_docker",
    sha256 = "85ffff62a4c22a74dbd98d05da6cf40f497344b3dbf1e1ab0a37ab2a1a6ca014",
    strip_prefix = "rules_docker-0.23.0",
    urls = ["https://github.com/bazelbuild/rules_docker/releases/download/v0.23.0/rules_docker-v0.23.0.tar.gz"],
)

load(
    "@io_bazel_rules_docker//repositories:repositories.bzl",
    container_repositories = "repositories",
)

container_repositories()

load("@io_bazel_rules_docker//repositories:deps.bzl", container_deps = "deps")

container_deps()

load(
    "@io_bazel_rules_docker//java:image.bzl",
    java_image_repos = "repositories",
)

java_image_repos()

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    name = "maven",
    artifacts = [
        "aopalliance:aopalliance:1.0",
        "com.google.auto.value:auto-value:1.5.4",
        "com.google.code.findbugs:jsr305:3.0.2",
        "com.google.code.gson:gson:2.8.5",
        "com.google.common.html.types:types:1.0.8",
        "com.google.errorprone:error_prone_annotations:2.21.1",
        "com.google.guava:guava:31.1-jre",
        "com.google.inject:guice:5.1.0",
        "com.google.protobuf:protobuf-java-util:3.12.2",
        "com.google.protobuf:protobuf-java:3.12.2",
        "com.google.template:soy:2023-07-19",
        "com.google.truth.extensions:truth-liteproto-extension:1.0",
        "com.google.truth.extensions:truth-proto-extension:1.0",
        "com.google.truth:truth:1.0",
        "com.ibm/icu:icu4j:60.2",
        "commons-cli:commons-cli:1.3.1",
        "javax.inject:javax.inject:1",
        "junit:junit:4.12",
        "org.apache.commons:commons-lang3:3.6",
        "org.hamcrest:hamcrest-core:1.3",
        "org.hamcrest:hamcrest-library:1.3",
        "org.ow2.asm:asm:9.4",
        "org.yaml:snakeyaml:1.32",
    ],
    fetch_sources = True,
    # See https://github.com/bazelbuild/rules_jvm_external/#repository-aliases
    # This can be removed if none of your external dependencies uses `maven_jar`.
    generate_compat_repositories = True,
    repositories = [
        "https://jcenter.bintray.com",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
    version_conflict_policy = "pinned",
)

load("@maven//:compat.bzl", "compat_repositories")

compat_repositories()
