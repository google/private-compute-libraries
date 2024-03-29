load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = [
    "//visibility:public",
])

android_library(
    name = "management_info",
    srcs = [
        "ManagementInfo.kt",
    ],
    deps = [
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)

android_library(
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

android_library(
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

android_library(
    name = "core",
    srcs = [
        "BlobStoreCore.kt",
    ],
    associates = [
        # '_kt' suffix references the actual kotlin dependency from bazel_kotlin to access 'internal' members
        ":manager_kt",
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

android_library(
    name = "provider",
    srcs = [
        "BlobStoreProvider.kt",
    ],
    deps = [
        ":blobstore",
        ":management_info",
    ],
)
