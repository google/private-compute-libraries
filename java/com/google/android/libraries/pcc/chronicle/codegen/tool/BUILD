load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"])

kt_jvm_library(
    name = "proto_chronicle_data_generator_lib",
    srcs = ["ProtoChronicleDataGenerator.kt"],
    visibility = [
        "//java/com/google/android/libraries/pcc/chronicle:__subpackages__",
        "//javatests/com/google/android/libraries/pcc/chronicle:__subpackages__",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend:datahub",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend/api",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend/lens",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/frontend",
        "@maven//:com_github_ajalt_clikt_clikt_jvm",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:com_squareup_javapoet",
        "@maven//:com_squareup_kotlinpoet",
    ],
)
