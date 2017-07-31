package com.example.zhulinping.contactdemo.diaplay;

import android.util.Log;

import com.example.zhulinping.contactdemo.contactdata.IContactDataHelper;
import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhulinping on 2017/7/28.
 */

public class ContactPresenter implements ContactContact.Presenter,IContactDataHelper.ContactLoadCallback{
    public static  int RECENT_DAYS = 3;
    public static int RECENT_COUNT = 5;
    private IContactDataHelper mDadaHelper;
    private ContactContact.View mContactView;
    public ContactPresenter(IContactDataHelper dataHelper, ContactContact.View contactView){
        mDadaHelper = dataHelper;
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
        mDadaHelper.getAllContactList(this,RECENT_DAYS,RECENT_COUNT);
    }

    @Override
    public void onContactListLoaded(List<ContactInfo> list) {
        mContactView.showContactList(list);
    }

    @Override
    public void onContactListNotAvailable() {
        mContactView.showErrorLayout();
    }
}
