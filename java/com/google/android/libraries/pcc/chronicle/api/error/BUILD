load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library", "kt_jvm_library")

package(default_visibility = ["//visibility:public"])

CHRONICLE_ERROR_SRCS = ["ChronicleError.kt"]

kt_android_library(
    # split out to avoid circular imports.
    name = "chronicle_error",
    srcs = CHRONICLE_ERROR_SRCS,
    # Non-internal components should use :error.
    visibility = ["//visibility:public"],
)

kt_jvm_library(
    # split out to avoid circular imports.
    name = "chronicle_error_jvm",
    srcs = CHRONICLE_ERROR_SRCS,
    # Non-internal components should use :error.
    visibility = ["//visibility:public"],
)

kt_android_library(
    name = "error",
    srcs = glob(
        ["*.kt"],
        exclude = CHRONICLE_ERROR_SRCS,
    ),
    exports = [":chronicle_error"],
    deps = [
        ":chronicle_error",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
    ],
)

kt_jvm_library(
    name = "error_jvm",
    srcs = glob(
        ["*.kt"],
        exclude = CHRONICLE_ERROR_SRCS,
    ),
    exports = [":chronicle_error_jvm"],
    deps = [
        ":chronicle_error_jvm",
        "//java/com/google/android/libraries/pcc/chronicle/api:api_jvm",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
    ],
)
