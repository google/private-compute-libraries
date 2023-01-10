load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library", "kt_jvm_library")

package(default_visibility = [
    "//visibility:public",
])

kt_android_library(
    name = "logcat",
    srcs = ["Logcat.kt"],
    deps = [
        "@maven//:com_google_flogger_flogger",
    ],
)

kt_jvm_library(
    name = "typed_map",
    srcs = ["TypedMap.kt"],
    visibility = [
        "//visibility:public",
    ],
)

kt_android_library(
    name = "timesource",
    srcs = ["TimeSource.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    visibility = [
        "//visibility:public",
    ],
    deps = [],
)
