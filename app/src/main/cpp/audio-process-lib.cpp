#include <jni.h>
#include <string>
#import "audio_processing.h"

// 全局的APM Process对象
std::unique_ptr<AINoise::AudioProcessingDemo> globalApm = nullptr;

/**
 * 使用独立AI noise库处理音频裸数据
 */
extern "C" JNIEXPORT jint JNICALL
Java_io_agora_ainoise_utils_AudioProcessLogic_audioProcess(
        JNIEnv *env, jobject thiz, jobject buffer, jint sampleRate, jint channels, jint samplesPerChannel) {
//    const char *hello = "Hello from C++";
//    printf("%s", hello);
    if (globalApm) {
        return globalApm->ProcessAudioFrame(env->GetDirectBufferAddress(buffer), sampleRate, channels, samplesPerChannel);
    } else {
        return -1;
    }
}

extern "C" __attribute__((unused)) JNIEXPORT jint JNICALL
Java_io_agora_ainoise_utils_AudioProcessLogic_audioProcessForArray(
        JNIEnv *env, jobject thiz, jbyteArray byteArray, jint sampleRate, jint channels, jint samplesPerChannel) {
    jbyte* byteArrayElements = env->GetByteArrayElements(byteArray, nullptr);
    jsize length = env->GetArrayLength(byteArray); // 获取数组长度
    if (byteArrayElements != nullptr) {
        char* data = new char[length];
        // 或使用 memcpy 分配内存
        memcpy(data, byteArrayElements, length);
        int res = globalApm->ProcessAudioFrame(data, sampleRate, channels, samplesPerChannel);
        // 释放data分配的内存
        delete[] data;
        // 释放 byteArray, 使用 JNI_ABORT 来避免复制回原数组
        env->ReleaseByteArrayElements(byteArray, byteArrayElements, JNI_ABORT);
        return res;
    } else {
        return -1;
    }
}

/**
 * 获取设备的UUID，用于请求业务服务器生成license
 */
extern "C" JNIEXPORT jstring JNICALL
Java_io_agora_ainoise_utils_AudioProcessLogic_getUUID(JNIEnv *env, jobject thiz, jstring appId) {
    const char* appIdStr = env->GetStringUTFChars(appId, nullptr);
    if (appIdStr == nullptr) {
        // 处理异常情况，例如内存不足
        return nullptr;
    }

    std::string uuid = globalApm->GetUUID(appIdStr);
    // 释放获取的字符串
    env->ReleaseStringUTFChars(appId, appIdStr);
    // 直接返回Java字符串
    return env->NewStringUTF(uuid.c_str());
}

/**
 * 对AudioProcess模块进行初始化配置
 */
extern "C" JNIEXPORT jint JNICALL
Java_io_agora_ainoise_utils_AudioProcessLogic_configure(JNIEnv *env, jobject thiz, jstring appid,
                                                                 jstring license) {
    const char* appIdStr = env->GetStringUTFChars(appid, JNI_FALSE);
    const char* licenseStr = env->GetStringUTFChars(license, JNI_FALSE);

    if (appIdStr == nullptr || licenseStr == nullptr) {
        // 处理异常情况，例如内存不足
        if (appIdStr != nullptr) {
            env->ReleaseStringUTFChars(appid, appIdStr);
        }
        if (licenseStr != nullptr) {
            env->ReleaseStringUTFChars(license, licenseStr);
        }
        return -1; // 或者其他错误代码
    }
    int result = 0;
    if (globalApm) {
        result = globalApm->Configure(appIdStr, licenseStr);
    } else {
        printf("globalApm is null!");
    }

    // 释放获取的字符串
    env->ReleaseStringUTFChars(appid, appIdStr);
    env->ReleaseStringUTFChars(license, licenseStr);

    return result;
}

/**
 * 创建全局的AudioProcess实例
 */
extern "C" JNIEXPORT void JNICALL
Java_io_agora_ainoise_utils_AudioProcessLogic_init(JNIEnv *env, jobject obj, jstring dumpPath) {
    const char* dmpPath = env->GetStringUTFChars(dumpPath, nullptr);
    try {
        globalApm = std::make_unique<AINoise::AudioProcessingDemo>(dmpPath);
    } catch (const std::exception& e) {
        env->ReleaseStringUTFChars(dumpPath, dmpPath); // 释放资源
        // 可以抛出异常或返回错误码
        return;
    }
    env->ReleaseStringUTFChars(dumpPath, dmpPath); // 释放资源
}

/**
 * 释放资源
 */
extern "C" JNIEXPORT void JNICALL
Java_io_agora_ainoise_utils_AudioProcessLogic_unConfigure(JNIEnv *env, jobject obj) {
    if (globalApm) {
        globalApm->UnConfigure();
        globalApm = nullptr;
    }
}