load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_KOTLIN_VERSION = "1.6.0"

RULES_KOTLIN_SHA = "a57591404423a52bd6b18ebba7979e8cd2243534736c5c94d35c89718ea38f94"

RULES_JVM_EXTERNAL_TAG = "4.2"

RULES_JVM_EXTERNAL_SHA = "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca"

ROBOLECTRIC_VERSION = "4.9"

ROBOLECTRIC_SHA = "7655c49633ec85a18b5a94b1ec36e250671808e45494194959b1d1d7f3e73a23"

# Pull down bazel_skylib to be able to define bzl_library targets.
http_archive(
    name = "bazel_skylib",
    sha256 = "74d544d96f4a5bb630d465ca8bbcfe231e3594e5aae57e1edbf17a6eb3ca2506",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/1.3.0/bazel-skylib-1.3.0.tar.gz",
        "https://github.com/bazelbuild/bazel-skylib/releases/download/1.3.0/bazel-skylib-1.3.0.tar.gz",
    ],
)

load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

bazel_skylib_workspace()

# Pull down rules_proto so that we can use proto_library and friends.
http_archive(
    name = "rules_proto",
    sha256 = "e017528fd1c91c5a33f15493e3a398181a9e821a804eb7ff5acdd1d2d6c2b18d",
    strip_prefix = "rules_proto-4.0.0-3.20.0",
    urls = [
        "https://github.com/bazelbuild/rules_proto/archive/refs/tags/4.0.0-3.20.0.tar.gz",
    ],
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

# Brings in the java_library and related rules, allows us to specify maven
# dependencies. Required by kotlin and android archives.
http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")

#############################
# Load Bazel-Common repository
#############################

http_archive(
    name = "google_bazel_common",
    sha256 = "60a9aebe25f476646f61c041d1679a9b21076deffbd51526838c7f24d6468ac0",
    strip_prefix = "bazel-common-227a23a508a2fab0fa67ffe2d9332ae536a40edc",
    urls = ["https://github.com/google/bazel-common/archive/227a23a508a2fab0fa67ffe2d9332ae536a40edc.zip"],
)

# Dagger
http_archive(
    name = "com_google_dagger",
    sha256 = "cbff42063bfce78a08871d5a329476eb38c96af9cf20d21f8b412fee76296181",
    strip_prefix = "dagger-dagger-2.44.2",
    urls = ["https://github.com/google/dagger/archive/dagger-2.44.2.zip"],
)

load("@com_google_dagger//:workspace_defs.bzl", "HILT_ANDROID_ARTIFACTS", "HILT_ANDROID_REPOSITORIES")

maven_install(
    artifacts = HILT_ANDROID_ARTIFACTS + [
        "androidx.annotation:annotation:1.4.0",
        "androidx.multidex:multidex:2.0.1",
        "androidx.appcompat:appcompat:1.3.1",
        "androidx.room:room-compiler:2.4.3",
        "androidx.room:room-ktx:2.4.3",
        "androidx.room:room-migration:2.4.3",
        "androidx.room:room-runtime:2.4.3",
        "androidx.sqlite:sqlite:2.2.0",
        "androidx.test:core:1.5.0",
        "androidx.test:monitor:1.6.0",
        "androidx.test.ext:junit:1.1.3",
        "com.github.ajalt.clikt:clikt-jvm:3.5.0",
        "com.google.auto.service:auto-service:1.0.1",
        "com.google.auto.service:auto-service-annotations:1.0.1",
        "com.google.auto.value:auto-value:1.9",
        "com.google.auto.value:auto-value-annotations:1.9",
        "com.google.code.findbugs:annotations:3.0.1u2",
        "com.google.code.gson:gson:2.9.1",
        "com.google.errorprone:error_prone_annotations:2.16",
        "com.google.flogger:flogger:0.7.4",
        "com.google.guava:guava:31.1-android",
        "com.google.protobuf:protobuf-java:3.21.7",
        "com.google.protobuf:protobuf-javalite:3.21.7",
        "com.google.testing.compile:compile-testing:0.19",
        "com.google.truth:truth:1.0",
        "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0",
        "com.squareup:javapoet:1.13.0",
        "com.squareup:kotlinpoet:1.12.0",
        "javax.inject:javax.inject:1",
        "junit:junit:4.11",
        "org.jetbrains.kotlin:kotlin-test:1.7.10",
        "org.jetbrains.kotlinx:atomicfu:0.18.4",
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4",
        "org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.6.4",
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4",
        "org.robolectric:robolectric:%s" % ROBOLECTRIC_VERSION,
        "org.robolectric:annotations:%s" % ROBOLECTRIC_VERSION,
        "org.robolectric:shadowapi:%s" % ROBOLECTRIC_VERSION,
        "org.robolectric:shadows-framework:%s" % ROBOLECTRIC_VERSION,
        "org.mockito:mockito-core:2.23.0",
        "org.checkerframework:checker-qual:3.27.0",
    ],
    fetch_sources = True,
    repositories = HILT_ANDROID_REPOSITORIES + [
        "https://jcenter.bintray.com/",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)

android_sdk_repository(
    name = "androidsdk",
    api_level = 31,
)

# Allows us to use android_library and android_binary rules.
# Required by the kotlin rules.
http_archive(
    name = "bazel_rules_android",
    sha256 = "cd06d15dd8bb59926e4d65f9003bfc20f9da4b2519985c27e190cddc8b7a7806",
    strip_prefix = "rules_android-0.1.1",
    urls = ["https://github.com/bazelbuild/rules_android/archive/v0.1.1.zip"],
)

# Allows us to use kt_android_library, kt_jvm_library, and friends.
http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = RULES_KOTLIN_SHA,
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/v%s/rules_kotlin_release.tgz" % RULES_KOTLIN_VERSION],
)

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")

kotlin_repositories()  # if you want the default. Otherwise see custom kotlinc distribution below

load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_register_toolchains")

kt_register_toolchains()  # to use the default toolchain, otherwise see toolchains below

http_archive(
    name = "robolectric",
    sha256 = ROBOLECTRIC_SHA,
    strip_prefix = "robolectric-bazel-%s" % ROBOLECTRIC_VERSION,
    urls = ["https://github.com/robolectric/robolectric-bazel/archive/%s.tar.gz" % ROBOLECTRIC_VERSION],
)

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")

robolectric_repositories()

# Protobuf

# Note: using the gRPC protobuf rules, since they seem to be the most
# comprehensive and best documented:
# https://github.com/rules-proto-grpc/rules_proto_grpc

http_archive(
    name = "rules_proto_grpc",
    sha256 = "5f0f2fc0199810c65a2de148a52ba0aff14d631d4e8202f41aff6a9d590a471b",
    strip_prefix = "rules_proto_grpc-1.0.2",
    urls = ["https://github.com/rules-proto-grpc/rules_proto_grpc/archive/1.0.2.tar.gz"],
)

load(
    "@rules_proto_grpc//android:repositories.bzl",
    rules_proto_grpc_android_repos = "android_repos",
)

rules_proto_grpc_android_repos()

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories(
    omit_bazel_skylib = True,
    omit_com_google_protobuf = True,
    omit_com_google_protobuf_javalite = True,
    omit_net_zlib = True,
)

load(
    "@rules_proto_grpc//:repositories.bzl",
    "rules_proto_grpc_repos",
    "rules_proto_grpc_toolchains",
)

rules_proto_grpc_toolchains()

rules_proto_grpc_repos()

########################
# Java Runtimes
########################
load("@bazel_tools//tools/jdk:local_java_repository.bzl", "local_java_repository")

# To use this, add --java_runtime_version=local-openjdk11 to any build command,
# or to your own .bazelrc file.
# Note: you must have the OpenJDK installed:
# sudo apt-get install default-jdk
local_java_repository(
    name = "local-openjdk11",
    java_home = "/usr/lib/jvm/java-11-openjdk-amd64/",
)
