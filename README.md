# Independent-3A
# 功能简介
本示例代码演示声网独立3A库的使用，Demo功能如下：
    1、适配接入TRTC的音频裸数据，对TRTC的音频裸数据进行降噪处理后给出处理好的数据
    2、适配声网的音频裸数据，对声网的音频裸数据进行降噪处理并给出处理好的数据
    3、支持对WAV音频文件进行降噪处理
    4、支持音频dump

# 说明
为了保证代码的实现效率，本工程使用C++的代码来封装调用独立3A的代码，所以Android端代码需要通过JNI的方式来调用C++的底层实现
工程采用CMake编译，所以您的工程需要有CMake和JNI的环境

# 需要用到的三方库
    1、TRTC的SDK implementation "com.tencent.liteav:LiteAVSDK_TRTC:11.4.0.13217"
    2、声网的SDK implementation "io.agora.rtc:agora-special-full:4.1.1.14"

1、在gradle.properties中填写APPID等信息

# 使用说明
1、腾讯的裸数据降噪，我们可以多个用户加入到同一个房间，接收端听到的声音就是降噪后的声音
2、声网的裸数据降噪同上

3、
    1、如果您需要对您的音频文件进行降噪处理，需要先将您的音频文件copy到SDCard下面，可通过adb完成
    adb push /xxx/xxx.wav /sdcard/Android/data/io.agora.ainoise/files
    2、点击 音频文件降噪 -> 输入文件名 -> 确定 -> 开始处理音频文件
    3、处理后的文件在 /sdcard/Android/data/io.agora.ainoise/files 下面，通过adb导出
    adb pull /sdcard/Android/data/io.agora.ainoise/files /xxx/xxx
    其中 near_out_xxx是处理后的文件




