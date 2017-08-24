package com.example.zhulinping.contactdemo.contactdata;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.ecity.android.tinypinyin.Pinyin;
import com.example.zhulinping.contactdemo.contactdata.model.ContactDbInfo;
import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;
import com.example.zhulinping.contactdemo.diaplay.ContactContact;
import com.example.zhulinping.contactdemo.utils.CnToSpell;
import com.example.zhulinping.contactdemo.utils.SortByFirstComparator;
import com.example.zhulinping.contactdemo.utils.SortByTimeComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhulinping on 2017/7/28.
 */

public class ContactDataUtils {
    private static String TAG = "test.zlp";

    /**
     * 获取包含联系人详细信息的表
     */
    public static List<ContactInfo> getContactDetailList(Context context) {
        Cursor cursor = null;
        AlphabeticIndexCompat compat = new AlphabeticIndexCompat(context);
        try {
            ContentResolver contentResolver = context.getContentResolver();
            String[] projection = {ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.PHOTO_URI,
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                    ContactsContract.Contacts.STARRED,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER};
            cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection,
                    null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
            if (null == cursor) {
                return null;
            }
            ArrayList<ContactInfo> list = new ArrayList<>();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                ContactInfo contact = new ContactInfo();
                contact.contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                contact.lookupUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                contact.contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contact.photo = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                contact.photoThumbnail = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                contact.isFavourite = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)));
                contact.firstLetter = getFirstLetter(contact.contactName);
                contact.isRecentContact = 0;
                if (contact.isFavourite == ContactInfo.IS_FAVOURITE) {
                    contact.indexFlag = ContactInfo.FAVOURITE_FLAG;
                } else {
                    contact.indexFlag = compat.computeSectionName(contact.firstLetter);
                }
                list.add(contact);
            }
            return list;

        } catch (Exception e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            cursor.close();
        }
    }

    //查询联系人电话、邮件等信息
    public static void fillContactInfos(Context context, List<ContactInfo> source) {
        if (source == null) {
            return;
        }

        final String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1};
        final String TYPE_PHONE = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
        final String TYPE_EMAIL = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
        final String TYPE_NOTE = ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE;
        String select = "(" + ContactsContract.Data.MIMETYPE + " = '" + TYPE_EMAIL + "' or "
                + ContactsContract.Data.MIMETYPE + "='" + TYPE_NOTE + "' or "
                + ContactsContract.Data.MIMETYPE + "='" + TYPE_PHONE + "')";
        ContentResolver contentResolver = context.getContentResolver();
        Map<String, List<String>> phoneMap = new HashMap<>();
        Map<String, List<String>> emailMap = new HashMap<>();
        Map<String, String> noteMap = new HashMap<>();
        //电话
        Cursor phoneCursor = null;
        try {
            phoneCursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI
                    , projection
                    , select
                    , null
                    , null);
            if (phoneCursor == null) {
                return;
            }
            phoneCursor.moveToPosition(-1);
            String phoneNum = "";
            String emailNum = "";
            while (phoneCursor.moveToNext()) {
                String contactId = phoneCursor.getString(0);
                String type = phoneCursor.getString(1);
                String value = phoneCursor.getString(2);
                switch (type) {
                    case TYPE_EMAIL: {
                        emailNum = value;
                        if (emailMap.containsKey(contactId)) {
                            emailMap.get(contactId).add(emailNum);
                        } else {
                            List<String> list = new ArrayList<>();
                            list.add(emailNum);
                            emailMap.put(contactId, list);
                        }
                        break;
                    }
                    case TYPE_NOTE: {
                        noteMap.put(contactId, value);
                        break;
                    }
                    case TYPE_PHONE: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            phoneNum = PhoneNumberUtils.normalizeNumber(value);
                        }else{
                            phoneNum = value;
                            if (phoneMap.containsKey(contactId)) {
                                phoneMap.get(contactId).add(phoneNum);
                            } else {
                                List<String> list = new ArrayList<>();
                                list.add(phoneNum);
                                phoneMap.put(contactId, list);
                            }
                        }
                        break;
                    }
                }
            }
            for (ContactInfo bean : source) {
                if (bean.contactId < 0) {
                    continue;
                }
                String contactId = String.valueOf(bean.contactId);
                if (phoneMap.containsKey(contactId)) {
                    bean.phoneNumList.addAll(phoneMap.get(contactId));
                }
                if (emailMap.containsKey(contactId)) {
                    bean.emailList.addAll(emailMap.get(contactId));
                }
                if (noteMap.containsKey(contactId)) {
                    bean.note = noteMap.get(contactId);
                }
                //处理姓名为空的情况
                if (TextUtils.isEmpty(bean.contactName)) {
                    if (bean.phoneNumList != null && bean.phoneNumList.size() > 0) {
                        bean.contactName = bean.phoneNumList.iterator().next();
                    } else if (bean.emailList != null && bean.emailList.size() > 0) {
                        bean.contactName = bean.phoneNumList.iterator().next();
                    }
                }
            }
        } catch (Exception e) {
                Log.e(TAG, "fillContactInfos: ", e);
        } finally {
            phoneCursor.close();
        }
    }

    //获取通话记录
    @SuppressLint("MissingPermission")
    public static List<ContactInfo> getRecentCalls(Context context) {
        ArrayList<ContactInfo> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(CallLog.Calls.CONTENT_URI,
                    new String[]{CallLog.Calls.CACHED_NAME,  //姓名
                            CallLog.Calls.NUMBER,
                            CallLog.Calls.DATE}, null, null,
                    CallLog.Calls.DEFAULT_SORT_ORDER);
            if (null == cursor || cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                ContactInfo bean = new ContactInfo();
                bean.contactName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                bean.lastContactTime = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                bean.phone = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                bean.contactName = bean.phone;
                if (!list.contains(bean)) {
                    list.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        fillContactName(context, list);
        return list;
    }

    public static void fillContactName(Context context, List<ContactInfo> list) {
        if (list == null) {
            return;
        }
        for (ContactInfo bean : list) {
            //根据电话号码获取名字
            Cursor phoneCursor = null;
            try {
                phoneCursor = context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                        ContactsContract.CommonDataKinds.Phone.NUMBER + " = ? or "
                                + ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " = ?",
                        new String[]{bean.phone, bean.phone}, null);
                if (phoneCursor != null) {
                    phoneCursor.moveToPosition(-1);
                    if (phoneCursor.moveToNext()) {
                        String contactName = phoneCursor.getString(phoneCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        bean.contactName = contactName;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                phoneCursor.close();
            }
        }
    }

    public static String getFirstLetter(String name) {
        String res = "#";
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            //只有汉字才有这种处理
            if (!Pinyin.isChinese(ch)) {
                if (i == 0) {
                    res = getFirst(ch);
                }
                continue;
            }
            String pinyin = Pinyin.toPinyin(ch);
            if (!TextUtils.isEmpty(pinyin)) {
                char firstLetter = pinyin.charAt(0);
                if (i == 0) {
                    res = getFirst(firstLetter);
                }
            }
        }
        return res;
    }

    //获取非拼音字母
    public static String getFirst(char key) {
        if (Character.isLetter(key)) {
            return String.valueOf(key).toUpperCase();
        } else {
            return "#";
        }
    }
}
