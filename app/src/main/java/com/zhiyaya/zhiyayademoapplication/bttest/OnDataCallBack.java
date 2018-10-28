package com.zhiyaya.zhiyayademoapplication.bttest;

/**
 * Created by yerunjie on 2018/9/18
 *
 * @author yerunjie
 */
public interface OnDataCallBack {
    void onDataReceive(TestBean testBean);

    void onConnectionStateChange(boolean isConnect);
}
