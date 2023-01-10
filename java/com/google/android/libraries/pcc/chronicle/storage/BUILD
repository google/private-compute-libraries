load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//visibility:public"])

kt_android_library(
    name = "storage",
    srcs = glob(["*.kt"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "@maven//:org_jetbrains_kotlinx_atomicfu",
    ],
)
