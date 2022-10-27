load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"])

kt_jvm_library(
    name = "api",
    srcs = glob(["*.kt"]),
    deps = [
        "@maven//:com_squareup_javapoet",
        "@maven//:com_squareup_kotlinpoet",
    ],
)
