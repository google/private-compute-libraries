load("//third_party/bazel_rules/rules_java/java:java_test.bzl", "java_test")
load(
    "//tools/build_defs/kotlin:rules.bzl",
    "kt_jvm_library",
)

[
    [
        kt_jvm_library(
            name = "%s_LIB" % file_name[:-3],
            srcs = [file_name],
            data = ["//javatests/com/google/android/libraries/pcc/chronicle/codegen:dtd_data"],
            deps = [
                "//java/com/google/android/libraries/pcc/chronicle/api:api_jvm",
                "//java/com/google/android/libraries/pcc/chronicle/codegen",
                "//java/com/google/android/libraries/pcc/chronicle/codegen/backend:datahub",
                "//java/com/google/android/libraries/pcc/chronicle/codegen/backend/api",
                "//javatests/com/google/android/libraries/pcc/chronicle/codegen:expected_types",
                "//javatests/com/google/android/libraries/pcc/chronicle/codegen:source_classes",
                "//third_party/kotlin/ktfmt/google/java/com/google/ktfmt:KtfmtComparer",
                "@maven//:com_google_truth_truth",
                "@maven//:com_squareup_javapoet",
                "@maven//:com_squareup_kotlinpoet",
                "@maven//:junit_junit",
            ],
        ),
        java_test(
            name = file_name[:-3],
            runtime_deps = [":%s_LIB" % file_name[:-3]],
        ),
    ]
    for file_name in glob(["*.kt"])
]
