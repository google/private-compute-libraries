load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library", "kt_jvm_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"])

kt_android_library(
    name = "chronicle",
    srcs = ["Chronicle.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    visibility = ["//java/com/google/android/libraries/pcc/chronicle:api_visibility"],
    deps = [
        ":api",
        "//java/com/google/android/libraries/pcc/chronicle/api/error:chronicle_error",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_guava",
    ],
)

kt_android_library(
    name = "api",
    srcs = [
        "ChronicleAnalyticsClient.kt",
        "ChronicleDeletionListener.kt",
        "ChronicleHost.kt",
        "ConnectionProvider.kt",
        "ConnectionRequest.kt",
        "DataType.kt",
        "DeletionTrigger.kt",
        "ManagedDataType.kt",
        "ManagementStrategy.kt",
        "ProcessorNode.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    visibility = ["//java/com/google/android/libraries/pcc/chronicle:api_visibility"],
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
        "ChronicleHost.kt",
        "ConnectionProvider.kt",
        "ConnectionRequest.kt",
        "DataType.kt",
        "DeletionTrigger.kt",
        "Flavor.kt",
        "ManagedDataType.kt",
        "ManagementStrategy.kt",
        "ProcessorNode.kt",
    ],
    visibility = ["//java/com/google/android/libraries/pcc/chronicle:api_visibility"],
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

kt_android_library(
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

kt_android_library(
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

kt_android_library(
    name = "chronicle_dsl_annotation",
    srcs = ["ChronicleDsl.kt"],
)

kt_jvm_library(
    name = "chronicle_dsl_annotation_jvm",
    srcs = ["ChronicleDsl.kt"],
)

kt_android_library(
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
