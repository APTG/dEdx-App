cmake_minimum_required(VERSION 3.22.1)
project("dEdx" C)

# Data paths — overridden at runtime via JNI, empty strings are fine here
set(DEDX_DATA_PATH "")
set(DEDX_DATA_PATH_LOCAL "")
configure_file(
    libdedx/dedx_config.h.in
    libdedx/dedx_config.h
)

add_library(
    dEdx
    SHARED
    dEdx.c
    libdedx/dedx.c
    libdedx/dedx_atima.c
    libdedx/dedx_bethe.c
    libdedx/dedx_calculate_charge.c
    libdedx/dedx_file.c
    libdedx/dedx_file_access.c
    libdedx/dedx_mpaul.c
    libdedx/dedx_mstar.c
    libdedx/dedx_periodic_table.c
    libdedx/dedx_spline.c
    libdedx/dedx_split.c
    libdedx/dedx_tools.c
    libdedx/dedx_validate.c
    libdedx/dedx_wrappers.c
    libdedx/tools/dedx_math.c
)

target_include_directories(dEdx PRIVATE . ${CMAKE_CURRENT_BINARY_DIR} ${CMAKE_CURRENT_BINARY_DIR}/libdedx)

find_library(log-lib log)
target_link_libraries(dEdx ${log-lib})
