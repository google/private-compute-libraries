load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "ExtensionsTest",
    srcs = ["ExtensionsTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/serialization",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/remote/handler",
        "@maven//:com_google_truth_truth",
        "@maven//:com_nhaarman_mockitokotlin2_mockito_kotlin",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
        "@maven//:org_mockito_mockito_core",
    ],
)

kt_android_local_test(
    name = "RemoteComputeServerHandlerTest",
    srcs = ["RemoteComputeServerHandlerTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/serialization",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/remote/handler",
        "@maven//:com_google_truth_truth",
        "@maven//:com_nhaarman_mockitokotlin2_mockito_kotlin",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
        "@maven//:org_mockito_mockito_core",
    ],
)

kt_android_local_test(
    name = "RemoteStoreServerHandlerTest",
    srcs = ["RemoteStoreServerHandlerTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/serialization",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/remote/handler",
        "//third_party/java/proto:empty_java_proto_lite",
        "@maven//:com_google_truth_truth",
        "@maven//:com_nhaarman_mockitokotlin2_mockito_kotlin",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
        "@maven//:org_mockito_mockito_core",
    ],
)

kt_android_local_test(
    name = "RemoteStreamServerHandlerTest",
    srcs = ["RemoteStreamServerHandlerTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/serialization",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/remote/handler",
        "@maven//:com_google_truth_truth",
        "@maven//:com_nhaarman_mockitokotlin2_mockito_kotlin",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
        "@maven//:org_mockito_mockito_core",
    ],
)

kt_android_local_test(
    name = "RemoteServerHandlerFactoryTest",
    srcs = ["RemoteServerHandlerFactoryTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/remote",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/remote/handler",
        "@maven//:com_google_truth_truth",
        "@maven//:com_nhaarman_mockitokotlin2_mockito_kotlin",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_mockito_mockito_core",
    ],
)
