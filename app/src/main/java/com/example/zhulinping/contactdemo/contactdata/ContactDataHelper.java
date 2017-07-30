package com.example.zhulinping.contactdemo.contactdata;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;
import com.example.zhulinping.contactdemo.utils.CnToSpell;

import java.util.ArrayList;

/**
 * Created by zhulinping on 2017/7/28.
 */

public class ContactDataHelper implements IContactDataHelper {
    private Context mContext;

    public ContactDataHelper(Context context) {
        mContext = context;
    }

    @Override
    public void getAllContactList(ContactLoadCallback callback) {
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, ContactsContract.Contacts.DISPLAY_NAME+" COLLATE LOCALIZED ASC");
        if (cursor.getCount() <= 0) {
            callback.onContactListNotAvailable();
            return;
        }
        cursor.moveToFirst();
        ArrayList<ContactInfo> favouriteList = new ArrayList<>();
        ArrayList<ContactInfo> recentList = new ArrayList<>();
        ArrayList<ContactInfo> nomalList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            String contactId = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts._ID));
            ContactInfo contact = new ContactInfo();
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            contact.setContactName(name);
            String sortKey = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts.SORT_KEY_PRIMARY));
            contact.setSortKeyName(sortKey);
            contact.setFirstLetter(getFirstLetter(sortKey));
            String photo = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts.PHOTO_URI));
            contact.setPhoto(photo);
            String photoThumbnail = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
            contact.setPhotoThumbnail(photoThumbnail);
            String isFavourite = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts.STARRED));
            contact.setIsFavourite(Integer.parseInt(isFavourite));
            long lastCallTime = cursor.getLong(cursor.getColumnIndex(
                    ContactsContract.Contacts.LAST_TIME_CONTACTED));
            contact.setLastContactTime(lastCallTime);
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
                while(phoneCursor.moveToNext()){
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
            if(null != cursor) {
                ArrayList<String> emailList = new ArrayList<>();
                while (emails.moveToNext()) {
                    emailList.add(emails.getString(emails.getColumnIndex(
                            ContactsContract.CommonDataKinds.Email.DATA)));
                }
                emails.close();
            }

            if (contact.getIsFavourite() == 1) {
                contact.setIsRecentContact(0);
                favouriteList.add(contact);
            } else if (recentList.size() < 5 && isLastThreeDays(contact)) {
                contact.setIsRecentContact(1);
                recentList.add(contact);
            } else {
                contact.setIsRecentContact(0);
                nomalList.add(contact);
            }
            cursor.moveToNext();
        }
        if (cursor != null) {
            cursor.close();
        }
        convertList(favouriteList, recentList, nomalList, callback);
    }

    public void convertList(ArrayList<ContactInfo> fList, ArrayList<ContactInfo> rList,
                            ArrayList<ContactInfo> nList, ContactLoadCallback callback) {
        ArrayList<ContactInfo> allList = new ArrayList<>();
        if(null != fList){
            allList.addAll(fList);
        }
        if(null != rList){
            allList.addAll(rList);
        }
        if(null != nList){
            allList.addAll(nList);
        }
        callback.onContactListLoaded(allList);
    }

    //获取首字母
    public String getFirstLetter(String key){
        String sortString = key.substring(0, 1).toUpperCase();
        if (sortString.matches("[A-Z]")) {
            return sortString.toUpperCase();
        } else {
            String sortPy = CnToSpell.getFirstLetter(sortString).toUpperCase();
            if (sortPy.matches("[A-Z]")){
                return sortPy;
            }else{
                return "#";
            }
        }

    }
    //TODO:排序还没有做好
    //按照最近联系时间排序
    public ArrayList<ContactInfo> sortByTimes(ArrayList<ContactInfo> list) {
        return list;
    }
    //判断是否是最近三天联系
    public boolean isLastThreeDays(ContactInfo contact) {
        long cha = System.currentTimeMillis() - contact.getLastContactTime();
        Log.d("mytest","==="+System.currentTimeMillis());
        Log.d("mytest","+++"+contact.getLastContactTime());
        if(cha < 0){
            return false;
        }
        double result = cha/(1000*60*60);
        if(result < 72){
            return true;
        }
        return false;
    }
}
