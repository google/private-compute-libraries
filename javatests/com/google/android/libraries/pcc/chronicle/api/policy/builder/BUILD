load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")
load(
    "//java/com/google/android/libraries/pcc/chronicle/build_defs:chronicle_codegen.bzl",
    "chronicle_data_library",
)

chronicle_data_library(
    name = "base",
    testonly = True,
    srcs = [
        "PolicyBuilderTestBase.kt",
    ],
    deps = [],
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
        "@maven//:junit_junit",
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
        "@maven//:junit_junit",
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
        "@maven//:junit_junit",
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
        "@maven//:junit_junit",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)
