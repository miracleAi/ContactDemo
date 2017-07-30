package com.example.zhulinping.contactdemo.diaplay;

import com.example.zhulinping.contactdemo.contactdata.IContactDataHelper;
import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhulinping on 2017/7/28.
 */

public class ContactPresenter implements ContactContact.Presenter,IContactDataHelper.ContactLoadCallback{
    private IContactDataHelper mDadaHelper;
    private ContactContact.View mContactView;
    public ContactPresenter(IContactDataHelper dataHelper, ContactContact.View contactView){
        mDadaHelper = dataHelper;
        mContactView = contactView;
        mContactView.setPresenter(this);
    }
    @Override
    public void start() {
        getContactList();
    }

    @Override
    public void getContactList() {
        mDadaHelper.getAllContactList(this);
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
