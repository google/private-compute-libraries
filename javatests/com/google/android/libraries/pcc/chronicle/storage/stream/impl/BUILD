load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "EntityStreamImplTest",
    srcs = ["EntityStreamImplTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/testutil:testdata_java_proto_lite",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage/stream",
        "//java/com/google/android/libraries/pcc/chronicle/storage/stream/impl",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
    ],
)

kt_android_local_test(
    name = "EntityStreamProviderImplTest",
    srcs = ["EntityStreamProviderImplTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/storage/stream",
        "//java/com/google/android/libraries/pcc/chronicle/storage/stream/impl",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
    ],
)
