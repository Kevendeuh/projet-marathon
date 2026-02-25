#!/bin/bash
set -e
export ANDROID_NDK=/home/antoine/android-ndk-r26b
export TVM_SOURCE_DIR=/home/antoine/mlc-llm/3rdparty/tvm
cd /mnt/c/Users/Antoine/Documents/GitHub/projet-marathon/mlc4j
mkdir -p build/lib
cd build
echo "set(TVM_SOURCE_DIR \"/home/antoine/mlc-llm/3rdparty/tvm\")" > config.cmake

cmake ../ \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK/build/cmake/android.toolchain.cmake \
    -DCMAKE_INSTALL_PREFIX=. \
    -DCMAKE_CXX_FLAGS="-O3" \
    -DANDROID_ABI=arm64-v8a \
    -DANDROID_NATIVE_API_LEVEL=android-24 \
    -DANDROID_PLATFORM=android-24 \
    -DCMAKE_FIND_ROOT_PATH_MODE_PACKAGE=ON \
    -DANDROID_STL=c++_static \
    -DUSE_HEXAGON_SDK=OFF \
    -DMLC_LLM_INSTALL_STATIC_LIB=ON \
    -DCMAKE_SKIP_INSTALL_ALL_DEPENDENCY=ON \
    -DUSE_OPENCL=ON \
    -DUSE_OPENCL_ENABLE_HOST_PTR=ON \
    -DUSE_VULKAN=ON \
    -DUSE_CUSTOM_LOGGING=ON \
    -DTVM_FFI_USE_LIBBACKTRACE=OFF \
    -DTVM_FFI_BACKTRACE_ON_SEGFAULT=OFF

cmake --build . --target tvm4j_runtime_packed --config release -j8
cmake --build . --target install --config release -j8

cd ~
./patch_model_lib.sh
