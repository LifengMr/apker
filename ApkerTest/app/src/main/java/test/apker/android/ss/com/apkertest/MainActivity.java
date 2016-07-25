package test.apker.android.ss.com.apkertest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ss.android.apker.Apker;
import com.ss.android.apker.download.DownLoadManager;
import com.ss.android.apker.helper.ApkHelper;

public class MainActivity extends Activity implements View.OnClickListener {

    private DownloadButton mBtn0;
    private DownloadButton mBtn1;
    private DownloadButton mBtn2;
    private View mBtn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtn0 = (DownloadButton) findViewById(R.id.btn0);
        mBtn1 = (DownloadButton) findViewById(R.id.btn1);
        mBtn2 = (DownloadButton) findViewById(R.id.btn2);
        mBtn3 = findViewById(R.id.btn3);

        mBtn0.setOnClickListener(this);
        mBtn1.setOnClickListener(this);
        mBtn2.setOnClickListener(this);
        mBtn3.setOnClickListener(this);

        DownLoadManager.ins().addTaskListener("com.letv.sport.game.sdk", mBtn0);
        DownLoadManager.ins().addTaskListener("com.testapk", mBtn1);
        DownLoadManager.ins().addTaskListener("com.ss.android.ugc.live", mBtn2);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        if (mBtn0.getId() == view.getId()) {
            test0();
        } else if (mBtn1.getId() == view.getId()) {
            test1();
        } else if (mBtn2.getId() == view.getId()) {
            test2();
        } else if (mBtn3.getId() == view.getId()) {
            test3();
        }
    }

    private void test0() {
        if (Apker.ins().isPluginInstalled("com.letv.sport.game.sdk")) {
            Intent intent = ApkHelper.buildIntent(this, "com.letv.sport.game.sdk.activity.SportGameMainActivity");
            startActivity(intent);
        }
    }

    private void test1(){
        if (Apker.ins().isPluginInstalled("com.testapk")) {
            Intent intent = ApkHelper.buildIntent(this, "com.testapk.ApkMainActivity");
            startActivity(intent);
        }
    }

    private void test2() {
        if (Apker.ins().isPluginInstalled("com.ss.android.ugc.live")) {
            Intent intent = ApkHelper.buildIntent(this, "com.ss.android.ugc.live.splash.SplashActivity");
            startActivity(intent);
        }
    }

    private void test3() {
        TestActivity1.launch(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownLoadManager.ins().removeTaskListener("com.letv.sport.game.sdk");
        DownLoadManager.ins().removeTaskListener("com.testapk");
        DownLoadManager.ins().removeTaskListener("com.ss.android.ugc.live");
    }
}
