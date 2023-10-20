package io.agora.ainoise;

import android.app.Application;

import io.agora.ainoise.utils.GlobalSettings;

public class AiNoiseApp extends Application {

    private GlobalSettings globalSettings;

    static {
        System.loadLibrary("native-lib");
    }

    public GlobalSettings getGlobalSettings() {
        if(globalSettings == null){
            globalSettings = new GlobalSettings();
        }
        return globalSettings;
    }
}
