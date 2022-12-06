package(default_visibility = ["//visibility:public"])

java_library(
    name = "install_in",
    exported_plugins = [
        #":hilt_aggregated_deps_processor",
        ":hilt_generates_root_input_processor",
    ],
    exports = [
        "@maven//:com_google_dagger_hilt_android",
        "@maven//:com_google_dagger_hilt_core",
    ],
)

java_plugin(
    name = "hilt_android_entry_point_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.android.processor.internal.androidentrypoint.AndroidEntryPointProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)

java_plugin(
    name = "hilt_aggregated_deps_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.processor.internal.aggregateddeps.AggregatedDepsProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)

java_plugin(
    name = "hilt_alias_of_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.processor.internal.aliasof.AliasOfProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)

java_plugin(
    name = "hilt_define_component_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.processor.internal.definecomponent.DefineComponentProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)

java_plugin(
    name = "hilt_early_entry_points_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.processor.internal.earlyentrypoint.EarlyEntryPointProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)

java_plugin(
    name = "hilt_generates_root_input_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.processor.internal.generatesrootinput.GeneratesRootInputProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)

java_plugin(
    name = "hilt_originating_element_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.processor.internal.originatingelement.OriginatingElementProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)

java_plugin(
    name = "hilt_root_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.processor.internal.root.RootProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)

java_plugin(
    name = "hilt_component_tree_deps_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.processor.internal.root.ComponentTreeDepsProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)

java_plugin(
    name = "hilt_view_model_processor",
    generates_api = 1,
    processor_class = "dagger.hilt.android.processor.internal.viewmodel.ViewModelProcessor",
    deps = ["@maven//:com_google_dagger_hilt_android_compiler"],
)
