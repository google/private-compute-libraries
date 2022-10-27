load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")

package(default_visibility = [
    "//java/com/google/android/libraries/pcc/chronicle:integration_visibility",
])

kt_android_library(
    name = "management_info",
    srcs = [
        "ManagementInfo.kt",
    ],
    deps = [
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)

kt_android_library(
    name = "blobstore",
    srcs = [
        "BlobStore.kt",
        "BlobStoreManagement.kt",
    ],
    deps = [
        ":management_info",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
    ],
)

kt_android_library(
    name = "manager",
    srcs = [
        "BlobStoreManager.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        ":blobstore",
        ":management_info",
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "@maven//:org_jetbrains_kotlinx_atomicfu",
    ],
)

kt_android_library(
    name = "core",
    srcs = [
        "BlobStoreCore.kt",
    ],
    deps = [
        ":blobstore",
        ":management_info",
        ":manager",
        ":provider",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore/db",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore/inmemory:in_memory",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore/persisted",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "//third_party/java/androidx/room",
    ],
)

kt_android_library(
    name = "provider",
    srcs = [
        "BlobStoreProvider.kt",
    ],
    deps = [
        ":blobstore",
        ":management_info",
    ],
)
