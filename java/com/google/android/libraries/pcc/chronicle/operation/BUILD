load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//visibility:public"])

kt_android_library(
    name = "empties",
    srcs = ["Empties.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
    ],
)

kt_android_library(
    name = "test_ops",
    testonly = True,
    srcs = ["TestOps.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
    ],
)
