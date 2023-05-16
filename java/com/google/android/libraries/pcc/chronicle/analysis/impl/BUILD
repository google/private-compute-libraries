load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = [
    "//visibility:public",
])

android_library(
    name = "impl",
    srcs = glob(["*.kt"]),
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:sandbox_processor_node",
        "//java/com/google/android/libraries/pcc/chronicle/api/cantrip",
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
        "//java/com/google/android/libraries/pcc/chronicle/api/optics",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
        "@maven//:androidx_annotation_annotation",
    ],
)
