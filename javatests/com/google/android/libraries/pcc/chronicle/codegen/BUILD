load(
    "//tools/build_defs/kotlin:rules.bzl",
    "kt_android_library",
    "kt_jvm_library",
)

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"])

kt_jvm_library(
    name = "source_classes",
    testonly = True,
    srcs = ["SourceClasses.kt"],
    deps = [
    ],
)

kt_android_library(
    name = "source_classes_android",
    testonly = True,
    srcs = ["SourceClasses.kt"],
    deps = [
    ],
)

kt_jvm_library(
    name = "combo_classes",
    testonly = True,
    srcs = ["ComboClasses.kt"],
    deps = [
        ":test_java_proto",
    ],
)

kt_android_library(
    name = "combo_classes_android",
    testonly = True,
    srcs = ["ComboClasses.kt"],
    deps = [
        ":test_java_proto_lite",
    ],
)

kt_jvm_library(
    name = "expected_types",
    testonly = True,
    srcs = ["ExpectedTypes.kt"],
    deps = [
        ":source_classes",
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
    ],
)

java_library(
    name = "simple_autovalue",
    testonly = True,
    srcs = ["SimpleAutoValue.java"],
    deps = [
        "//third_party/java/auto:auto_value",
        "@maven//:com_google_guava_guava",
    ],
)

java_library(
    name = "nested_autovalue",
    testonly = True,
    srcs = ["NestedAutoValue.java"],
    deps = [
        ":simple_autovalue",
        ":source_classes",
        "//third_party/java/auto:auto_value",
        "@maven//:com_google_guava_guava",
    ],
)

kt_jvm_library(
    name = "auto_value_in_data_class",
    testonly = True,
    srcs = ["AutoValueInDataClass.kt"],
    deps = [
        ":nested_autovalue",
        ":simple_autovalue",
    ],
)

proto_library(
    name = "test_proto",
    testonly = True,
    srcs = [
        "test_proto.proto",
        "test_sub_proto.proto",
    ],
    deps = [
        "@com_google_protobuf//:timestamp_proto",
    ],
)

proto_library(
    name = "simple_test_protos",
    srcs = ["simple_test_protos.proto"],
)

java_proto_library(
    name = "simple_test_java_proto",
    deps = [":simple_test_protos"],
)

java_lite_proto_library(
    name = "simple_test_java_proto_lite",
    deps = [":simple_test_protos"],
)

kt_jvm_library(
    name = "TypeLocationTest_LIB",
    srcs = ["TypeLocationTest.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

java_test(
    name = "TypeLocationTest",
    runtime_deps = [":TypeLocationTest_LIB"],
)

java_proto_library(
    name = "test_java_proto",
    deps = [":test_proto"],
)

java_lite_proto_library(
    name = "test_java_proto_lite",
    deps = [":test_proto"],
)

filegroup(
    name = "test_manifest_data",
    srcs = glob(["testdata/manifest/*"]),
)

filegroup(
    name = "ingress_conversion_code_data",
    srcs = glob(["testdata/ingress/*"]),
)

filegroup(
    name = "particle_code_data",
    srcs = glob(["testdata/particle/*"]),
)

filegroup(
    name = "publisher_data",
    srcs = glob(["testdata/publisher/*"]),
)

filegroup(
    name = "dtd_data",
    srcs = glob(["testdata/datatypedescriptor/*"]),
)

filegroup(
    name = "arcs_schemas_data",
    srcs = glob(["testdata/arcs_schema/*"]),
)
