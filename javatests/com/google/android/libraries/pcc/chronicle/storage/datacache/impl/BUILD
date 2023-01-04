load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "DataCacheStorageDataRemovalDownstreamListenerTest",
    srcs = ["DataCacheStorageDataRemovalDownstreamListenerTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage/datacache",
        "//java/com/google/android/libraries/pcc/chronicle/storage/datacache/impl",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)

kt_android_local_test(
    name = "DataCacheStoragePackageDeletionListenerTest",
    srcs = ["DataCacheStoragePackageDeletionListenerTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    shard_count = 1,
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage/datacache",
        "//java/com/google/android/libraries/pcc/chronicle/storage/datacache/impl",
        "@maven//:androidx_test_core",
        "@maven//:androidx_test_ext_junit",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
        "@maven//:com_nhaarman_mockitokotlin2_mockito_kotlin",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_test",
        "@maven//:org_mockito_mockito_core",
    ],
)
