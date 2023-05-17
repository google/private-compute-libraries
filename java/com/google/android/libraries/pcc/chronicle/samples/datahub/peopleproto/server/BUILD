load("@bazel_rules_android//android:rules.bzl", "android_library")

android_library(
    name = "server",
    srcs = [
        "PeopleServer.kt",
        "PeopleStore.kt",
        "ServerLocalPeopleReader.kt",
        "ServerLocalPeopleWriter.kt",
    ],
    manifest = "//java/com/google/android/libraries/pcc/chronicle:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/serialization",
        "//java/com/google/android/libraries/pcc/chronicle/api/remote/server",
        "//java/com/google/android/libraries/pcc/chronicle/api/storage",
        "//java/com/google/android/libraries/pcc/chronicle/samples/datahub/peopleproto",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core_jvm",
    ],
)
