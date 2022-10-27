load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "GenericPassThroughTest",
    srcs = ["GenericPassThroughTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/storage",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
