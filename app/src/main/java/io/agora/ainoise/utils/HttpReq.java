package io.agora.ainoise.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

import io.agora.ainoise.BuildConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpReq extends AsyncTask<Void, Void, String> {
    private static final String TAG = "HttpReq";

    private final String mUUID;
    private final String mAppId;
    private final LicenseCallBack mCallBack;

    public HttpReq(String uuid, String appId, LicenseCallBack callBack){
        mUUID = uuid;
        mAppId = appId;
        mCallBack = callBack;
    }

    @Override
    protected String doInBackground(Void... voids) {
        // 创建一个 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient();

        // 替换为您要发送请求的 URL
        String url = "https://api.agora.io/3a/v2/apps/c55045d149074edca8a0cd74405a9fcb/licenses";
        Log.d(TAG, "new uuid is : " + mUUID);
        // 创建 JSON 请求体
        MediaType mediaType = MediaType.parse("application/json");
        String jsonRequestBody = "{\"credential\": \"4a5fa2eeddd2b2c9c7c4199aad48205b9b0c9bb19eaefdea4996aa99f6dd9252\", \"custom\":\""  + mUUID + "\"}";
        RequestBody requestBody = RequestBody.create(jsonRequestBody, mediaType);
        Log.d(TAG, "requestBody is : " + requestBody.toString());

        // 创建 HTTP POST 请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type","application/json")
                .addHeader("Authorization",createAuthorizationHeader())
                .post(requestBody)
                .build();

        try {
            // 执行请求并获取响应
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                // 处理响应数据
                return Objects.requireNonNull(response.body()).string();
            } else {
                // 处理 HTTP 错误
                return "HTTP Error: " + response.code();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG, "result is : " + result);
        String cert = "";
        try {
            JSONObject obj = new JSONObject(result);
            cert = String.valueOf(obj.get("cert"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 在此处处理响应数据
        // result 包含了来自服务器的响应数据
        int res = mCallBack.initConfigure(mAppId, cert);
        Log.d(TAG, "onPostExecute res is : " + cert);
        if (res == 0) {
            Log.d(TAG, "take license success!");
            // AINoise(getExternalFilesDir(null)+File.separator+downlink,getExternalFilesDir(null)+File.separator+uplink,dumpPath);
        }
        Log.d(TAG, "RES IS : " + res);
    }

    /**
     * 创建 authorization header
     */
    private String createAuthorizationHeader() {
        String plainCredentials = BuildConfig.AGORA_CUSTOM_KEY + ":" + BuildConfig.AGORA_CUSTOM_SECRET;
        String base64Credentials = new String(Base64.getEncoder().encode(plainCredentials.getBytes()));
        // 创建 authorization header
        return "Basic " + base64Credentials;
    }

    public interface LicenseCallBack {
        int initConfigure(String appid, String license);
    }
}
