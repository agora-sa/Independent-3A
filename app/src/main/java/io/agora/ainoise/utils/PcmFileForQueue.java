package io.agora.ainoise.utils;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class PcmFileForQueue {

    private static final String TAG = "PcmFileForQueue";

    private String mPcmPath;
    private FileOutputStream fos = null;
    // 缓存音频裸数据的队列，dump pcm文件的时候防止直接写耗时
    private final Queue<byte[]> pcmDataQueue = new LinkedList<>();

    public void init(String pcmPath) {
        mPcmPath = pcmPath;
        // 初始化写pcm文件相关信息
        try {
            File pcmFile = new File(pcmPath);
            fos = new FileOutputStream(pcmFile, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将字节buffer添加到队列中
     *
     * @param buffer 数据buffer
     */
    public void addByteBufferToQueue(ByteBuffer buffer) {
        if (null != buffer){
            byte[] dest = new byte[buffer.remaining()];
            buffer.get(dest);
            pcmDataQueue.offer(dest);
        }
    }

    /**
     * 将字节数组添加到队列中
     *
     * @param byteArray 字节数组
     */
    public void addByteArrayToQueue(byte[] byteArray) {
        if (null != byteArray){
            pcmDataQueue.offer(byteArray);
        }
    }

    /**
     * 将队列中缓存的数据存成pcm文件
     */
    public void saveToPcmFile() {
        try {
            while (!pcmDataQueue.isEmpty()) {
                fos.write(pcmDataQueue.poll());
            }
            fos.close();
            Log.d(TAG, "All PCM data blocks saved to " + mPcmPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
