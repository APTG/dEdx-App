cmake_minimum_required(VERSION 3.22.1)
project("dEdx" C)

# Pre-built libdedx shared library (v1.4.0, data tables embedded)
add_library(dedx SHARED IMPORTED)
set_target_properties(dedx PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libdedx.so
)

add_library(dEdx SHARED dEdx.c)

target_include_directories(dEdx PRIVATE include)

find_library(log-lib log)
target_link_libraries(dEdx dedx ${log-lib})
