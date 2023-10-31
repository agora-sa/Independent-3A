package io.agora.ainoise;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.trtc.audiocall.TRTCRawEnterActivity;

import io.agora.ainoise.activity.AgoraRawActivity;
import io.agora.ainoise.activity.FileRawActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.ains_for_trtc).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TRTCRawEnterActivity.class)));

        findViewById(R.id.ains_for_agora).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AgoraRawActivity.class)));

        findViewById(R.id.ains_for_source_file).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FileRawActivity.class)));
    }

}