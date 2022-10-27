load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(
    default_visibility = [
        "//java/com/google/android/libraries/pcc/chronicle:api_visibility",
    ],
)

kt_android_library(
    name = "builder",
    srcs = glob(["*.kt"]),
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/contextrules",
    ],
)
