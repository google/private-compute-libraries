load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"])

kt_android_local_test(
    name = "BlobStoreIntegrationTest",
    srcs = ["BlobStoreIntegrationTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        ":test_message_java_proto_lite",
        ":test_person_java_proto_lite",
        ":test_place_java_proto_lite",
        ":test_shape_java_proto_lite",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore:core",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore:management_info",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore:manager",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore:provider",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore/inmemory:in_memory",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore/persisted",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "@maven//:androidx_test_core",
        "@maven//:androidx_test_ext_junit",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_jetbrains_kotlin_kotlin_test",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)

proto_library(
    name = "test_message_proto",
    srcs = ["test_message.proto"],
)

java_lite_proto_library(
    name = "test_message_java_proto_lite",
    deps = [":test_message_proto"],
)

proto_library(
    name = "test_person_proto",
    srcs = ["test_person.proto"],
)

java_lite_proto_library(
    name = "test_person_java_proto_lite",
    deps = [":test_person_proto"],
)

proto_library(
    name = "test_shape_proto",
    srcs = ["test_shape.proto"],
)

java_lite_proto_library(
    name = "test_shape_java_proto_lite",
    deps = [":test_shape_proto"],
)

proto_library(
    name = "test_place_proto",
    srcs = ["test_place.proto"],
)

java_lite_proto_library(
    name = "test_place_java_proto_lite",
    deps = [":test_place_proto"],
)
