LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := dEdx
LOCAL_SRC_FILES := dEdx.c libdedx/dedx.c libdedx/dedx_atima.c libdedx/dedx_bethe.c libdedx/dedx_calculate_charge.c libdedx/dedx_file.c libdedx/dedx_file_access.c libdedx/dedx_mpaul.c libdedx/dedx_mstar.c libdedx/dedx_periodic_table.c libdedx/dedx_spline.c libdedx/dedx_split.c libdedx/dedx_tools.c libdedx/dedx_validate.c libdedx/tools/dedx_math.c 
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog 

include $(BUILD_SHARED_LIBRARY)
