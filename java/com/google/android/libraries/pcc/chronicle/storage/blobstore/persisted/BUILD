load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = ["//visibility:public"])

kt_android_library(
    name = "persisted",
    srcs = [
        "PersistedBlobStore.kt",
        "PersistedBlobStoreManagement.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore:management_info",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore/db",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "//third_party/java/androidx/room",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)
