package io.agora.ainoise.activity;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.tencent.trtc.audiocall.util.AudioProcessLogic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.agora.ainoise.BaseActivity;
import io.agora.ainoise.R;

/**
 * 对文件裸数据进行降噪处理
 */
public class FileRawActivity extends BaseActivity {

    private static final String TAG = "FileRawActivity";
    String uplink = "uplink.wav";
    String downlink = "downlink.wav";

    private static final String ROOT_PATH = "/sdcard/Android/data/io.agora.ainoise/files/";

    private StringBuilder builder;
    private FileInputStream fileInputStream;
    private AudioProcessLogic audioProcessLogic;
    private int channelCount;
    private int sampleRate;
    private int bitDepth;
    private int perSample;
    private int dataFor10Ms;

    private AppCompatButton mProcessBtn;
    private AppCompatEditText mFileName;
    private AppCompatButton mFileNameBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_raw);
        requestPermissions();

        mProcessBtn = findViewById(R.id.start_audio_read);
        mFileName = findViewById(R.id.text_file_name);
        mFileNameBtn = findViewById(R.id.btn_file_name);
        progressBar = findViewById(R.id.progress_bar);

        mProcessBtn.setOnClickListener(view -> {
            // takeVolumeAlgorithm();
            startProcessAudio();
        });

        mFileNameBtn.setOnClickListener(v -> {
            initAudioInfo();
        });
    }

    /**
     * 初始化需要用到的重要的音频信息
     */
    private void initAudioInfo() {
        String filePath = ROOT_PATH + mFileName.getText();
        // 初始化独立3A处理模块
        audioProcessLogic = new AudioProcessLogic();
        audioProcessLogic.audioProcessInit(getString(R.string.agora_app_id), ROOT_PATH);

        try {
            fileInputStream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        getWavFileInfo(filePath);

        mProcessBtn.setEnabled(true);
    }

    /**
     * 测试音量算法
     */
    private void takeVolumeAlgorithm() {
        builder = new StringBuilder();
        for (double m = 0.0; m <= 100.0; m+=1.0) {
            volumePercent2Volume(m);
        }
        Log.d(TAG, "output is : " + builder.toString());
    }

    /**
     * 开始处理音频文件
     */
    private void startProcessAudio() {
        progressBar.setVisibility(View.VISIBLE);
        hintKeyBoards(progressBar);
        new Thread(() -> {
            try {
                byte[] buffer = new byte[dataFor10Ms];
                if (fileInputStream == null) {
                    Log.d(TAG, "请检查是否存在要处理的音频文件");
                    return;
                }
                while (fileInputStream.read(buffer) != -1) {
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    byteBuffer.put(buffer);
                    byteBuffer.flip();
                    audioProcessLogic.startAudioProcess(byteBuffer, sampleRate, channelCount, sampleRate/100);
                }
                fileInputStream.close();

                runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(mContext, "降噪处理完成", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void copyAssets(){
        try {
            InputStream uplinkInput = getAssets().open(uplink);
            FileOutputStream unlinkOutput = new FileOutputStream(new File(getExternalFilesDir(null)+File.separator+uplink));
            copyFile(uplinkInput,unlinkOutput);

            InputStream downlinkInput = getAssets().open(downlink);
            FileOutputStream downlinkOutput = new FileOutputStream(new File(getExternalFilesDir(null)+File.separator+downlink));
            copyFile(downlinkInput,downlinkOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void copyFile(InputStream fis, OutputStream fos){
        try {
            byte[] buffer = new byte[2048];
            int count = 0;
            while ((count = fis.read(buffer))>0){
                fos.write(buffer,0,count);
            }
            fos.flush();
            fos.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioProcessLogic.entConfigure();
    }

    /**
     * old = 0 => new = 0
     * old = 6 => new = 1
     * old = 15 => new = 1
     * old = 25 => new = 2
     * old = 38 => new = 4
     * old = 54 => new = 10
     * old = 73 => new = 27
     * old = 91 => new = 64
     * old = 100 => new = 100
     * @param volumePercent input
     * @return output
     */
    int volumePercent2Volume(double volumePercent) {
        double z = volumePercent / 2.38 + 58;
        int value = (int) (Math.pow(10, (z - 100) / 20) * 100);
        builder.append(volumePercent + "-->" + value + " , ");
        return value;
    }

    /**
     * 获取指定音频文件的采样率、声道数、位宽等信息
     * @param filePath 音频文件在sdcard上的路径
     */
    public void getWavFileInfo(String filePath) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            // 处理异常
            return;
        }
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);

            if (mime != null && mime.startsWith("audio/")) {
                // 找到音频轨道
                channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
                sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE, 48000);
                bitDepth = format.getInteger(MediaFormat.KEY_PCM_ENCODING, 2);
                perSample = format.getInteger("bits-per-sample", 16);

                dataFor10Ms = (int)(sampleRate * channelCount * bitDepth * 0.01);
                Log.d("INFO", channelCount + "," + sampleRate + "," + bitDepth + "," + perSample + " , " + dataFor10Ms);
            }
        }
    }

    public static void hintKeyBoards(View view) {
        InputMethodManager manager = ((InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE));
        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}
