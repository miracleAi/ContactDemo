package com.example.zhulinping.contactdemo.contactdata;

import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;

import java.util.List;

/**
 * Created by zhulinping on 2017/7/28.
 */

public interface IContactDataHelper {
    interface ContactLoadCallback{
        void onContactListLoaded(List<ContactInfo> list);
        void onContactListNotAvailable();
    }
    void getAllContactList(ContactLoadCallback callback,int days,int count);
}
