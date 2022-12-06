package(default_visibility = ["//visibility:public"])

java_library(
    name = "testing",
    testonly = True,
    exported_plugins = [
        "//third_party/java/dagger/hilt:hilt_android_entry_point_processor",
        "//third_party/java/dagger/hilt:hilt_alias_of_processor",
        "//third_party/java/dagger/hilt:hilt_define_component_processor",
        "//third_party/java/dagger/hilt:hilt_early_entry_points_processor",
        "//third_party/java/dagger/hilt:hilt_originating_element_processor",
        "//third_party/java/dagger/hilt:hilt_root_processor",
        "//third_party/java/dagger/hilt:hilt_component_tree_deps_processor",
        "//third_party/java/dagger/hilt:hilt_view_model_processor",
        ":hilt_bind_value_processor",
        ":hilt_custom_test_application_processor",
        ":hilt_uninstall_modules_processor",
    ],
    exports = [
        "@maven//:androidx_test_core",
        "@maven//:com_google_dagger_hilt_android",
        "@maven//:com_google_dagger_hilt_android_testing",
        "@maven//:com_google_dagger_hilt_core",
    ],
)

java_plugin(
    name = "hilt_custom_test_application_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.android.processor.internal.customtestapplication.CustomTestApplicationProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)

java_plugin(
    name = "hilt_bind_value_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.android.processor.internal.bindvalue.BindValueProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)

java_plugin(
    name = "hilt_uninstall_modules_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.processor.internal.uninstallmodules.UninstallModulesProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)
