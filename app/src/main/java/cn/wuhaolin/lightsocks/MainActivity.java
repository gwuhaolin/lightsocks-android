package cn.wuhaolin.lightsocks;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(this, LsVpnService.class);
            intent.setAction(LsVpnService.ACTION_START);
            startService(intent);
        }
    }


    public void onOpenBtnClick(View view) {
        // VpnService.prepare函数的目的，主要是用来检查当前系统中是不是已经存在一个VPN连接了，如果有了的话，是不是就是本程序创建的。
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            // 如果当前系统中没有VPN连接，或者存在的VPN连接不是本程序建立的，则VpnService.prepare函数会返回一个intent。
            startActivityForResult(intent, 0);
        } else {
            // 如果当前系统中有VPN连接，并且这个连接就是本程序建立的，则函数会返回null，就不需要用户再确认了。因为用户在本程序第一次建立VPN连接的时候已经确认过了，就不要再重复确认了，直接手动调用onActivityResult函数就行了。
            onActivityResult(0, Activity.RESULT_OK, null);
        }
    }

    public void onCloseBtnClick(View view) {
        //关闭vpn连接
        Intent intentStop = new Intent(this, LsVpnService.class);
        intentStop.setAction(LsVpnService.ACTION_STOP);
        startService(intentStop);
    }
}
