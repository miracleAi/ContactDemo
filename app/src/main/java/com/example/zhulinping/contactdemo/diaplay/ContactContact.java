package com.example.zhulinping.contactdemo.diaplay;

import com.example.zhulinping.contactdemo.base.BasePresenter;
import com.example.zhulinping.contactdemo.base.BaseView;
import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhulinping on 2017/7/28.
 */

public class ContactContact {
    interface View extends BaseView<Presenter>{
        void showContactList(List<ContactInfo> list);
        void showErrorLayout();
    }
    interface Presenter extends BasePresenter{
        void getContactList();
    }
}
