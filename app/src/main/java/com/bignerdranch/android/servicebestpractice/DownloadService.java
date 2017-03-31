package com.bignerdranch.android.servicebestpractice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {

    private DownloadTask mDownloadTask;

    private String downloadUrl;

    public DownloadService() {
    }

    private  DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {

            mDownloadTask = null;

            stopForeground(true);

            getNotificationManager().notify(1, getNotification("Download Success",-1));

            Toast.makeText(getApplicationContext(), "Download Success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            mDownloadTask = null;

            stopForeground(true);

            getNotificationManager().notify(1, getNotification("Download Failed",-1));

            Toast.makeText(getApplicationContext(), "Download Failed", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onPaused() {

            mDownloadTask = null;

            Toast.makeText(getApplicationContext(), "Paused", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCanceled() {
            mDownloadTask = null;

            stopForeground(true);

            Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    private DownloadBinder mBinder = new DownloadBinder();
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    class DownloadBinder extends Binder {

        //下载
        public void startDownload(String url) {

            if (mDownloadTask == null) {

                downloadUrl = url;

                mDownloadTask = new DownloadTask(listener);
                //执行下载后台任务
                mDownloadTask.execute(downloadUrl);

                startForeground(1, getNotification("Doenloading...", 0));

                Toast.makeText(getApplicationContext(), "Doenloading...", Toast.LENGTH_SHORT).show();;


            }
        }

        //暂停
        public void pauseDownload() {
            if (mDownloadTask != null) {

                mDownloadTask.pauseDownload();
            }

        }


        public void cancelDownload() {

            if (mDownloadTask != null) {

                mDownloadTask.cancelDownload();
            }else {

                if (downloadUrl != null) {

                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));

                    String directory = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS).getPath();

                    File file = new File(directory, fileName);

                    if (file.exists()) {
                        file.delete();
                    }

                    getNotificationManager().cancel(1);

                    stopForeground(true);
                    Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_SHORT).show();;


                }
            }
        }
    }






    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder  builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress > 0) {
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }


        return builder.build();

    }


}
