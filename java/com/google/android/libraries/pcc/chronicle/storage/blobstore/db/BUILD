load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "db",
    srcs = [
        "BlobDao.kt",
        "BlobDatabase.kt",
        "BlobEntity.kt",
        "PackageEntity.kt",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/storage/blobstore:management_info",
        "//third_party/java/androidx/room",
        "@maven//:androidx_room_room_ktx",
    ],
)
