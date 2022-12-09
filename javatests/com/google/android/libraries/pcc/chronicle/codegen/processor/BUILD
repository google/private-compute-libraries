load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")
load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

## Libraries

java_library(
    name = "jvm_deps",
    srcs = [],
    exports = [
        "//java/com/google/android/libraries/pcc/chronicle/api:api_jvm",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

kt_android_local_test(
    name = "ChronicleDataAnnotationProcessorKotlinTest",
    srcs = ["ChronicleDataAnnotationProcessorKotlinTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api:data_type_descriptor",
        "//java/com/google/android/libraries/pcc/chronicle/storage/datacache",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen/processor/testdata/annotatedtypes:chronicle_data_kt",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen/processor/testdata/expectedgeneratedtypes:expected_generated_kotlin_types",
        "@maven//:com_google_truth_truth",
    ],
)

kt_android_local_test(
    name = "ChronicleDataAnnotationProcessorJavaTest",
    srcs = ["ChronicleDataAnnotationProcessorJavaTest.java"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api:data_type_descriptor",
        "//java/com/google/android/libraries/pcc/chronicle/storage/datacache",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen/processor/testdata/annotatedtypes:chronicle_data_java",
        "//javatests/com/google/android/libraries/pcc/chronicle/codegen/processor/testdata/expectedgeneratedtypes:expected_generated_java_types",
        "@maven//:com_google_truth_truth",
    ],
)

## Compile test

# There is no android-variant, because there is only a kt_jvm_library version of "chronicle_data_processor", since it was only meant to run on build machines, not Android.

kt_jvm_library(
    name = "ChronicleDataAnnotationProcessorCompileTest_LIB",
    testonly = True,
    srcs = ["ChronicleDataAnnotationProcessorCompileTest.kt"],
    resources = ["//javatests/com/google/android/libraries/pcc/chronicle/codegen/processor/testdata/annotatedtypes"],
    deps = [
        ":jvm_deps",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/processor:chronicle_connection_processor",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/processor:chronicle_data_processor",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/processor:data_cache_store_processor",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_testing_compile_compile_testing",  # TODO(b/198787009): support Kotlin compile testing
        "@maven//:junit_junit",
    ],
)

java_test(
    name = "chronicle_data_annotation_processor_compile_test",
    resources = ["//javatests/com/google/android/libraries/pcc/chronicle/codegen/processor/testdata/annotatedtypes"],
    test_class = "com.google.android.libraries.pcc.chronicle.codegen.processor.ChronicleDataAnnotationProcessorCompileTest",
    runtime_deps = [
        ":ChronicleDataAnnotationProcessorCompileTest_LIB",
    ],
)

kt_jvm_library(
    name = "DataCacheStoreAnnotationProcessorDurationTest_LIB",
    testonly = True,
    srcs = ["DataCacheStoreAnnotationProcessorDurationTest.kt"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/codegen/processor:data_cache_store_processor",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
    ],
)

java_test(
    name = "DataCacheStoreAnnotationProcessorDurationTest",
    test_class = "com.google.android.libraries.pcc.chronicle.codegen.processor.DataCacheStoreAnnotationProcessorDurationTest",
    runtime_deps = [
        ":DataCacheStoreAnnotationProcessorDurationTest_LIB",
    ],
)
