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

# 独立3A集成注意事项
1、设置音频的采集参数，音频的处理模式必须是"读写模式"。即：RAW_AUDIO_FRAME_OP_MODE_READ_WRITE
2、设置音频的采集参数，采样率和采样点数的关系必须是100倍。比如：采样率是16000，采样点数是160
    即：setRecordingAudioFrameParameters(16000, 1, Constants.RAW_AUDIO_FRAME_OP_MODE_READ_WRITE, 160);
3、独立3A一次处理10ms的数据，这个需要注意下
    给的多了：多余的数据会被丢弃，不处理。给的少了：数据直接会被丢弃，不处理。
4、接入其他厂商的裸数据需要注意
    其中 网易和声网的一次都会给出10ms的数据，腾讯每次给出的数据是20ms。
5、需要注意给出裸数据的数据格式
    对于Android和IOS，声网和网易给出的都是ByteBuffer，腾讯给出的是byte[]
6、Android和IOS的集成方式，为了独立3A执行的效率，统一使用C++的底层实现
    Android采用JNI的方式; IOS端OC可以直接调用C++，如果是swift需要使用OC做bridge。
7、使用独立3A需要使用到license，获取license需要使用到UUID
    Android上每次获取到的UUID都会变化; IOS不会
8、获取到的license，sdk是会缓存下来的，每次init的时候传"",sdk会取缓存，如果校验失败，
    客户端需要先获取UUID，然后在获取license，然后在重新init。
    如果init的时候传具体的值，sdk会更新缓存的license。




