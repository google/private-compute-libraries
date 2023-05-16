load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "fake_chronicle",
    testonly = True,
    srcs = [
        "FakeChronicle.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/analysis/impl",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:chronicle",
        "//java/com/google/android/libraries/pcc/chronicle/api/flags",
        "//java/com/google/android/libraries/pcc/chronicle/api/flags:fake_flags_reader",
        "//java/com/google/android/libraries/pcc/chronicle/api/integration",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy:default_policy_conformance_check",
    ],
)

android_library(
    name = "testutil",
    testonly = True,
    srcs = [
        "EmptyProcessorNode.kt",
        "TestConnectionRequest.kt",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
    ],
)
