package io.agora.ainoise.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.basic.TRTCBaseActivity;
import com.tencent.liteav.TXLiteAVCode;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import io.agora.ainoise.R;
import io.agora.ainoise.utils.AudioProcessLogic;
import io.agora.ainoise.utils.PcmFileForQueue;
import com.tencent.trtc.debug.Constant;
import com.tencent.trtc.debug.GenerateTestUserSig;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * TRTC 语音通话的主页面
 * <p>
 * 包含如下简单功能：
 * - 进入语音通话房间{@link TRTCRawActivity#enterRoom()}
 * - 退出语音通话房间{@link TRTCRawActivity#exitRoom()}
 * - 关闭/打开麦克风{@link TRTCRawActivity#muteAudio()}
 * - 免提(听筒/扬声器切换){@link TRTCRawActivity#audioRoute()}
 * <p>
 * - 详见接入文档{https://cloud.tencent.com/document/product/647/42047}
 * <p>
 * Audio Call
 * <p>
 * Features:
 * - Enter an audio call room: {@link TRTCRawActivity#enterRoom()}
 * - Exit an audio call room: {@link TRTCRawActivity#exitRoom()}
 * - Turn on/off the mic: {@link TRTCRawActivity#muteAudio()}
 * - Switch between the speaker (hands-free mode) and receiver: {@link TRTCRawActivity#audioRoute()}
 * <p>
 * - For more information, please see the integration document {https://cloud.tencent.com/document/product/647/42047}.
 */
public class TRTCRawActivity extends TRTCBaseActivity implements View.OnClickListener {

    private static final String TAG = "AudioCallingActivity";
    private static final int MAX_USER_COUNT = 6;
    private static FileOutputStream directSavePcmFos = null;

    private TextView mTextTitle;
    private ImageView mImageBack;
    private List<LinearLayout> mListUserView;
    private List<TextView> mListUserIdView;
    private List<TextView> mListVoiceInfo;
    private List<TextView> mListNetWorkInfo;
    private Button mButtonMuteAudio;
    private Button mButtonAudioRoute;
    private Button mButtonHangUp;

    private TRTCCloud mTRTCCloud;
    private String mRoomId;
    private String mUserId;
    private boolean mAudioRouteFlag = true;
    private List<String> mRemoteUserList = new ArrayList<>();

    private AudioProcessLogic audioProcessLogic;
    private PcmFileForQueue pcmFileForQueue;
    private int tenMsData;

    @SuppressLint("SdCardPath")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trtc_raw);
        getSupportActionBar().hide();
        handleIntent();

        if (checkPermission()) {
            initView();
            enterRoom();
        }

        // 初始化独立3A处理模块
        audioProcessLogic = new AudioProcessLogic();
        audioProcessLogic.audioProcessInit("/sdcard/Android/data/io.agora.ainoise/files");

        // 初始化写pcm文件相关信息
        pcmFileForQueue = new PcmFileForQueue();
        pcmFileForQueue.init("/sdcard/Android/data/io.agora.ainoise/files/tx_output.pcm");

//        try {
//            File directSavePcmFile = new File("/sdcard/Android/data/io.agora.ainoise/files/output_direct.pcm");
//            directSavePcmFos = new FileOutputStream(directSavePcmFile, true); // 使用追加模式
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (null != intent) {
            if (intent.getStringExtra(Constant.USER_ID) != null) {
                mUserId = intent.getStringExtra(Constant.USER_ID);
            }
            if (intent.getStringExtra(Constant.ROOM_ID) != null) {
                mRoomId = intent.getStringExtra(Constant.ROOM_ID);
            }
        }
    }

    protected void initView() {
        mTextTitle = findViewById(R.id.tv_room_number);
        mImageBack = findViewById(R.id.iv_back);
        mButtonMuteAudio = findViewById(R.id.btn_mute_audio);
        mButtonAudioRoute = findViewById(R.id.btn_audio_route);
        mButtonHangUp = findViewById(R.id.btn_hangup);

        mListUserView = new ArrayList<>();
        mListUserIdView = new ArrayList<>();
        mListVoiceInfo = new ArrayList<>();
        mListNetWorkInfo = new ArrayList<>();

        mListUserView.add((LinearLayout) findViewById(R.id.ll_user1));
        mListUserView.add((LinearLayout) findViewById(R.id.ll_user2));
        mListUserView.add((LinearLayout) findViewById(R.id.ll_user3));
        mListUserView.add((LinearLayout) findViewById(R.id.ll_user4));
        mListUserView.add((LinearLayout) findViewById(R.id.ll_user5));
        mListUserView.add((LinearLayout) findViewById(R.id.ll_user6));

        mListUserIdView.add((TextView) findViewById(R.id.tv_user1));
        mListUserIdView.add((TextView) findViewById(R.id.tv_user2));
        mListUserIdView.add((TextView) findViewById(R.id.tv_user3));
        mListUserIdView.add((TextView) findViewById(R.id.tv_user4));
        mListUserIdView.add((TextView) findViewById(R.id.tv_user5));
        mListUserIdView.add((TextView) findViewById(R.id.tv_user6));

        mListVoiceInfo.add((TextView) findViewById(R.id.tv_voice1));
        mListVoiceInfo.add((TextView) findViewById(R.id.tv_voice2));
        mListVoiceInfo.add((TextView) findViewById(R.id.tv_voice3));
        mListVoiceInfo.add((TextView) findViewById(R.id.tv_voice4));
        mListVoiceInfo.add((TextView) findViewById(R.id.tv_voice5));
        mListVoiceInfo.add((TextView) findViewById(R.id.tv_voice6));

        mListNetWorkInfo.add((TextView) findViewById(R.id.tv_network1));
        mListNetWorkInfo.add((TextView) findViewById(R.id.tv_network2));
        mListNetWorkInfo.add((TextView) findViewById(R.id.tv_network3));
        mListNetWorkInfo.add((TextView) findViewById(R.id.tv_network4));
        mListNetWorkInfo.add((TextView) findViewById(R.id.tv_network5));
        mListNetWorkInfo.add((TextView) findViewById(R.id.tv_network6));

        mButtonAudioRoute.setSelected(mAudioRouteFlag);
        if (!TextUtils.isEmpty(mRoomId)) {
            mTextTitle.setText(getString(R.string.audiocall_roomid) + mRoomId);
        }
        mImageBack.setOnClickListener(this);
        mButtonMuteAudio.setOnClickListener(this);
        mButtonAudioRoute.setOnClickListener(this);
        mButtonHangUp.setOnClickListener(this);

        refreshUserView();
    }

    @Override
    protected void onPermissionGranted() {
        initView();
        enterRoom();
    }

    protected void enterRoom() {
        mTRTCCloud = TRTCCloud.sharedInstance(getApplicationContext());
        mTRTCCloud.setListener(new TRTCCloudImplListener(TRTCRawActivity.this));

        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = GenerateTestUserSig.SDKAPPID;
        trtcParams.userId = mUserId;
        trtcParams.roomId = Integer.parseInt(mRoomId);
        trtcParams.userSig = GenerateTestUserSig.genTestUserSig(trtcParams.userId);


        TRTCCloudDef.TRTCAudioFrameCallbackFormat format = new TRTCCloudDef.TRTCAudioFrameCallbackFormat();
        format.channel = 1;
        format.mode = TRTCCloudDef.TRTC_AUDIO_FRAME_OPERATION_MODE_READWRITE;
        format.sampleRate = 16000;
        format.samplesPerCall = format.sampleRate / 100;
        mTRTCCloud.setLocalProcessedAudioFrameCallbackFormat(format);

        mTRTCCloud.enableAudioVolumeEvaluation(2000, true);
        mTRTCCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH);
        mTRTCCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_AUDIOCALL);

        mTRTCCloud.setAudioFrameListener(new TRTCCloudListener.TRTCAudioFrameListener() {
            @Override
            public void onCapturedAudioFrame(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame) {
                // 1、保存原始数据
                // appendToPcmFileForByteArray(trtcAudioFrame.data);
                //Log.d(TAG, "length = " + trtcAudioFrame.data.length + trtcAudioFrame.channel + " , " + trtcAudioFrame.sampleRate + " , " + trtcAudioFrame.extraData + " , " + trtcAudioFrame.timestamp);

                // 2、保存处理后的byte[]
//                ByteBuffer byteBuffer = ByteBuffer.allocate(trtcAudioFrame.data.length);
//                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
//                byteBuffer.put(trtcAudioFrame.data);
//                byteBuffer.flip();
//                byte[] dest = new byte[trtcAudioFrame.data.length];
//                byteBuffer.get(dest);
//                appendToPcmFileForByteArray(dest, "/sdcard/Android/data/io.agora.ainoise/files/output_old.pcm");

                // 3、保存处理后的ByteBuffer
//                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(trtcAudioFrame.data.length);
//                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
//                byteBuffer.put(trtcAudioFrame.data);
//                byteBuffer.flip();
//                if (!isStop) {
//                    pcmDataQueue.offer(byteBuffer);
//                }
//                audioProcess(byteBuffer, trtcAudioFrame.sampleRate, trtcAudioFrame.channel, trtcAudioFrame.sampleRate/100);


                // 以下的代码是将回调回来的裸数据拆成10ms的小数据块,并优化了内存占用
                audioProcess(trtcAudioFrame);

                // 添加byte[]到队列中，用户点击挂断的时候会将队列里面的数据持久化到文件
                // pcmFileForQueue.addByteArrayToQueue(trtcAudioFrame.data);
            }

            @Override
            public void onLocalProcessedAudioFrame(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame) {

            }

            @Override
            public void onRemoteUserAudioFrame(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame, String s) {

            }

            @Override
            public void onMixedPlayAudioFrame(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame) {

            }

            @Override
            public void onMixedAllAudioFrame(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame) {

            }

            @Override
            public void onVoiceEarMonitorAudioFrame(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame) {

            }
        });
    }

    /**
     * 处理腾讯裸数据返回的byte[]
     *
     * @param trtcAudioFrame trtc返回的裸数据实体类
     */
    private void audioProcess(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame) {
        // Log.d(TAG, "start...");
        // 每一帧过来的数据大小
        int dataLength = trtcAudioFrame.data.length;
        // number of bytes per sample
        int numBytesPerSample = 16 / 8;
        // 10ms的数据大小
        double unitData = trtcAudioFrame.sampleRate * trtcAudioFrame.channel * numBytesPerSample * 0.01;
        // 需要循环的次数
        int count = (int) (dataLength / unitData);
        int remainder = (int) (dataLength % unitData);
        count += remainder > 0 ? 1 : 0;

        // 创建一个存放处理后数据的最终buffer
        ByteBuffer mergedBuffer = ByteBuffer.allocate(dataLength);
        byte[] chunkData = new byte[(int)unitData];
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(chunkData.length);
        newBuffer.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < count; i++) {
            System.arraycopy(trtcAudioFrame.data, i * (int)unitData, chunkData, 0, chunkData.length);
            newBuffer.put(chunkData);
            newBuffer.flip();
            audioProcessLogic.startAudioProcess(newBuffer, trtcAudioFrame.sampleRate, trtcAudioFrame.channel, trtcAudioFrame.sampleRate / 100);
            mergedBuffer.put(newBuffer);
            newBuffer.clear();
        }
        mergedBuffer.flip();
        System.arraycopy(mergedBuffer.array(), 0, trtcAudioFrame.data, 0, mergedBuffer.array().length);
        mergedBuffer.clear();
        // Log.d(TAG, "end...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitRoom();

        audioProcessLogic.entConfigure();
    }

    private void exitRoom() {
        if (mTRTCCloud != null) {
            mTRTCCloud.stopLocalAudio();
            mTRTCCloud.exitRoom();
            mTRTCCloud.setListener(null);
        }
        mTRTCCloud = null;
        TRTCCloud.destroySharedInstance();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.btn_mute_audio) {
            muteAudio();
        } else if (id == R.id.btn_audio_route) {
            audioRoute();
        } else if (id == R.id.btn_hangup) {
            mTRTCCloud.setAudioFrameListener(null);
            pcmFileForQueue.saveToPcmFile();
            hangUp();
        }
    }

    private void hangUp() {
        finish();
    }

    private void audioRoute() {
        mAudioRouteFlag = !mAudioRouteFlag;
        mButtonAudioRoute.setSelected(mAudioRouteFlag);
        if (mAudioRouteFlag) {
            mTRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER);
            mButtonAudioRoute.setText(getString(R.string.audiocall_use_receiver));
        } else {
            mTRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_EARPIECE);
            mButtonAudioRoute.setText(getString(R.string.audiocall_use_speaker));
        }
    }


    private void muteAudio() {
        boolean isSelected = mButtonMuteAudio.isSelected();
        if (!isSelected) {
            mTRTCCloud.muteLocalAudio(true);
            mButtonMuteAudio.setText(getString(R.string.audiocall_stop_mute_audio));
        } else {
            mTRTCCloud.muteLocalAudio(false);
            mButtonMuteAudio.setText(getString(R.string.audiocall_mute_audio));
        }
        mButtonMuteAudio.setSelected(!isSelected);
    }

    private class TRTCCloudImplListener extends TRTCCloudListener {

        private WeakReference<TRTCRawActivity> mContext;

        public TRTCCloudImplListener(TRTCRawActivity activity) {
            super();
            mContext = new WeakReference<>(activity);
        }

        @Override
        public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> arrayList, int i) {
            Log.d(TAG, "onUserVoiceVolume:i = " + i);
            if (arrayList != null && arrayList.size() > 0) {
                Log.d(TAG, "onUserVoiceVolume:arrayList.size = " + arrayList.size());
                int index = 0;
                for (TRTCCloudDef.TRTCVolumeInfo info : arrayList) {
                    if (info != null && !mUserId.equals(info.userId) && index < MAX_USER_COUNT) {
                        Log.d(TAG, "onUserVoiceVolume:userId = " + info.userId + ", volume = " + info.volume);
                        mListVoiceInfo.get(index).setVisibility(View.VISIBLE);
                        mListVoiceInfo.get(index).setText(info.userId + ":" + info.volume);
                        index++;
                    }
                }
                for (int j = index; j < MAX_USER_COUNT; j++) {
                    mListVoiceInfo.get(j).setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onNetworkQuality(TRTCCloudDef.TRTCQuality trtcQuality,
                                     ArrayList<TRTCCloudDef.TRTCQuality> arrayList) {
            Log.d(TAG, "onNetworkQuality");
            if (arrayList != null && arrayList.size() > 0) {
                int index = 0;
                for (TRTCCloudDef.TRTCQuality info : arrayList) {
                    if (info != null && index < MAX_USER_COUNT) {
                        Log.d(TAG, "onNetworkQuality:userId = " + info.userId + ", quality = " + info.quality);
                        mListNetWorkInfo.get(index).setText(info.userId + ":" + NetQuality.getMsg(info.quality));
                        mListNetWorkInfo.get(index).setVisibility(View.VISIBLE);
                        index++;
                    }
                }
                for (int j = index; j < MAX_USER_COUNT; j++) {
                    mListNetWorkInfo.get(j).setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onRemoteUserEnterRoom(String s) {
            Log.d(TAG, "onRemoteUserEnterRoom userId " + s);
            mRemoteUserList.add(s);
            refreshUserView();
        }

        @Override
        public void onRemoteUserLeaveRoom(String s, int i) {
            Log.d(TAG, "onRemoteUserLeaveRoom userId " + s);
            mRemoteUserList.remove(s);
            refreshUserView();
        }

        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            Log.d(TAG, "sdk callback onError");
            TRTCRawActivity activity = mContext.get();
            if (activity != null) {
                Toast.makeText(activity, "onError: " + errMsg + "[" + errCode + "]", Toast.LENGTH_SHORT).show();
                if (errCode == TXLiteAVCode.ERR_ROOM_ENTER_FAIL) {
                    activity.exitRoom();
                }
            }
        }
    }

    private void refreshUserView() {
        if (mRemoteUserList != null) {
            for (int i = 0; i < MAX_USER_COUNT; i++) {
                if (i < mRemoteUserList.size()) {
                    mListUserView.get(i).setVisibility(View.VISIBLE);
                    mListUserIdView.get(i).setText(mRemoteUserList.get(i));
                } else {
                    mListUserView.get(i).setVisibility(View.GONE);
                }
            }
        }
    }

    public enum NetQuality {
        UNKNOW(0, "未定义"),
        EXCELLENT(1, "最好"),
        GOOD(2, "好"),
        POOR(3, "一般"),
        BAD(4, "差"),
        VBAD(5, "很差"),
        DOWN(6, "不可用");

        private int code;
        private String msg;

        NetQuality(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public static String getMsg(int code) {
            for (NetQuality item : NetQuality.values()) {
                if (item.code == code) {
                    return item.msg;
                }
            }
            return "未定义";
        }
    }

    public static void appendToPcmFile(ByteBuffer pcmData) {
        try {
            byte[] byteArray = new byte[pcmData.remaining()];
            pcmData.get(byteArray);
            directSavePcmFos.write(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendToPcmFileForByteArray(byte[] pcmData) {
        try {
            directSavePcmFos.write(pcmData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
