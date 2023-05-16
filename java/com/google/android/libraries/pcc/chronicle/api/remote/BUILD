load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = [
    "//visibility:public",
])

# This target and remote_java below are a workaround to a bug in the Bazel
# kt_android_library related to idl_parcelables not working correctly.
# For more information, see:
# https://github.com/bazelbuild/rules_kotlin/issues/228
# If that bug is ever resolved within bazel's kt_android_library, we can merge
# :remote_deps and :remote_java into :remote.
android_library(
    name = "remote_deps",
    srcs = [
        "RemoteError.kt",
        "RemoteRequest.kt",
        "RemoteRequestMetadataExt.kt",
        "RemoteResponse.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    visibility = ["//visibility:private"],
    deps = [
        ":remote_api_java_proto_lite",
        ":remote_entity",
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
    ],
)

android_library(
    name = "remote_java",
    idl_parcelables = [
        "RemoteError.aidl",
        "RemoteRequest.aidl",
        "RemoteResponse.aidl",
    ],
    idl_srcs = [
        "ICancellationSignal.aidl",
        "IRemote.aidl",
        "IResponseCallback.aidl",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    visibility = ["//visibility:private"],
    deps = [
        ":remote_api_java_proto_lite",
        ":remote_deps",
    ],
)

android_library(
    name = "remote",
    srcs = [
        "IResponseCallbackExt.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    exports = [
        ":remote_api_java_proto_lite",
        ":remote_deps",
        ":remote_entity",
        ":remote_java",
    ],
    deps = [
        ":remote_api_java_proto_lite",
        ":remote_deps",
        ":remote_java",
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
    ],
)

android_library(
    name = "remote_entity",
    srcs = ["RemoteEntity.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)

proto_library(
    name = "remote_api_proto",
    srcs = [
        "remote_api.proto",
    ],
    visibility = ["//visibility:private"],
    deps = ["@com_google_protobuf//:empty_proto"],
)

java_lite_proto_library(
    name = "remote_api_java_proto_lite",
    visibility = ["//visibility:private"],
    deps = [":remote_api_proto"],
)
