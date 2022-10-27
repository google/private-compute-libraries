load(
    "//tools/build_defs/kotlin:rules.bzl",
    "kt_jvm_library",
)

kt_jvm_library(
    name = "ProtoChronicleDataGeneratorTest_LIB",
    srcs = ["ProtoChronicleDataGeneratorTest.kt"],
    resources = glob(["goldens/**"]),
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api:api_jvm",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend/api",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/tool:proto_chronicle_data_generator_lib",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:simple_test_java_proto",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen:test_java_proto",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:com_google_truth_truth",
        "@maven//:com_squareup_javapoet",
        "@maven//:com_squareup_kotlinpoet",
        "@maven//:junit_junit",
    ],
)

java_test(
    name = "ProtoChronicleDataGeneratorTest",
    runtime_deps = [":ProtoChronicleDataGeneratorTest_LIB"],
)
