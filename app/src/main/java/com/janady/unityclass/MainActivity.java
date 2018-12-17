package com.janady.unityclass;

import android.os.Bundle;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;
import com.xuanma.log.Log;
import com.xuanma.module.LockingPlate;

public class MainActivity extends UnityPlayerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void check() {
        LockingPlate.getDefault().check();
    }
}
