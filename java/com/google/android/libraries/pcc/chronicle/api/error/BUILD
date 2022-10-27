load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:api_visibility"])

CHRONICLE_ERROR_SRCS = ["ChronicleError.kt"]

kt_android_library(
    # split out to avoid circular imports.
    name = "chronicle_error",
    srcs = CHRONICLE_ERROR_SRCS,
    # Non-internal components should use :error.
    visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"],
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
        "//java/com/google/android/libraries/pcc/chronicle/api:connection",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
    ],
)
