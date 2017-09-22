package com.example.zhulinping.contactdemo.contactdisplay.utils;

import com.example.zhulinping.contactdemo.contactdisplay.contactdata.model.ContactInfo;

import java.util.Comparator;

/**
 * Created by zhulinping on 2017/7/31.
 */

public class SortByFirstComparator implements Comparator<ContactInfo>{
    @Override
    public int compare(ContactInfo contactInfo, ContactInfo t1) {
        if(contactInfo.firstLetter.equals("#")){
            return 1;
        }
        if(t1.firstLetter.equals("#")){
            return -1;
        }
            return contactInfo.firstLetter.compareTo(t1.firstLetter);
    }
}
