load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "flags",
    srcs = [
        "Flags.kt",
        "FlagsReader.kt",
    ],
    deps = [
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
    ],
)

android_library(
    name = "fake_flags_reader",
    testonly = True,
    srcs = [
        "FakeFlagsReader.kt",
    ],
    deps = [
        ":flags",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
    ],
)
