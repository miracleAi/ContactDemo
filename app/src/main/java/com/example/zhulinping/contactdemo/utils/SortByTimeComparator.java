package com.example.zhulinping.contactdemo.utils;

import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;

import java.util.Comparator;

/**
 * Created by zhulinping on 2017/7/31.
 */

public class SortByTimeComparator implements Comparator<ContactInfo> {
    @Override
    public int compare(ContactInfo contactInfo, ContactInfo t1) {
        if (contactInfo.lastContactTime > t1.lastContactTime) {
            return -1;
        }
        return 1;
    }
}
