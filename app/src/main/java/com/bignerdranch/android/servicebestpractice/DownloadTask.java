package com.bignerdranch.android.servicebestpractice;

import android.animation.FloatArrayEvaluator;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Switch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by lfs-ios on 2017/3/31.
 */

//AsyncTask<String, Integer, Integer>
//String 执行时传入的字符
//第2个参数，表示下载进度
//第3个参数，表示已整型来反馈执行结果
public class DownloadTask extends AsyncTask<String, Integer, Integer> {

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;

    private DownloadListener mDownloadListener;

    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;


    public DownloadTask(DownloadListener listener) {

        this.mDownloadListener = listener;
    }




    @Override
    protected Integer doInBackground(String... params) {


        InputStream is = null;

        RandomAccessFile savedFile = null;

        File file = null;

        try {
            //记录下载的文件长度
            long downloadLength = 0;
            //下载的url
            String downloadUrl = params[0];
            //文件名
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            //文件存放路径
            String directory = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getPath();
            //创建文件
            file = new File(directory, fileName);

            if (file.exists()) {
                //如果文件已存在，读取已经下载的字节数
                downloadLength = file.length();
            }

            //获取待下载的文件总长度
            long contentLength = getContentLength(downloadUrl);

            if (contentLength == 0) {
                //说明文件有问题，下载失败
                return TYPE_FAILED;//1
            }else if (contentLength == downloadLength) {
                //说明下载已经完成
                return TYPE_SUCCESS;//0
            }

            OkHttpClient client = new OkHttpClient();
            //添加Header告诉从哪个字节开始下载，已经下载过的部分不需要再重新下载了
            Request request = new Request.Builder()
                    .addHeader("RANGE", "tytes=" + downloadLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();

            if (response != null) {

                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadLength);//跳过已下载的字节

                byte[] b = new byte[1024];
                int total = 0;
                int len;

                while ((len = is.read(b)) != -1) {

                    //不断的判断用户的操作，是否有暂停，取消的操作
                    if (isCanceled){
                        return TYPE_CANCELED;//3
                    }else if (isPaused) {
                        return TYPE_PAUSED;//2
                    } else {
                        //用户没有取消，暂停的操作，就计算当前进度
                        total += len;
                        savedFile.write(b, 0, len);

                        int progress = (int) ((total + downloadLength) * 100 / contentLength);

                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if (is != null){
                    is.close();
                }
                if (savedFile != null){
                    savedFile.close();
                }
                if (isCanceled && file != null){
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {

            mDownloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer) {

            case TYPE_SUCCESS:
                mDownloadListener.onSuccess();
                break;
            case TYPE_FAILED:
                mDownloadListener.onFailed();
                break;
            case TYPE_PAUSED:
                mDownloadListener.onPaused();
                break;
            case TYPE_CANCELED:
                mDownloadListener.onCanceled();
                break;
            default:
                break;
        }
    }

    public void pauseDownload() {
        isPaused = true;
    }
    public void cancelDownload() {
        isCanceled = true;
    }

    private long getContentLength(String downloadUrl) throws IOException {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();

        Response response = client.newCall(request).execute();

        if (response != null && response.isSuccessful()) {

            long contentLength = response.body().contentLength();

            response.close();

            return contentLength;
        }
        return 0;
    }
}
