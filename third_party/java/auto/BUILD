package(default_visibility = ["//visibility:public"])

java_library(
    name = "auto_value",
    exported_plugins = [
        ":auto_annotation_plugin",
        ":auto_builder_plugin",
        ":auto_oneof_plugin",
        ":auto_value_plugin",
        ":auto_value_builder_plugin",
    ],
    exports = [
        "@maven//:com_google_auto_value_auto_value",
        "@maven//:com_google_auto_value_auto_value_annotations",
    ],
)

java_plugin(
    name = "auto_builder_plugin",
    processor_class = "com.google.auto.value.processor.AutoBuilderProcessor",
    tags = [
        "annotation=com.google.auto.value.AutoBuilder;" +
        "genclass=${package}.AutoBuilder_${outerclasses}${classname}",
    ],
    deps = [
        "@maven//:com_google_auto_value_auto_value",
    ],
)

java_plugin(
    name = "auto_oneof_plugin",
    processor_class = "com.google.auto.value.processor.AutoOneOfProcessor",
    tags = [
        "annotation=com.google.auto.value.AutoOneOf;" +
        "genclass=${package}.AutoOneOf_${outerclasses}${classname}",
    ],
    deps = [
        "@maven//:com_google_auto_value_auto_value",
    ],
)

java_plugin(
    name = "auto_annotation_plugin",
    processor_class = "com.google.auto.value.processor.AutoAnnotationProcessor",
    tags = [
        "annotation=com.google.auto.value.AutoAnnotation;" +
        "genclass=${package}.AutoAnnotation_${outerclasses}${classname}_${methodname}",
    ],
    deps = [
        "@maven//:com_google_auto_value_auto_value",
    ],
)

java_plugin(
    name = "auto_value_plugin",
    processor_class = "com.google.auto.value.processor.AutoValueProcessor",
    tags = [
        "annotation=com.google.auto.value.AutoValue;" +
        "genclass=${package}.AutoValue_${outerclasses}${classname}",
    ],
    deps = [
        "@maven//:com_google_auto_value_auto_value",
    ],
)

java_plugin(
    name = "auto_value_builder_plugin",
    processor_class = "com.google.auto.value.processor.AutoValueBuilderProcessor",
    tags = [
        "annotation=com.google.auto.value.AutoValue.Builder;",
    ],
    deps = [
        "@maven//:com_google_auto_value_auto_value",
    ],
)

java_plugin(
    name = "com_google_auto_service_auto_service_processor",
    neverlink = True,
    processor_class = "com.google.auto.service.processor.AutoServiceProcessor",
    deps = ["@maven//:com_google_auto_service_auto_service"],
)

java_library(
    name = "auto_service",
    exported_plugins = [
        ":com_google_auto_service_auto_service_processor",
    ],
    exports = [
        "@maven//:com_google_auto_service_auto_service_annotations",
    ],
)
