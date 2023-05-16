load("@bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_visibility = ["//visibility:public"],
)

android_library(
    name = "impl",
    srcs = [
        "DataCacheStorageDataRemovalDownstreamListener.kt",
        "DataCacheStorageImpl.kt",
        "DataCacheStoragePackageDeletionListener.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage/datacache",
        "//java/com/google/android/libraries/pcc/chronicle/util:logcat",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_jetbrains_kotlinx_atomicfu",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_guava",
    ],
)
