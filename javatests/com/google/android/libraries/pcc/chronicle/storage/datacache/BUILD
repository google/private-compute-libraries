load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

SRCS_FOR_ROBOLECTRIC_KT_TEST = glob(
    ["*.kt"],
)

[kt_android_local_test(
    name = src_for_robolectric_kt_test[:-3],
    srcs = [src_for_robolectric_kt_test],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    shard_count = 1,
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/api/testutil",
        "//java/com/google/android/libraries/pcc/chronicle/storage/datacache",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "@maven//:com_google_truth_truth",
        "@maven//:com_nhaarman_mockitokotlin2_mockito_kotlin",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
    ],
) for src_for_robolectric_kt_test in SRCS_FOR_ROBOLECTRIC_KT_TEST]
