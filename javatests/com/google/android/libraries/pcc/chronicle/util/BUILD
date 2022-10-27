load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "TypedMapTest",
    srcs = ["TypedMapTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
