package com.example.roren.auctioncast.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.utility_http_DBQuery;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

/**
 * AR_Advertisement activity - 로고를 인식하면 해당 로고 위에 광고 동영상이 노출되고, 동영상이 모두 재생되면
 * 보상을 지급하는 액티비티.
 */

public class activity_AR_advertisement extends UnityPlayerActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    /**
     * 액티비티가 시작되면 호출되는 콜백메소드.
     * 재생할 동영상의 url 을 unity 로 전달한다.
     */
    public void onStartAD(String string){

        String[] urlList = new String[1];

        urlList[0] = "http://mirrors.standaloneinstaller.com/video-sample/small.mp4";

        UnityPlayer.UnitySendMessage("videoQuad", "setVideoURL", "http://mirrors.standaloneinstaller.com/video-sample/small.mp4");

    }

    /**
     * 동영상이 재생완료 되면 호출되는 콜백메소드. 보물상자가 나타나는 애니메이션이 동작하도록 유니티로 메시지를 전송한다.
     */
    public void onCompleteAD(String string){

        UnityPlayer.UnitySendMessage("videoQuad", "treasure_box_open", "open");

    }

    /**
     * 뒤로가기 버튼을 누르면 호출되는 콜백메소드. 액티비티를 종료한다.
     */
    public void onBackPushed(String string){

        finish();

    }

    /**
     * 보물상자를 터치하면 보물상자가 열리는 애니메이션이 동작한다.
     * 사용자에게 캐시 100원을 지급하고 토스트메시지를 띄운다.
     */
    public void onOpenTreasure(String string){

        dialog_ad_rewarding dialog = new dialog_ad_rewarding(this);

        dialog.show();

    }
}
