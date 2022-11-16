load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "IResponseCallbackExtTest",
    srcs = ["IResponseCallbackExtTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)

kt_android_local_test(
    name = "RemoteEntityTest",
    srcs = ["RemoteEntityTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote:remote_entity",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/testutil:testdata_java_proto_lite",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)

kt_android_local_test(
    name = "RemoteRequestTest",
    srcs = ["RemoteRequestTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/testutil:testdata_java_proto_lite",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//third_party/java/proto:empty_java_proto_lite",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)

kt_android_local_test(
    name = "RemoteRequestMetadataExtTest",
    srcs = ["RemoteRequestMetadataExtTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//third_party/java/proto:empty_java_proto_lite",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)

kt_android_local_test(
    name = "RemoteResponseTest",
    srcs = ["RemoteResponseTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/testutil:testdata_java_proto_lite",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)

kt_android_local_test(
    name = "RemoteErrorTest",
    srcs = ["RemoteErrorTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)

kt_android_local_test(
    name = "RemoteStoreIntegrationTest",
    srcs = ["RemoteStoreIntegrationTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/analysis",
        "//java/com/google/android/libraries/pcc/chronicle/analysis/impl",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:chronicle",
        "//java/com/google/android/libraries/pcc/chronicle/api/error",
        "//java/com/google/android/libraries/pcc/chronicle/api/flags",
        "//java/com/google/android/libraries/pcc/chronicle/api/flags:fake_flags_reader",
        "//java/com/google/android/libraries/pcc/chronicle/api/integration",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy:default_policy_conformance_check",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote:remote_api_java_proto_lite",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote:remote_deps",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/client",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/serialization",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/remote",
        "//java/com/google/android/libraries/pcc/chronicle/remote/handler",
        "//java/com/google/android/libraries/pcc/chronicle/remote/impl",
        "//java/com/google/android/libraries/pcc/chronicle/storage/datacache",
        "//java/com/google/android/libraries/pcc/chronicle/storage/datacache/impl",
        "//java/com/google/android/libraries/pcc/chronicle/storage/stream",
        "//java/com/google/android/libraries/pcc/chronicle/storage/stream/impl",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "//javatests/com/google/android/libraries/pcc/chronicle/api/remote/testutil",
        "//third_party/java/grpc:inprocess_android",
        "//third_party/java/grpc:testing_android",
        "@maven//:com_google_truth_truth",
        "@maven//:com_nhaarman_mockitokotlin2_mockito_kotlin",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)
