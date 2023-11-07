package io.agora.ainoise.utils;

import android.util.Log;

import java.nio.ByteBuffer;

import io.agora.ainoise.BuildConfig;

public class AudioProcessLogic {
    private static final String TAG = "audioProcessLogic";

    /**
     * 初始化音频处理模块
     * @param dumpPath dump路径
     * @return 方法是否执行成功
     */
    public int audioProcessInit(String dumpPath) {
        init(dumpPath);
        int result = configure(BuildConfig.AGORA_APP_ID, "");
        Log.d(TAG, "click configure result is : " + result);
        if (result == 0) {
            Log.d(TAG, "configure result is ： " + result);
        } else {
            new HttpReq(getUUID(BuildConfig.AGORA_APP_ID), BuildConfig.AGORA_APP_ID, this::configure).execute();
        }

        return 0;
    }

    public int startAudioProcess(ByteBuffer buffer, int sampleRate, int channels, int samplesPerChannel) {
        return audioProcess(buffer, sampleRate, channels, samplesPerChannel);
    }

    public void entConfigure() {
        unConfigure();
    }

    private native int audioProcess(ByteBuffer buffer, int sampleRate, int channels, int samplesPerChannel);

    private native void init(String dumpPath);

    private native String getUUID(String appId);

    private native int configure(String appId, String license);

    private native void unConfigure();
}
