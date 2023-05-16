load("@bazel_rules_android//android:rules.bzl", "android_library")
load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "chronicle",
    srcs = ["Chronicle.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    visibility = ["//visibility:public"],
    deps = [
        ":api",
        "//java/com/google/android/libraries/pcc/chronicle/api/error:chronicle_error",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_guava",
    ],
)

android_library(
    name = "api",
    srcs = [
        "ChronicleAnalyticsClient.kt",
        "ChronicleDeletionListener.kt",
        "ConnectionProvider.kt",
        "ConnectionRequest.kt",
        "DataType.kt",
        "DeletionTrigger.kt",
        "ManagedDataType.kt",
        "ManagementStrategy.kt",
        "ProcessorNode.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    visibility = ["//visibility:public"],
    exports = [
        ":connection",
        ":data_type_descriptor",
    ],
    deps = [
        ":connection",
        ":data_type_descriptor",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "@maven//:com_google_dagger_dagger",
    ],
)

kt_jvm_library(
    name = "api_jvm",
    srcs = [
        "ConnectionProvider.kt",
        "ConnectionRequest.kt",
        "DataType.kt",
        "DeletionTrigger.kt",
        "ManagedDataType.kt",
        "ManagementStrategy.kt",
        "ProcessorNode.kt",
    ],
    visibility = ["//visibility:public"],
    exports = [
        ":connection_jvm",
        ":data_type_descriptor_jvm",
    ],
    deps = [
        ":connection_jvm",
        ":data_type_descriptor_jvm",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
    ],
)

android_library(
    name = "connection",
    srcs = [
        "Connection.kt",
        "ConnectionName.kt",
        "ConnectionUtils.kt",
        "Name.kt",
        "ReadConnection.kt",
        "WriteConnection.kt",
    ],
)

kt_jvm_library(
    name = "connection_jvm",
    srcs = [
        "Connection.kt",
        "ConnectionName.kt",
        "ConnectionUtils.kt",
        "Name.kt",
        "ReadConnection.kt",
        "WriteConnection.kt",
    ],
)

android_library(
    name = "data_type_descriptor",
    srcs = [
        "DataTypeDescriptor.kt",
        "DataTypeDescriptorSet.kt",
    ],
    deps = [
        ":chronicle_dsl_annotation",
    ],
)

kt_jvm_library(
    name = "data_type_descriptor_jvm",
    srcs = [
        "DataTypeDescriptor.kt",
        "DataTypeDescriptorSet.kt",
    ],
    deps = [
        ":chronicle_dsl_annotation_jvm",
    ],
)

android_library(
    name = "chronicle_dsl_annotation",
    srcs = ["ChronicleDsl.kt"],
)

kt_jvm_library(
    name = "chronicle_dsl_annotation_jvm",
    srcs = ["ChronicleDsl.kt"],
)

android_library(
    name = "sandbox_processor_node",
    srcs = ["SandboxProcessorNode.kt"],
    visibility = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis:__subpackages__",
        "//java/com/google/android/libraries/pcc/chronicle/remote/impl:__subpackages__",
        "//javatests/com/google/android/libraries/pcc/chronicle:__subpackages__",
        "//javatests/com/google/android/libraries/pcc/chronicle/remote/impl:__subpackages__",
    ],
    deps = [
        ":api",
    ],
)

android_library(
    name = "default_data_removal_request_listener",
    srcs = ["DefaultDataRemovalRequestListener.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        ":api",
        "//java/com/google/android/libraries/pcc/chronicle/api/flags",
        "//java/com/google/android/libraries/pcc/chronicle/util:logcat",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_guava",
    ],
)
