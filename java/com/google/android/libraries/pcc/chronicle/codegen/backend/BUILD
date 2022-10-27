load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = ["//java/com/google/android/libraries/pcc/chronicle:internal_visibility"])

kt_jvm_library(
    name = "datahub",
    srcs = [
        "ConnectionProviderTypeProvider.kt",
        "ConnectionsPropertyProvider.kt",
        "DataTypeDescriptorDaggerProvider.kt",
        "DataTypeDescriptorPropertyProvider.kt",
        "ManagedDataCacheStorageDaggerProvider.kt",
        "TypeToDataTypeDescriptor.kt",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api:connection_jvm",
        "//java/com/google/android/libraries/pcc/chronicle/api:data_type_descriptor_jvm",
        "//java/com/google/android/libraries/pcc/chronicle/codegen",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/backend/api",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/typeconversion",
        "//java/com/google/android/libraries/pcc/chronicle/codegen/util",
        "@maven//:com_squareup_javapoet",
        "@maven//:com_squareup_kotlinpoet",
    ],
)
