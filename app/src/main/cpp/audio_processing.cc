#include "audio_processing.h"

#define TAG "[TESTDEMO]"
#include <android/log.h>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "AudioProcessNative", __VA_ARGS__)

static void log(const char *msg) {
    LOGD("%s", msg);
}

std::string globalString; // 全局的string变量

namespace AINoise {

    AudioProcessingDemo::AudioProcessingDemo(const char* dumpPath) {
        globalString = dumpPath;
        LOGD("input dump path is : %s and dump_path_=%s", dumpPath, globalString.c_str());
    }

    AudioProcessingDemo::~AudioProcessingDemo() {
        if (agora_audio_processing_) {
            agora_audio_processing_->Release();
            agora_audio_processing_ = nullptr;
        }
    }

    /**
     * 获取设备的UUID
     * @return 返回获取到的设备的UUID
     */
    std::string AudioProcessingDemo::GetUUID(const char *appId) {
        char uuid[AGORA_MAX_UUID_LENGTH];
        int error = GetAgoraDeviceUUID(appId, uuid, AGORA_MAX_UUID_LENGTH);
        if (error) {
            log(uuid);
            printf("%s%s(%d): error %d!\n", TAG, __FUNCTION__, __LINE__, error);
        }
        return uuid;
    }

    /**
     * 设置独立库输出日志的回调
     * @param logMessage 需要输出的日志信息
     */
    void CustomLogOutput(const char *logMessage) {
        // 这里可以将 logMessage 输出到您想要的地方，例如控制台
        std::string log_path =  globalString + "/ns_log.txt";
        // LOGD("native lib log path is %s ", log_path.c_str());
        std::ofstream logFile(log_path, std::ios::app);

        if (logFile.is_open()) {
            // 将日志消息写入文件
            logFile << logMessage << std::endl;
            logFile.close();
        } else {
            // std::cerr << "Unable to open log file." << std::endl;
        }
    }

    /**
     * 执行初始化配置
     * @param license
     * @param appId
     * @return 返回config的结果
     */
    int AudioProcessingDemo::Configure(const char *appId, const char *license) {
        AgoraUAP::AgoraAudioProcessing::LogOption logOption(true, true, CustomLogOutput);
        AgoraUAP::AgoraAudioProcessing::EnableLogOutput(logOption);

        // create agora audio process object
        agora_audio_processing_ = CreateAgoraAudioProcessing();
        if (!agora_audio_processing_) {
            printf("%s%s(%d): agora_audio_processing_ nullptr!\n", TAG, __FUNCTION__, __LINE__);
            return -1;
        }

        AgoraUAP::AgoraAudioProcessing::UapConfig config(appId, license, this);
        int error = agora_audio_processing_->Init(config);
        if (error) {
            log(error);
            printf("%s%s(%d): error %d!\n", TAG, __FUNCTION__, __LINE__, error);
            return error;
        }

        LOGD("input dump_path_=%s", globalString.c_str());
        // if (dump_path_ != nullptr && dump_path_[0] != '\0') {
            AgoraUAP::AgoraAudioProcessing::DumpOption dumpOption(true, globalString.c_str());
            error = agora_audio_processing_->EnableDataDump(dumpOption);
            if (error) {
                printf("%s%s(%d): error %d!\n", TAG, __FUNCTION__, __LINE__, error);
                return error;
            }
//        } else {
//            printf("dump_path_ is null!");
//        }

        // set ans config
        AgoraUAP::AgoraAudioProcessing::AnsConfig ansConfig;
        ansConfig.enabled = true;
        ansConfig.suppressionMode = AgoraUAP::AgoraAudioProcessing::kAggressive;
        error = agora_audio_processing_->SetAnsConfiguration(ansConfig);
        if (error) {
            printf("%s%s(%d): error %d!\n", TAG, __FUNCTION__, __LINE__, error);
            return error;
        }
        return 0;
    }

    /**
     * 释放初始化信息资源
     */
    void AudioProcessingDemo::UnConfigure() {
        if (agora_audio_processing_) {
            AgoraUAP::AgoraAudioProcessing::DumpOption dumpOption(false, nullptr);
            agora_audio_processing_->EnableDataDump(dumpOption);

            agora_audio_processing_->Release();
            agora_audio_processing_ = nullptr;

            AgoraUAP::AgoraAudioProcessing::LogOption logOption(false, false, nullptr);
            AgoraUAP::AgoraAudioProcessing::EnableLogOutput(logOption);

        }
    }

    /**
     * 处理一帧音频数据
     * @return 返回处理结果
     */
    int AudioProcessingDemo::ProcessAudioFrame(void* buffer, int sampleRate, int channels,
                                               int samplesPerChannel) {

        LOGD("sampleRate is %d , channels is %d , samplesPerChannel is %d , buffersize is %d \n", sampleRate, channels, samplesPerChannel,sizeof(buffer));
        uplink_frame_.buffer = buffer;
        uplink_frame_.sampleRate = sampleRate;
        uplink_frame_.channels = channels;
        uplink_frame_.samplesPerChannel = samplesPerChannel;

        int error = agora_audio_processing_->ProcessStream(&uplink_frame_);
        LOGD("ProcessAudioFrame error id %d", error);
        if (error) {
            printf("%s%s(%d): error %d!\n", TAG, __FUNCTION__, __LINE__, error);
            return error;
        }
        return 0;
    }

    void AudioProcessingDemo::onEvent(AgoraAudioProcessingEventType event) {
        // worker()->async_call( [this] {TODO: MUST dispatch to other thread!
        //    if (agora_audio_processing_) {
        //      agora_audio_processing_->Reset();
        //    }
        // });
    }

    void AudioProcessingDemo::onError(int error) {
        printf("error %d\n", error);
    }

}  // namespace AINoise
