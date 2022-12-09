load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library", "kt_android_local_test")

kt_android_library(
    name = "base",
    testonly = True,
    srcs = [
        "PolicyBuilderTestBase.kt",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/annotation:chronicle_data_android",
        "//java/com/google/android/libraries/pcc/chronicle/api",
    ],
)

kt_android_local_test(
    name = "PolicyBuilderTest",
    srcs = ["PolicyBuilderTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        ":base",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api:data_type_descriptor",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/contextrules",
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)

kt_android_local_test(
    name = "PolicyConfigBuilderTest",
    srcs = ["PolicyConfigBuilderTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        ":base",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/contextrules",
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)

kt_android_local_test(
    name = "PolicyFieldBuilderTest",
    srcs = ["PolicyFieldBuilderTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        ":base",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/contextrules",
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)

kt_android_local_test(
    name = "PolicyTargetBuilderTest",
    srcs = ["PolicyTargetBuilderTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        ":base",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/builder",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/contextrules",
        "//java/com/google/android/libraries/pcc/chronicle/util:typed_map",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)
