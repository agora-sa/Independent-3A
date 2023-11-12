package io.agora.ainoise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.agora.ainoise.activity.TRTCRawEnterActivity;

import io.agora.ainoise.activity.AgoraRawActivity;
import io.agora.ainoise.activity.FileRawActivity;

public class MainActivity extends AppCompatActivity {

    private String appId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appId = BuildConfig.AGORA_APP_ID;

        findViewById(R.id.ains_for_trtc).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TRTCRawEnterActivity.class)));

        findViewById(R.id.ains_for_agora).setOnClickListener(v -> {
            if ("".equals(appId)) {
                Toast.makeText(MainActivity.this, "请到gradle.properties中填写APPID等信息", Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(MainActivity.this, AgoraRawActivity.class));
        });

        findViewById(R.id.ains_for_source_file).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FileRawActivity.class)));
    }

}