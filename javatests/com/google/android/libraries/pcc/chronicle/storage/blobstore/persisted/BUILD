load("//tools/build_defs/kotlin:rules.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "PersistedBlobStoreTest",
    srcs = ["PersistedBlobStoreTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore:management_info",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore/db",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore/persisted",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "//javatests/com/google/android/libraries/pcc/chronicle/storage/blobstore:test_message_java_proto_lite",
        "//third_party/java/androidx/room",
        "@maven//:androidx_sqlite_sqlite",
        "@maven//:androidx_test_core",
        "@maven//:androidx_test_ext_junit",
        "@maven//:com_google_truth_truth",
        "@maven//:com_nhaarman_mockitokotlin2_mockito_kotlin",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
        "@maven//:org_mockito_mockito_core",
    ],
)

kt_android_local_test(
    name = "PersistedBlobStoreManagementTest",
    srcs = ["PersistedBlobStoreManagementTest.kt"],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore:management_info",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore/db",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore/persisted",
        "//javatests/com/google/android/libraries/pcc/chronicle/storage/blobstore:test_message_java_proto_lite",
        "//javatests/com/google/android/libraries/pcc/chronicle/storage/blobstore:test_person_java_proto_lite",
        "//third_party/java/androidx/room",
        "@maven//:androidx_sqlite_sqlite",
        "@maven//:androidx_test_core",
        "@maven//:androidx_test_ext_junit",
        "@maven//:com_google_truth_truth",
        "@maven//:com_nhaarman_mockitokotlin2_mockito_kotlin",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
        "@maven//:org_mockito_mockito_core",
    ],
)
