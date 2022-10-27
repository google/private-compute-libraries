load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "ParcelableSerializerTest",
    srcs = ["ParcelableSerializerTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote:remote_entity",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/serialization",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/testutil:testdata_java_proto_lite",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "@maven//:com_google_truth_truth",
    ],
)

kt_android_local_test(
    name = "ProtoSerializerTest",
    srcs = ["ProtoSerializerTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote:remote_entity",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/serialization",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/testutil:testdata_java_proto_lite",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:com_google_truth_truth",
    ],
)
