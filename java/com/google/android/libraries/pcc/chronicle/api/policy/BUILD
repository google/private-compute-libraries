load("@bazel_rules_android//android:rules.bzl", "android_library")
load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = [
    "//visibility:public",
])

kt_jvm_library(
    name = "policy",
    srcs = [
        "Policy.kt",
        "PolicyConformanceCheck.kt",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/capabilities",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/contextrules",
    ],
)

android_library(
    name = "default_policy_conformance_check",
    srcs = ["DefaultPolicyConformanceCheck.kt"],
    deps = [
        ":policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
    ],
)
