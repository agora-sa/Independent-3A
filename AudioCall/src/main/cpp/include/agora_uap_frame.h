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

namespace AgoraUAP {

/**
 * AgoraAudioFrame struct.
 */
struct AgoraAudioFrame {
  /**
   * Audio frame types.
   */
  enum AudioFrameType {
    /**
     * 0: 16-bit PCM.
     */
    kPcm16 = 0,
  };
  /**
   * Bytes per sample.
   */
  enum BytesPerSample {
    /**
     * two bytes per sample
     */
    kTwoBytesPerSample = 2,
  };

  /**
   * The audio frame type. See #AudioFrameType.
   */
  AudioFrameType type;
  /**
   * The number of samples per channel in the audio frame, should
   * be 8000, 16000, 32000 or 48000.
   */
  int sampleRate;
  /**
   * The number of audio channels (interleaved if stereo).
   * - 1: Mono.
   * - 2: Stereo.
   */
  int channels;
  /**
   * The number of samples per channel in this frame, should
   * be (sampleRate / 100) corresponding to 10ms.
   */
  int samplesPerChannel;
  /**
   * The number of bytes per sample. See #BytesPerSample
   */
  BytesPerSample bytesPerSample;
  /**
   * The data buffer of the audio frame. When the audio frame uses a stereo
   * channel, the data buffer is interleaved.
   *
   * Buffer size in bytes = channels * samplesPerChannel * bytesPerSample.
   */
  void* buffer;

  AgoraAudioFrame()
      : type(kPcm16),
        sampleRate(0),
        channels(0),
        samplesPerChannel(0),
        bytesPerSample(kTwoBytesPerSample),
        buffer(nullptr) {}
};

}  // namespace AgoraUAP
