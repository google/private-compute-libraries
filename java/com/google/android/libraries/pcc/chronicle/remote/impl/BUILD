load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "remote_policy_checker",
    srcs = [
        "RemotePolicyCheckerImpl.kt",
    ],
    visibility = [
        "//visibility:public",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:chronicle",
        "//java/com/google/android/libraries/pcc/chronicle/api:sandbox_processor_node",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/remote",
    ],
)

android_library(
    name = "impl",
    srcs = [
        "ClientDetailsProviderImpl.kt",
        "RemoteContextImpl.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    exports = [
        ":remote_policy_checker",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/remote",
    ],
)
