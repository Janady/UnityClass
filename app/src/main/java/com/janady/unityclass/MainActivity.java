package com.janady.unityclass;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;
import com.xuanma.core.FileChooseUtil;
import com.xuanma.log.Log;
import com.xuanma.module.LockingPlate;

public class MainActivity extends UnityPlayerActivity {
    private static final int REQUEST_CHOOSEFILE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case REQUEST_CHOOSEFILE:
                Uri uri=data.getData();
                String chooseFilePath = FileChooseUtil.getInstance(this).getChooseFileResultPath(uri);
                sendFileMessage(chooseFilePath);
        }
    }

    private void sendFileMessage(String chooseFilePath) {
        UnityPlayer.UnitySendMessage("InternalMsgManager", "chooseFile", chooseFilePath);
    }

    public void chooseFile() {
        chooseFile("*/*");
    }
    public void chooseVideoFile() {
        chooseFile("video/*");
    }
    public void chooseAudioFile() {
        chooseFile("audio/*");
    }
    public void chooseTextFile() {
        chooseFile("text/plain");
    }

    public void chooseImageFile() {
        chooseFile("image/*");
    }

    public void chooseFile(String type) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type);//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,REQUEST_CHOOSEFILE);
    }
    public void check() {
        LockingPlate.getDefault().check();
    }
}
