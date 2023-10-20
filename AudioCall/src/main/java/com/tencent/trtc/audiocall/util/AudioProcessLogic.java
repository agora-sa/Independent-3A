package com.tencent.trtc.audiocall.util;

import android.util.Log;
import com.tencent.trtc.audiocall.HttpReq;

import java.nio.ByteBuffer;

public class AudioProcessLogic {
    private static final String TAG = "audioProcessLogic";

    /**
     * 初始化音频处理模块
     * @param appId 对应的appid
     * @param dumpPath dump路径
     * @return 方法是否执行成功
     */
    public int audioProcessInit(String appId, String dumpPath) {
        init(dumpPath);
        int result = configure(appId, "");
        Log.d(TAG, "click configure result is : " + result);
        if (result == 0) {
            Log.d(TAG, "configure result is ： " + result);
        } else {
            new HttpReq(getUUID(appId), appId, this::configure).execute();
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
