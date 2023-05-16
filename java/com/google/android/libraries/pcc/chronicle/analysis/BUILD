load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = [
    "//visibility:public",
])

android_library(
    name = "analysis",
    srcs = glob(["*.kt"]),
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/cantrip",
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
    ],
)
