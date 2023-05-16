load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "in_memory",
    srcs = [
        "InMemoryBlobStore.kt",
        "InMemoryBlobStoreManagement.kt",
        "InMemoryStorage.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore",
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore:management_info",
        "//java/com/google/android/libraries/pcc/chronicle/util:timesource",
        "@maven//:org_jetbrains_kotlinx_atomicfu",
    ],
)
