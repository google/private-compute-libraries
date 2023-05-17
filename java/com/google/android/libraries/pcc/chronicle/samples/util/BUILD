load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle/samples:sample_visibility"])

android_library(
    name = "chronicle_helper",
    srcs = ["ChronicleHelper.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/analysis/impl",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:chronicle",
        "//java/com/google/android/libraries/pcc/chronicle/api/flags",
        "//java/com/google/android/libraries/pcc/chronicle/api/integration",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy:default_policy_conformance_check",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/remote",
        "//java/com/google/android/libraries/pcc/chronicle/remote/handler",
        "//java/com/google/android/libraries/pcc/chronicle/remote/impl",
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
    ],
)
