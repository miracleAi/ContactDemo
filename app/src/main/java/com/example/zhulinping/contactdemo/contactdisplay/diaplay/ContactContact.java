package com.example.zhulinping.contactdemo.contactdisplay.diaplay;

import com.example.zhulinping.contactdemo.contactdisplay.base.BasePresenter;
import com.example.zhulinping.contactdemo.contactdisplay.base.BaseView;
import com.example.zhulinping.contactdemo.contactdisplay.contactdata.model.ContactInfo;

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
        public void getContactList();
    }
}
