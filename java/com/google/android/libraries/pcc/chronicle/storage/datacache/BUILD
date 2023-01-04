load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(
    default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:datacache_visibility"],
)

kt_android_library(
    name = "datacache",
    srcs = glob(["*.kt"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "@maven//:com_google_guava_guava",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_guava",
    ],
)
