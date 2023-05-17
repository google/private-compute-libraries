load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "empties",
    srcs = ["Empties.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
    ],
)

android_library(
    name = "test_ops",
    testonly = True,
    srcs = ["TestOps.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
    ],
)
