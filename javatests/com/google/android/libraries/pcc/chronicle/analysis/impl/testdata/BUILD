load("@bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_visibility = [
        "//javatests/com/google/android/libraries/pcc/chronicle/analysis/impl:__subpackages__",
    ],
)

android_library(
    name = "testdata",
    testonly = True,
    srcs = glob(["*.kt"]),
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
    ],
)
