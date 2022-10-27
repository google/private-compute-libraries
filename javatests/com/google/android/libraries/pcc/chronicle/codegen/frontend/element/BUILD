load(
    "//tools/build_defs/kotlin:rules.bzl",
    "kt_jvm_library",
)

kt_jvm_library(
    name = "element_to_type_converter_test",
    testonly = True,
    srcs = ["ElementToTypeConverterTest.kt"],
    data = [
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:test_proto",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/frontend/element",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/testutil",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:auto_value_in_data_class",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:expected_types",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:nested_autovalue",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:simple_autovalue",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:source_classes",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

java_test(
    name = "ElementToTypeConverterTest",
    runtime_deps = [
        ":element_to_type_converter_test",
    ],
)
