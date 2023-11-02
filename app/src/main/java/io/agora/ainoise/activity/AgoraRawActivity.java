package io.agora.ainoise.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import io.agora.ainoise.utils.AudioProcessLogic;

import java.nio.ByteBuffer;

import io.agora.ainoise.AiNoiseApp;
import io.agora.ainoise.BaseActivity;
import io.agora.ainoise.R;
import io.agora.ainoise.utils.CommonUtil;
import io.agora.ainoise.utils.TokenUtils;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IAudioFrameObserver;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.audio.AudioParams;

/**
 * 集成声网音频裸数据，裸数据出来后通过jni调用独立库3A进行噪声消除，处理后的数据在给回声网SDK
 */
public class AgoraRawActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "RawAudioDataActivity";

    protected Handler handler;

    private AppCompatButton mGetUUID;
    private AppCompatButton mConfigure;
    private AppCompatButton mInit;
    private AppCompatButton mProcess;

    private EditText et_channel;
    private Button join;
    private RtcEngine engine;
    private int myUid;
    private boolean joined = false;
    private static final Integer SAMPLE_RATE = 16000;
    private static final Integer SAMPLE_NUM_OF_CHANNEL = 1;
    private static final Integer SAMPLES = 160;

    private AudioProcessLogic audioProcessLogic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_audio_data);

        mGetUUID = findViewById(R.id.getUUID);
        mConfigure = findViewById(R.id.configure);
        mInit = findViewById(R.id.init);
        mProcess = findViewById(R.id.process);

        join = findViewById(R.id.btn_join);
        et_channel = findViewById(R.id.et_channel);

        mGetUUID.setOnClickListener(this);
        mConfigure.setOnClickListener(this);
        mInit.setOnClickListener(this);
        mProcess.setOnClickListener(this);
        join.setOnClickListener(this);

        initEngine();

        // 初始化独立3A处理模块
        audioProcessLogic = new AudioProcessLogic();
        audioProcessLogic.audioProcessInit(getString(R.string.agora_app_id), "/sdcard/Android/data/io.agora.ainoise/files");
    }

    private void initEngine() {
        Context context = AgoraRawActivity.this;
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = context.getApplicationContext();
            config.mAppId = getString(R.string.agora_app_id);
            config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
            config.mEventHandler = iRtcEngineEventHandler;
            config.mAreaCode = ((AiNoiseApp)getApplication()).getGlobalSettings().getAreaCode();
            engine = RtcEngine.create(config);
            engine.setParameters("{"
                    + "\"rtc.report_app_scenario\":"
                    + "{"
                    + "\"appScenario\":" + 100 + ","
                    + "\"serviceType\":" + 11 + ","
                    + "\"appVersion\":\"" + RtcEngine.getSdkVersion() + "\""
                    + "}"
                    + "}");
            /* setting the local access point if the private cloud ip was set, otherwise the config will be invalid.*/
            engine.setLocalAccessPoint(((AiNoiseApp)getApplication()).getGlobalSettings().getPrivateCloudConfig());
            engine.registerAudioFrameObserver(iAudioFrameObserver);
            engine.setRecordingAudioFrameParameters(SAMPLE_RATE, SAMPLE_NUM_OF_CHANNEL, Constants.RAW_AUDIO_FRAME_OP_MODE_READ_WRITE, SAMPLES);
            engine.setPlaybackAudioFrameParameters(SAMPLE_RATE, SAMPLE_NUM_OF_CHANNEL, Constants.RAW_AUDIO_FRAME_OP_MODE_READ_WRITE, SAMPLES);
        }
        catch (Exception e) {
            e.printStackTrace();
            onBackPressed();
        }
    }

    // 单独测试代码------------------------------------------------------------start
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.getUUID:
                // do getUUID
                break;
            case R.id.configure:
                // do configure
                break;
            case R.id.init:
                // do init
                break;
            case R.id.process:
                // do process
                break;
            case R.id.btn_join:
                // join channel
                if (!joined) {
                    CommonUtil.hideInputBoard(AgoraRawActivity.this, et_channel);
                    // Check permission
                    requestPermissions();
                } else {
                    joined = false;
                    engine.leaveChannel();
                    join.setText("加入");
                }
                break;
        }
    }
    // 单独测试代码------------------------------------------------------------end

    @Override
    protected void onDestroy() {
        super.onDestroy();

        audioProcessLogic.entConfigure();
    }

    private final IRtcEngineEventHandler iRtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onError(int err) {
            Log.w(TAG, String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err)));
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            Log.i(TAG, String.format("local user %d leaveChannel!", myUid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid));
            myUid = uid;
            joined = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    join.setEnabled(true);
                    join.setText("leave");
                }
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            Log.i(TAG, "onUserJoined->" + uid);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason));
        }

        @Override
        public void onActiveSpeaker(int uid) {
            super.onActiveSpeaker(uid);
            Log.i(TAG, String.format("onActiveSpeaker:%d", uid));
        }
    };

    private final IAudioFrameObserver iAudioFrameObserver = new IAudioFrameObserver() {

        @Override
        public boolean onRecordAudioFrame(String channel, int audioFrameType, int samples, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer byteBuffer, long renderTimeMs, int bufferLength) {
            audioProcessLogic.startAudioProcess(byteBuffer, samplesPerSec, channels, samples);
            // Log.i(TAG, "new buffer length is : " + byteBuffer.remaining());
            return true;
        }


        @Override
        public boolean onPlaybackAudioFrame(String channel, int audioFrameType, int samples, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer byteBuffer, long renderTimeMs, int bufferLength) {
            return false;
        }

        @Override
        public boolean onMixedAudioFrame(String channel, int audioFrameType, int samples, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer byteBuffer, long renderTimeMs, int bufferLength) {
            return false;
        }

        @Override
        public boolean onEarMonitoringAudioFrame(int type, int samplesPerChannel, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer buffer, long renderTimeMs, int avsync_type) {
            return false;
        }

        @Override
        public boolean onPlaybackAudioFrameBeforeMixing(String channel, int uid, int audioFrameType, int samples, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer byteBuffer, long renderTimeMs, int bufferLength) {
            return false;
        }

        @Override
        public boolean onPublishAudioFrame(String channelId, int type, int samplesPerChannel, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer buffer, long renderTimeMs, int avsync_type) {
            return false;
        }

        @Override
        public int getObservedAudioFramePosition() {
            return 0;
        }

        @Override
        public AudioParams getRecordAudioParams() {
            return new AudioParams(16000, 1, Constants.RAW_AUDIO_FRAME_OP_MODE_READ_WRITE, 160);
        }

        @Override
        public AudioParams getPlaybackAudioParams() {
            return null;
        }

        @Override
        public AudioParams getMixedAudioParams() {
            return null;
        }

        @Override
        public AudioParams getEarMonitoringAudioParams() {
            return null;
        }

        @Override
        public AudioParams getPublishAudioParams() {
            return null;
        }

    };


    private void joinChannel(String channelId) {
        /**In the AINoise, the default is to enter as the anchor.*/
        engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        engine.setDefaultAudioRoutetoSpeakerphone(true);

        /**Please configure accessToken in the string_config file.
         * A temporary token generated in Console. A temporary token is valid for 24 hours. For details, see
         *      https://docs.agora.io/en/Agora%20Platform/token?platform=All%20Platforms#get-a-temporary-token
         * A token generated at the server. This applies to scenarios with high-security requirements. For details, see
         *      https://docs.agora.io/en/cloud-recording/token_server_java?platform=Java*/
        TokenUtils.gen(AgoraRawActivity.this, channelId, 0, ret -> {
            ChannelMediaOptions option = new ChannelMediaOptions();
            option.autoSubscribeAudio = true;
            option.autoSubscribeVideo = true;
            int res = engine.joinChannel(ret, channelId, 0, option);
            if (res != 0) {
                // Usually happens with invalid parameters
                // Error code description can be found at:
                // en: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
                // cn: https://docs.agora.io/cn/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
                Log.e(TAG, RtcEngine.getErrorDescription(Math.abs(res)));
                return;
            }
            // Prevent repeated entry
            join.setEnabled(false);
        });
    }
}
