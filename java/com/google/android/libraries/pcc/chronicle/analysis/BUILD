load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = [
    "//java/com/google/android/libraries/pcc/chronicle:integration_visibility",
])

kt_android_library(
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
