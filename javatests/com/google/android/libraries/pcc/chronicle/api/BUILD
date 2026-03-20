load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "ConnectionTest",
    srcs = ["ConnectionTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api:connection",
        "@maven//:com_google_truth_truth",
    ],
)

kt_android_local_test(
    name = "ConnectionRequestTest",
    srcs = ["ConnectionRequestTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "@maven//:com_google_truth_truth",
    ],
)

kt_android_local_test(
    name = "DataTypeTest",
    srcs = ["DataTypeTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "@maven//:com_google_truth_truth",
    ],
)

kt_android_local_test(
    name = "DataTypeDescriptorBuilderTest",
    srcs = ["DataTypeDescriptorBuilderTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api:data_type_descriptor",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)

kt_android_local_test(
    name = "DefaultDataRemovalRequestListenerTest",
    srcs = ["DefaultDataRemovalRequestListenerTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:default_data_removal_request_listener",
        "//java/com/google/android/libraries/pcc/chronicle/api/flags",
        "//java/com/google/android/libraries/pcc/chronicle/api/flags:fake_flags_reader",
        "@maven//:androidx_test_core",
        "@maven//:androidx_test_ext_junit",
        "@maven//:com_google_truth_truth",
    ],
)

kt_android_local_test(
    name = "ManagedDataTypeTest",
    srcs = ["ManagedDataTypeTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "@maven//:com_google_truth_truth",
    ],
)
