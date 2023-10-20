#include <memory>
#include <fstream>

#include "agora_audio_processing.h"

namespace AINoise {

    class AudioProcessingDemo : public AgoraUAP::AgoraAudioProcessingEventHandler {
    public:
        AudioProcessingDemo(const char* dumpPath);
        ~AudioProcessingDemo();

        static std::string GetUUID(const char* appId);
        int Configure(const char* appId, const char* license);
        int ProcessAudioFrame(void* buffer, int sampleRate, int channels, int samplesPerChannel);
        void UnConfigure();


    private:
        void onEvent(AgoraAudioProcessingEventType event) override;
        void onError(int error) override;

    private:
        AgoraUAP::AgoraAudioProcessing* agora_audio_processing_ = nullptr;
        AgoraUAP::AgoraAudioFrame uplink_frame_;
    };

}  // namespace AINoise
