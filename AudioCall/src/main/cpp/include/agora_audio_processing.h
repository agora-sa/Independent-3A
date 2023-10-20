//
//  Agora Uplink Audio Processing Library
//
//  Copyright (c) 2023 Agora IO. All rights reserved.
//
//  This program is confidential and proprietary to Agora.io.
//  And may not be copied, reproduced, modified, disclosed to others, published
//  or used, in whole or in part, without the express prior written permission
//  of Agora.io.

#pragma once

#include "agora_uap_optional.h"
#include "agora_uap_frame.h"

#if defined(_WIN32)
#define AGORA_UAP_API __declspec(dllexport)
#include <windows.h>
#elif defined(__APPLE__) || defined(__ANDROID__)
#define AGORA_UAP_API __attribute__((visibility("default")))
#define WINAPI
#else
#define AGORA_UAP_API
#define WINAPI
#endif

#define AGORA_MAX_UUID_LENGTH 128

namespace AgoraUAP {

class AgoraAudioProcessingEventHandler;

/**
 * The AgoraAudioProcessing class, which is the basic interface that provides
 * access to Agora uplink audio processing algorithms.
 */
class AgoraAudioProcessing {
 public:
  /**
   * The UAP configuration.
   */
  struct UapConfig {
    /**
     * appId which you can get it from agora console.
     */
    const char* appId;
    /**
     * license for audio processing engine.
     */
    const char* license;
    /**
     * eventHandler.
     */
    AgoraAudioProcessingEventHandler* eventHandler;
    UapConfig(const char* appId, const char* license,
              AgoraAudioProcessingEventHandler* eventHandler)
        : appId(appId), license(license), eventHandler(eventHandler) {}
  };

  using LogOutputFunc = void (*)(const char* log);
  /**
   * The log output option.
   */
  struct LogOption {
    /**
     * Whether to enable log output.
     * - `true`: Enable log output.
     * - `false`: (Default) Disable log output.
     */
    bool enabled;
    /**
     * Whether to output verbose log.
     * - `true`: Output verbose log. Used for debug purpose.
     * - `false`: (Default) Don't output verbose log.
     */
    bool verboseMode;
    /**
     * The log output function pointer. Should be implemented
     * asynchronously in order not to block the calling thread.
     */
    LogOutputFunc func;

    LogOption() : enabled(false), verboseMode(false), func(nullptr) {}
    LogOption(bool enable, bool verbose, LogOutputFunc ptr)
        : enabled(enable), verboseMode(verbose), func(ptr) {}
  };

  /**
   * The PCM dump option.
   */
  struct DumpOption {
    /**
     * Whether to enable PCM dump.
     * - `true`: Enable PCM dump.
     * - `false`: (Default) Disable PCM dump. Must be called
     * to finish dumping otherwise PCM files may be corrupted.
     */
    bool enabled;
    /**
     * The directory to save dump files. Ensure that the
     * directory exists and is writable.
     */
    const char* path;

    DumpOption() : enabled(false), path(nullptr) {}
    DumpOption(bool enable, const char* dir) : enabled(enable), path(dir) {}
  };

  /**
   * The AEC linear filter length.
   */
  enum AecFilterLength {
    /**
     * kNormal: (Default) 48ms for normal environment.
     */
    kNormal,
    /**
     * kLong: 200ms for reverberant environment.
     */
    kLong,
    /**
     * kLongest: 480ms for highly reverberant environment.
     */
    kLongest
  };

  /**
   * The AEC configuration.
   */
  struct AecConfig {
    /**
     * Whether to enable AEC.
     * - `true`: Enable AEC.
     * - `false`: (Default) Disable AEC.
     */
    AgoraUAP::optional<bool> enabled;
    /**
     * The AEC linear filter length. See #AecFilterLength.
     */
    AgoraUAP::optional<AecFilterLength> filterLength;
  };

  /**
   * The ANS noise suppression mode.
   */
  enum AnsSuppressionMode {
    /**
     * kMild: (Default) Low suppression level with only statistical ns enabled.
     */
    kMild,
    /**
     * kModerate: Moderate suppression level with both statistical and ai ns enabled.
     */
    kModerate,
    /**
     * kAggressive: High suppression level with both statistical and ai ns enabled.
     */
    kAggressive
  };

  /**
   * The ANS configuration.
   */
  struct AnsConfig {
    /**
     * Whether to enable ANS.
     * - `true`: Enable ANS.
     * - `false`: (Default) Disable ANS.
     */
    AgoraUAP::optional<bool> enabled;
    /**
     * The ANS noise suppression mode. See #AnsSuppressionMode.
     */
    AgoraUAP::optional<AnsSuppressionMode> suppressionMode;
  };

  /**
   * The AGC configuration.
   */
  struct AgcConfig {
    /**
     * Whether to enable AGC.
     * - `true`: Enable AGC.
     * - `false`: (Default) Disable AGC.
     */
    AgoraUAP::optional<bool> enabled;
    /**
     * Whether to use analog AGC working mode.
     * - `true`: Use anglog mode. Analog AGC is only available for
     * platforms with an analog volume control on the capture device.
     * The SetStreamAnalogLevel and StreamAnalogLevel methods provide
     * coupling between OS analog volume control and AGC.
     * - `false`: (Default) Use digital mode.
     */
    AgoraUAP::optional<bool> useAnalogMode;
  };

 public:
  /**
   * Init the AgoraAudioProcessing object.
   *
   * @note Agora recommends calling this method after creating
   *  AgoraAudioProcessing object.
   *
   * @param config The uap config. See #UapConfig.
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int Init(const UapConfig& config) = 0;

  /**
   * Release the AgoraAudioProcessing object.
   */
  virtual void Release() = 0;

  /**
   * Set log output option.
   *
   * @note Agora recommends calling this method to enable
   * log output before creating AgoraAudioProcessing object.
   *
   * @param option The log output option. See #LogOption.
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  AGORA_UAP_API static int WINAPI EnableLogOutput(const LogOption option);

  /**
   * Set PCM dump option.
   *
   * @note If dump is needed, Agora recommends calling this
   * method to enable PCM dump before starting process capture
   * and reverse stream.
   *
   * @param option The PCM dump option. See #DumpOption.
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int EnableDataDump(const DumpOption option) = 0;

  /**
   * Set AEC configuration.
   *
   * @param config The AEC configuration. See #AecConfig.
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int SetAecConfiguration(const AecConfig config) = 0;

  /**
   * Set ANS configuration.
   *
   * @param config The ANS configuration. See #AnsConfig.
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int SetAnsConfiguration(const AnsConfig config) = 0;

  /**
   * Set AGC configuration.
   *
   * @param config The AGC configuration. See #AgcConfig.
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int SetAgcConfiguration(const AgcConfig config) = 0;

  /**
   * Set stream delay in ms.
   *
   * @note Must be called if and only if AEC is enabled, in way of
   * before ProcessStream() in each processing cycle.
   *
   * @param delay Approximate time in ms between ProcessReverseStream()
   * receiving a far-end frame and ProcessStream() receiving the near-end
   * frame containing the corresponding echo. The value range is [60, 500].
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int SetStreamDelayMs(int delay) = 0;

  /**
   * Set stream analog level.
   *
   * @note Must be called if and only if analog agc is enabled, in way of
   * before ProcessStream() in each processing cycle.
   *
   * @param level The current analog level from the audio HAL. The value
   * range is [0, 255].
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int SetStreamAnalogLevel(int level) = 0;

  /**
   * Get new stream analog level suggested by AGC.
   *
   * @note Should be called if and only if analog agc is enabled, in way of
   * after ProcessStream() in each processing cycle.
   *
   * @param level The new analog level suggested by AGC. Should be set to
   * audio HAL if not equal to the level set in SetStreamAnalogLevel. The
   * value range is [0, 255].
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int GetStreamAnalogLevel(int& level) = 0;

  /**
   * Set signal gain.
   *
   * @param gain The gain applied to audio frame. The value range
   * is [0, 400].
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int SetGain(int gain) = 0;

  /**
   * Set private parameter.
   *
   * @param key The parameter string to be set.
   * @param value The value of the corresponding parameter string.
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int SetParameter(const char* key, int value) = 0;

  /**
   * Processes a 10 ms |frame| of the near-end (or captured) audio.
   *
   * @param frame The near-end frame to be processed. See #AgoraUAP::AudioFrame.
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int ProcessStream(AgoraUAP::AgoraAudioFrame* frame) = 0;

  /**
   * Processes a 10 ms |frame| of the far-end (or to be rendered) audio.
   *
   * @param frame The far-end frame to be processed. See #AgoraUAP::AudioFrame.
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int ProcessReverseStream(AgoraUAP::AgoraAudioFrame* frame) = 0;

  /**
   * Reset internal states and restart audio processing modules.
   *
   * @note Should be called if and only if critical runtime errors reported.
   * Agora uplink audio processing library will try best to recover from
   * critical runtime errors with no other method calling needed.
   *
   * @return
   * - 0: Success.
   * - < 0: Failure.
   */
  virtual int Reset() = 0;

 protected:
  virtual ~AgoraAudioProcessing(){};
};

/**
 * The AgoraAudioProcessingEventHandler class.
 *
 * Agora uses this class to send callback event notifications to the app, and
 * the app inherits methods in this class to retrieve these event notifications.
 *
 * To ensure thread safety, the app must not call AgoraAudioProcessing APIs
 * directly in the callback thread. Dispatch time-consuming tasks to other
 * threads asynchronously instead.
 */
class AgoraAudioProcessingEventHandler {
 public:
  /**
   * The event code of Agora uplink audio processing library.
   *
   * @note Agora reports critical runtime events or errors happened during
   * audio processing. The app should handle these events properly.
   */
  enum AgoraAudioProcessingEventType {
    /**
     * kAecMalfunction: The AEC breaks down.
     *
     * @note The app should call AgoraAudioProcessing::Reset asynchronously
     * to handle this event. See #AgoraAudioProcessing::Reset.
     */
    kAecMalfunction = 0
  };

  virtual ~AgoraAudioProcessingEventHandler() {}

  /**
   * The event callback of the Agora uplink audio processing library.
   *
   * @param event The event code of Agora uplink audio processing library.
   * See #AgoraAudioProcessingEventType.
   */
  virtual void onEvent(AgoraAudioProcessingEventType event) = 0;

  /**
   * The error callback of the Agora uplink audio processing library.
   *
   * @param error The error code of Agora uplink audio processing library.
   * See #ErrorCode.
   */
  virtual void onError(int error) = 0;
};

}  // namespace AgoraUAP

/**
 * Create an AgoraAudioProcessing object.
 */
extern "C" AGORA_UAP_API AgoraUAP::AgoraAudioProcessing* CreateAgoraAudioProcessing();

/**
 * Get Agora device UUID.
 *
 * @note Used to get license from agora license server.
 *
 * @param appId Input appid, must be the same as set in Init().
 * @param uuid Output uuid.
 * @param uuidBufLen Output uuid buffer length.
 *
 * @return
 * - 0: success.
 * - < 0: Failure.
 */
extern "C" AGORA_UAP_API int GetAgoraDeviceUUID(const char* appId, char* uuid, int uuidBufLen);

/**
 * Set android dir to store data.
 *
 * @note only work for android and should be called before GetAgoraDeviceUUID and AgoraAudioProcessing::Init.
 *
 * @param dir normally the dir should be Context.getCacheDir().getAbsolutePath().
 *
 * @return
 *  * - 0: success.
 *  - < 0: Failure.
 */
extern "C" AGORA_UAP_API int SetAgoraAndroidDataDir(const char* dir);
