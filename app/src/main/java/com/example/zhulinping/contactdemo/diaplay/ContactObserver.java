package com.example.zhulinping.contactdemo.diaplay;

import android.database.ContentObserver;
import android.os.Handler;

/**
 * Created by zhulinping on 2017/7/31.
 */

public class ContactObserver extends ContentObserver{
    private Handler mHandler;
    public ContactObserver(Handler handler) {
        super(handler);
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        mHandler.sendEmptyMessage(0);
    }
}
