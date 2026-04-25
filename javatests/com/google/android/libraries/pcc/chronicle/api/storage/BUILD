load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "ExtensionsTest",
    srcs = ["ExtensionsTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "@com_google_protobuf//:timestamp_proto_kt_proto_lite",
        "@maven//:com_google_truth_truth",
    ],
)
