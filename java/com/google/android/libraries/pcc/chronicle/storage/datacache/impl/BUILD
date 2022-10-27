load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(
    default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:datacache_impl_visibility"],
)

kt_android_library(
    name = "impl",
    srcs = glob(
        ["*.kt"],
        exclude = ["NoOpModule.kt"],
    ),
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/flags",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage/datacache",
        "//java/com/google/android/libraries/pcc/chronicle/util:logcat",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt_android",
        "@maven//:com_google_flogger_flogger",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_jetbrains_kotlinx_atomicfu",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_guava",
    ],
)

kt_android_library(
    name = "noop_module",
    srcs = ["NoOpModule.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt_android",
        "@maven//:javax_inject_javax_inject",
    ],
)
