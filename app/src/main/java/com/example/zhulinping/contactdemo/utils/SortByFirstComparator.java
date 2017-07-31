package com.example.zhulinping.contactdemo.utils;

import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;

import java.util.Comparator;

/**
 * Created by zhulinping on 2017/7/31.
 */

public class SortByFirstComparator implements Comparator<ContactInfo>{
    @Override
    public int compare(ContactInfo contactInfo, ContactInfo t1) {
        if(contactInfo.getFirstLetter().equals("#")){
            return 1;
        }
        if(t1.getFirstLetter().equals("#")){
            return -1;
        }
            return contactInfo.getFirstLetter().compareTo(t1.getFirstLetter());
    }
}
