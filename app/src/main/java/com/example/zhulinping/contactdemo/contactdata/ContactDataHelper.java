package com.example.zhulinping.contactdemo.contactdata;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.zhulinping.contactdemo.contactdata.model.ContactDbInfo;
import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;
import com.example.zhulinping.contactdemo.diaplay.ContactContact;
import com.example.zhulinping.contactdemo.utils.CnToSpell;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhulinping on 2017/7/28.
 */

public class ContactDataHelper implements IContactDataHelper {
    private Context mContext;
    private ArrayList<String> mFavouroteIds = new ArrayList<>();

    public ContactDataHelper(Context context) {
        mContext = context;
    }

    //favourote contact
    public ArrayList<ContactInfo> getFavouriteContact() {
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                ContactsContract.Contacts.STARRED + "=1", null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        if (null == cursor || cursor.getCount() <= 0) {
            return null;
        }
        ArrayList<ContactInfo> list = new ArrayList<>();
        mFavouroteIds.clear();
        while (cursor.moveToNext()) {
            ContactInfo contact = createContactInfo(cursor, contentResolver);
            contact.setIsFavourite(ContactDbInfo.IS_FAVOURITE);
            list.add(contact);
            mFavouroteIds.add(contact.getContactName());
        }
        if (null != cursor) {
            cursor.close();
        }
        return list;
    }

    //The last three days call record，except starred
    public ArrayList<String> getRecentContact(int days) {
        long time = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000;
        ContentResolver contentResolver = mContext.getContentResolver();
        @SuppressLint("MissingPermission") Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, //系统方式获取通讯录存储地址
                new String[]{CallLog.Calls.CACHED_NAME,  //姓名
                }, CallLog.Calls.DATE+">="+String.valueOf(time), null, CallLog.Calls.DEFAULT_SORT_ORDER);

        if (null == cursor || cursor.getCount() == 0) {
            return null;
        }
        Log.d("mytest", "cursor size " + cursor.getCount());

        ArrayList<String> recentList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String contactName = cursor.getString(cursor.getColumnIndex(
                    CallLog.Calls.CACHED_NAME));
            if (!mFavouroteIds.contains(contactName)) {
                recentList.add(contactName);
            }
        }
        Log.d("mytest", "list size " + recentList.size());
        return recentList;
    }

    @Override
    public void getAllContactList(ContactLoadCallback callback, int days, int count) {
        ArrayList<ContactInfo> favouriteList = getFavouriteContact();
        ArrayList<String> callLogList = getRecentContact(days);
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                ContactsContract.Contacts.STARRED + "=0", null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        if (null == cursor || cursor.getCount() <= 0) {
            callback.onContactListNotAvailable();
            return;
        }
        ArrayList<ContactInfo> recentList = new ArrayList<>();
        ArrayList<ContactInfo> nomalList = new ArrayList<>();
        while (cursor.moveToNext()) {
            ContactInfo contact = createContactInfo(cursor, contentResolver);
            contact.setIsFavourite(0);
            if (recentList.size() < count && null != callLogList && callLogList.contains(contact.getContactName())) {
                contact.setIsRecentContact(ContactInfo.IS_RECENT);
                recentList.add(contact);
            } else {
                contact.setIsRecentContact(0);
                nomalList.add(contact);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        convertList(favouriteList, recentList, nomalList, callback);
    }

    public ContactInfo createContactInfo(Cursor cursor, ContentResolver contentResolver) {
        ContactInfo contact = new ContactInfo();
        String contactId = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts._ID));
        contact.setContactId(contactId);
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        contact.setContactName(name);
        contact.setFirstLetter(getFirstLetter(name));
        String photo = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.PHOTO_URI));
        contact.setPhoto(photo);
        String photoThumbnail = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
        contact.setPhotoThumbnail(photoThumbnail);
        String isFavourite = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.STARRED));
        contact.setIsFavourite(Integer.parseInt(isFavourite));
        int phoneCount = Integer.parseInt(cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.HAS_PHONE_NUMBER)));
        if (phoneCount > 0) {
            ArrayList<String> phoneList = new ArrayList<>();
            Cursor phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                    , null
                    , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
                    , new String[]{contactId}
                    , null);
            while (phoneCursor.moveToNext()) {
                phoneList.add(phoneCursor.getString(phoneCursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER)));

            }
            contact.setPhoneNumList(phoneList);
            if (phoneCursor != null) {
                phoneCursor.close();
            }
        }
        //查询Email
        Cursor emails = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,
                null, null);
        if (null != cursor) {
            ArrayList<String> emailList = new ArrayList<>();
            while (emails.moveToNext()) {
                emailList.add(emails.getString(emails.getColumnIndex(
                        ContactsContract.CommonDataKinds.Email.DATA)));
            }
            emails.close();
        }
        return contact;
    }

    public void convertList(ArrayList<ContactInfo> fList, ArrayList<ContactInfo> rList,
                            ArrayList<ContactInfo> nList, ContactLoadCallback callback) {
        ArrayList<ContactInfo> allList = new ArrayList<>();
        if (null != fList) {
            allList.addAll(fList);
        }
        if (null != rList) {
            allList.addAll(rList);
        }
        if (null != nList) {
            allList.addAll(nList);
        }
        callback.onContactListLoaded(allList);
    }

    //获取首字母
    public String getFirstLetter(String key) {
        String sortString = key.substring(0, 1).toUpperCase();
        if (sortString.matches("[A-Z]")) {
            return sortString.toUpperCase();
        } else {
            String sortPy = CnToSpell.getFirstLetter(sortString).toUpperCase();
            if (sortPy.matches("[A-Z]")) {
                return sortPy;
            } else {
                return "#";
            }
        }

    }
}
