//
// Created by luca on 03/03/24.
//
#include <jni.h>
#include <stdio.h>
#include <string>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <android/sharedmem.h>
#include <sys/mman.h>
#include <unistd.h>
#include <android/NeuralNetworks.h>
#include <android/NeuralNetworksTypes.h>

jstring stringToJstring2(JNIEnv* env, std::string str);

extern "C" jstring
Java_com_bluetooth_communicatorexample_nnapi_NNAPITest_getAvailableDevices(JNIEnv* env, jclass clazz){
    /*uint32_t n_device = 0;
    std::string ret;
    ANeuralNetworks_getDeviceCount(&n_device);
    for(int i=0; i < n_device; i++){
        ANeuralNetworksDevice * device;
        ANeuralNetworks_getDevice(i,&device);
        const char *name = nullptr;
        ANeuralNetworksDevice_getName(device, &name);
        int32_t version = 0;
        ANeuralNetworksDevice_getType(device, &version);
    }*/
    return stringToJstring2(env,"");
}


jstring stringToJstring2(JNIEnv* env, std::string str){
    const char* chars = str.data();
    return env->NewStringUTF(chars);
}