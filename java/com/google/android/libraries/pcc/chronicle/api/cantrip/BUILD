load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "cantrip",
    srcs = glob(["*.kt"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics",
    ],
)
