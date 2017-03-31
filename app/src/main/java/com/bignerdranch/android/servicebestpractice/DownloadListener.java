package com.bignerdranch.android.servicebestpractice;


/**
 * Created by lfs-ios on 2017/3/31.
 *
 */
//回调接口，用于对下载过程中的各种状态进行监听和回调
public interface DownloadListener {

    //通知当前下载进度事件
    public void onProgress(int progress);
    //通知下载成功事件
    public void onSuccess();
    //通知下载失败事件
    public void onFailed();
    //通知下载暂停事件
    public void onPaused();
    //通知下载取消事件
    public void onCanceled();
}
