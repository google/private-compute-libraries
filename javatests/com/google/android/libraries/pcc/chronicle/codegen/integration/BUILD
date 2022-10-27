load(
    "//tools/build_defs/kotlin:rules.bzl",
    "kt_jvm_library",
)

kt_jvm_library(
    name = "ClassToDataTypeDescriptorTest_LIB",
    srcs = ["ClassToDataTypeDescriptorTest.kt"],
    data = ["//javatests/com/google/android/libraries/pcc/chronicle/codegen:dtd_data"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api:api_jvm",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend:datahub",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/frontend",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:source_classes",
        "@maven//:com_google_truth_truth",
        "@maven//:com_squareup_kotlinpoet",
        "@maven//:junit_junit",
    ],
)

java_test(
    name = "ClassToDataTypeDescriptorTest",
    runtime_deps = [":ClassToDataTypeDescriptorTest_LIB"],
)

kt_jvm_library(
    name = "ElementToDataTypeDescriptorTest_LIB",
    srcs = ["ElementToDataTypeDescriptorTest.kt"],
    data = ["//javatests/com/google/android/libraries/pcc/chronicle/codegen:dtd_data"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api:api_jvm",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend:datahub",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/frontend/element",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/testutil",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:auto_value_in_data_class",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:nested_autovalue",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:simple_autovalue",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:source_classes",
        "@maven//:com_google_truth_truth",
        "@maven//:com_squareup_kotlinpoet",
        "@maven//:junit_junit",
    ],
)

java_test(
    name = "ElementToDataTypeDescriptorTest",
    runtime_deps = [":ElementToDataTypeDescriptorTest_LIB"],
)
