package(default_visibility = ["//visibility:public"])

java_plugin(
    name = "androidx_room_room_compiler_plugin",
    generates_api = True,
    processor_class = "androidx.room.RoomProcessor",
    deps = ["@maven//:androidx_room_room_compiler"],
)

java_library(
    name = "room",
    exported_plugins = [
        ":androidx_room_room_compiler_plugin",
    ],
    exports = [
        "@maven//:androidx_lifecycle_lifecycle_livedata",
        "@maven//:androidx_room_room_common",
        "@maven//:androidx_room_room_compiler",
        "@maven//:androidx_room_room_runtime",
    ],
)
