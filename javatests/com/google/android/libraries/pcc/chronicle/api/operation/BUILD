load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "DefaultOperationLibraryTest",
    srcs = ["DefaultOperationLibraryTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/operation",
        "@maven//:com_google_truth_truth",
    ],
)
