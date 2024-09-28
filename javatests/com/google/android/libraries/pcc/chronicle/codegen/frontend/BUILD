load("//third_party/bazel_rules/rules_java/java:java_test.bzl", "java_test")
load(
    "//tools/build_defs/kotlin:rules.bzl",
    "kt_jvm_library",
)

java_test(
    name = "class_to_type_converter_test",
    test_class = "com.google.android.libraries.pcc.chronicle.codegen.frontend.ClassToTypeConverterTest",
    runtime_deps = [":class_to_type_converter_test_lib"],
)

kt_jvm_library(
    name = "class_to_type_converter_test_lib",
    testonly = True,
    srcs = ["ClassToTypeConverterTest.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/frontend",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:expected_types",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:source_classes",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

java_test(
    name = "class_to_type_converter_config_test",
    test_class = "com.google.android.libraries.pcc.chronicle.codegen.frontend.ClassToTypeConverterConfigTest",
    runtime_deps = [":class_to_type_converter_config_test_lib"],
)

kt_jvm_library(
    name = "class_to_type_converter_config_test_lib",
    testonly = True,
    srcs = ["ClassToTypeConverterConfigTest.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/frontend",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/testutil",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:expected_types",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:source_classes",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

java_test(
    name = "java_proto_to_type_converter_test",
    test_class = "com.google.android.libraries.pcc.chronicle.codegen.frontend.JavaProtoToTypeConverterTest",
    runtime_deps = [":java_proto_to_type_converter_test_lib"],
)

kt_jvm_library(
    name = "java_proto_to_type_converter_test_lib",
    testonly = True,
    srcs = ["JavaProtoToTypeConverterTest.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/frontend",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:expected_types",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:test_java_proto",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

java_test(
    name = "class_to_type_converter_with_proto_alternates_test",
    test_class = "com.google.android.libraries.pcc.chronicle.codegen.frontend.ClassToTypeConverterWithProtoAlternatesTest",
    runtime_deps = [":class_to_type_converter_with_proto_alternates_test_lib"],
)

kt_jvm_library(
    name = "class_to_type_converter_with_proto_alternates_test_lib",
    testonly = True,
    srcs = ["ClassToTypeConverterWithProtoAlternatesTest.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/frontend",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:combo_classes",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:expected_types",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:source_classes",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

java_test(
    name = "descriptor_to_type_converter_test",
    test_class = "com.google.android.libraries.pcc.chronicle.codegen.frontend.DescriptorToTypeConverterTest",
    runtime_deps = [":descriptor_to_type_converter_test_lib"],
)

kt_jvm_library(
    name = "descriptor_to_type_converter_test_lib",
    testonly = True,
    srcs = ["DescriptorToTypeConverterTest.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/frontend",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:expected_types",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:test_java_proto",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

java_test(
    name = "descriptor_to_type_converter_config_test",
    test_class = "com.google.android.libraries.pcc.chronicle.codegen.frontend.DescriptorToTypeConverterConfigTest",
    runtime_deps = [":descriptor_to_type_converter_config_test_lib"],
)

kt_jvm_library(
    name = "descriptor_to_type_converter_config_test_lib",
    testonly = True,
    srcs = ["DescriptorToTypeConverterConfigTest.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/frontend",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/testutil",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:expected_types",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:test_java_proto",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
