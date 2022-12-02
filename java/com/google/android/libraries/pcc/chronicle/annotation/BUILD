load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library", "kt_jvm_library")
load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:api_visibility"])

kt_android_library(
    name = "chronicle_data_annotation_android",
    srcs = [
        "ChronicleData.kt",
    ],
    visibility = ["//visibility:private"],
)

kt_jvm_library(
    name = "chronicle_data_annotation",
    srcs = [
        "ChronicleData.kt",
    ],
    visibility = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen/processor:__subpackages__",
    ],
)

kt_android_library(
    name = "chronicle_connection_annotation_android",
    srcs = [
        "ChronicleConnection.kt",
    ],
    visibility = ["//visibility:private"],
)

kt_jvm_library(
    name = "chronicle_connection_annotation",
    srcs = [
        "ChronicleConnection.kt",
    ],
    visibility = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen/processor:__subpackages__",
    ],
)

kt_android_library(
    name = "data_cache_store_annotation_android",
    srcs = [
        "DataCacheStore.kt",
    ],
    visibility = ["//visibility:private"],
    deps = ["//java/com/google/android/libraries/pcc/chronicle/api"],
)

kt_jvm_library(
    name = "data_cache_store_annotation",
    srcs = [
        "DataCacheStore.kt",
    ],
    visibility = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen/processor:__subpackages__",
    ],
    deps = ["//java/com/google/android/libraries/pcc/chronicle/api:api_jvm"],
)

# Exports @ChronicleData annotation and the processor.  (Used by clients of the annotation.)
android_library(
    name = "chronicle_data_android",
    exported_plugins = ["//java/com/google/android/libraries/pcc/chronicle/codegen/processor:chronicle_data_plugin"],
    visibility = ["//visibility:public"],
    exports = [
        ":chronicle_data_annotation_android",
    ],
)

# Exports @ChronicleConnection annotation and the processor.  (Used by clients of the annotation.)
android_library(
    name = "chronicle_connection_android",
    exported_plugins = ["//java/com/google/android/libraries/pcc/chronicle/codegen/processor:chronicle_connection_plugin"],
    visibility = ["//visibility:public"],
    exports = [
        ":chronicle_connection_annotation_android",
    ],
)

# Exports @DataCacheStore annotation and the processor.  (Used by clients of the annotation.)
android_library(
    name = "data_cache_store_android",
    exported_plugins = ["//java/com/google/android/libraries/pcc/chronicle/codegen/processor:data_cache_store_plugin"],
    visibility = ["//visibility:public"],
    exports = [
        ":data_cache_store_annotation_android",
    ],
)
