load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:api_visibility"])

kt_android_library(
    name = "flags",
    srcs = [
        "Flags.kt",
        "FlagsReader.kt",
    ],
    deps = [
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)

kt_android_library(
    name = "fake_flags_reader",
    testonly = True,
    srcs = [
        "FakeFlagsReader.kt",
    ],
    deps = [
        ":flags",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)
