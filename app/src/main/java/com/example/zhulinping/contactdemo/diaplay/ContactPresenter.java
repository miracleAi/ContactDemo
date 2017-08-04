package com.example.zhulinping.contactdemo.diaplay;

import android.app.Activity;

import com.example.zhulinping.contactdemo.contactdata.IContactDataHelper;
import com.example.zhulinping.contactdemo.contactdata.ContactDataHelper;
import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;

import java.util.List;

import static android.os.AsyncTask.execute;

/**
 * Created by zhulinping on 2017/7/28.
 */

public class ContactPresenter implements ContactContact.Presenter,IContactDataHelper.ContactLoadCallback{
    public static  int RECENT_DAYS = 3;
    public static int RECENT_COUNT = 5;
    private ContactContact.View mContactView;
    private Activity mActivity;
    public ContactPresenter(Activity activity, ContactContact.View contactView){
        mActivity = activity;
        mContactView = contactView;
        mContactView.setPresenter(this);
    }
    @Override
    public void start() {
        //TODO:在这里获取最近联系人的时间和个数限制
        RECENT_DAYS = 3;
        RECENT_COUNT = 5;
        getContactList();
    }
    @Override
    public void getContactList() {
        execute(new Runnable() {
            @Override
            public void run() {
                ContactDataHelper.getInstance(mActivity).getAllContactList(ContactPresenter.this,RECENT_DAYS,RECENT_COUNT);
            }
        });
    }

    @Override
    public void onContactListLoaded(final List<ContactInfo> list) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mContactView.showContactList(list);
            }
        });
    }

    @Override
    public void onContactListNotAvailable() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mContactView.showErrorLayout();
            }
        });
    }
}
