# Independent-3A
本示例代码演示了如何将声网的独立3A库接入腾讯回调出来的裸数据和声网回调出来的裸数据
为了保证代码的实现效率，本工程使用C++的代码来封装调用独立3A的代码，所以Android端的java代码需要通过JNI的方式来调用C++的底层实现，工程采用CMake编译。
所以您的工程需要有CMake和JNI的环境

1、在 string-configs中替换您从声网控制台申请的APPID和app_certificate 即可



