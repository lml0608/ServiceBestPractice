package com.bignerdranch.android.servicebestpractice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private DownloadService.DownloadBinder mDownloadBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mStartDownload = (Button) findViewById(R.id.start_download);
        Button mPauseDownload = (Button) findViewById(R.id.start_download);
        Button mCancelDownload = (Button) findViewById(R.id.start_download);

        Intent intent = new Intent(this,DownloadService.class);
        //启动服务
        startService(intent);
        //绑定服务
        bindService(intent, connection, BIND_AUTO_CREATE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        mStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mDownloadBinder == null){
                    return;
                }
                String url = "http://uploadfiles.nowcoder.com/course%2Fjike%2Fandroid_ppt.zip";
                mDownloadBinder.startDownload(url);

            }

        });
        mPauseDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mDownloadBinder == null){
                    return;
                }
                mDownloadBinder.pauseDownload();

            }
        });
        mCancelDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mDownloadBinder == null){
                    return;
                }
                mDownloadBinder.cancelDownload();

            }

        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑服务
        unbindService(connection);
    }
}
