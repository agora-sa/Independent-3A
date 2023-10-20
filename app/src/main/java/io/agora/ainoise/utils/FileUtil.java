package io.agora.ainoise.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtil {

    public static void byteBufferToFile(ByteBuffer byteBuffer) {
        byteBuffer.flip(); // 切换到读模式
        String filePath = "/sdcard/Android/data/io.agora.ainoise/ns_output.wav";

        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath);
             FileChannel fileChannel = fileOutputStream.getChannel()) {

            // 将ByteBuffer的内容写入文件
            while (byteBuffer.hasRemaining()) {
                fileChannel.write(byteBuffer);
            }

            System.out.println("ByteBuffer数据已写入文件 " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
