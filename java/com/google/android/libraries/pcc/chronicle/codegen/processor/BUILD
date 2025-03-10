load("//third_party/bazel_rules/rules_java/java:java_plugin.bzl", "java_plugin")
load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = ["//visibility:public"])

kt_jvm_library(
    name = "annotation_processor",
    srcs = ["AnnotationProcessor.kt"],
    visibility = [
        "//visibility:private",
    ],
)

kt_jvm_library(
    name = "chronicle_data_processor",
    srcs = ["ChronicleDataAnnotationProcessor.kt"],
    deps = [
        ":annotation_processor",
        "//java/com/google/android/libraries/pcc/chronicle/annotation:chronicle_data_annotation",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend:datahub",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend/api",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/frontend/element",
        "//third_party/kotlin/kotlin_symbol_processing:meta_inf_services",
        "@maven//:com_squareup_kotlinpoet",
    ],
)

kt_jvm_library(
    name = "chronicle_connection_processor",
    srcs = ["ChronicleConnectionAnnotationProcessor.kt"],
    deps = [
        ":annotation_processor",
        "//java/com/google/android/libraries/pcc/chronicle/annotation:chronicle_connection_annotation",
        "//java/com/google/android/libraries/pcc/chronicle/annotation:chronicle_data_annotation",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend:datahub",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend/api",
        "//third_party/kotlin/kotlin_symbol_processing:meta_inf_services",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_squareup_kotlinpoet",
    ],
)

kt_jvm_library(
    name = "data_cache_store_processor",
    srcs = ["DataCacheStoreAnnotationProcessor.kt"],
    deps = [
        ":annotation_processor",
        "//java/com/google/android/libraries/pcc/chronicle/annotation:data_cache_store_annotation",
        "//java/com/google/android/libraries/pcc/chronicle/api:api_jvm",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/util",
        "@maven//:com_squareup_kotlinpoet",
    ],
)

# Builds our annotation processor as a compiler plugin.
java_plugin(
    name = "chronicle_data_plugin",
    generates_api = True,
    processor_class = "com.google.android.libraries.pcc.chronicle.codegen.processor.ChronicleDataAnnotationProcessor",
    deps = [":chronicle_data_processor"],
)

java_plugin(
    name = "chronicle_connection_plugin",
    generates_api = True,
    processor_class = "com.google.android.libraries.pcc.chronicle.codegen.processor.ChronicleConnectionAnnotationProcessor",
    deps = [":chronicle_connection_processor"],
)

java_plugin(
    name = "data_cache_store_plugin",
    generates_api = True,
    processor_class = "com.google.android.libraries.pcc.chronicle.codegen.processor.DataCacheStoreAnnotationProcessor",
    deps = [":data_cache_store_processor"],
)
