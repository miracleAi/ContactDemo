package com.example.zhulinping.contactdemo.contactdata;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;
import com.example.zhulinping.contactdemo.utils.SortByFirstComparator;
import com.example.zhulinping.contactdemo.utils.SortByTimeComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zhulinping on 2017/8/4.
 */

public class ContactDataHelper implements IContactDataHelper,android.os.Handler.Callback{
    private static String TAG = "test.contactdatahelper";
    private static boolean DEBUG = true;
    private static ContactDataHelper mInstance;
    Context mContext;
    private Handler mWorkHandler;
    private volatile boolean isIniting;
    //联系人列表，以人为维度
    private List<ContactInfo> mContactDetailList = new ArrayList<ContactInfo>();
    private List<ContactInfo> mRecentCallList = new ArrayList<ContactInfo>();
    //联系人变化后更新数据，但是没有通知ui更新
    private Runnable mQueryContactTask = new Runnable() {
        @Override
        public void run() {
                Log.i(TAG, "联系人变化了，重新搜索");
                initData();
        }
    };

    public static synchronized ContactDataHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ContactDataHelper(context);
        }
        return mInstance;
    }

    private ContactDataHelper(Context context) {
        mContext = context;
        HandlerThread handlerThread = new HandlerThread("load_contact");
        handlerThread.start();
        mWorkHandler = new android.os.Handler(handlerThread.getLooper(), this);//todo 换到后台线程

        initDataSync();

        //联系人变化监听:通话记录表 联系人表
        //通话记录包括短信SMSConstant.CANONICAL_URI，因此移除之前短信监听
        context.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, false, new ContentObserver(mWorkHandler) {
            @Override
            public void onChange(boolean selfChange) {
                onChange(selfChange, CallLog.Calls.CONTENT_URI);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if (uri != null) {
                    mWorkHandler.removeCallbacks(mQueryContactTask);
                    mWorkHandler.postDelayed(mQueryContactTask, 300);
                }
            }
        });
        context.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, false, new ContentObserver(mWorkHandler) {
            @Override
            public void onChange(boolean selfChange) {
                onChange(selfChange, ContactsContract.Contacts.CONTENT_URI);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if (uri != null) {
                    mWorkHandler.removeCallbacks(mQueryContactTask);
                    mWorkHandler.postDelayed(mQueryContactTask, 300);
                }
            }
        });
    }

    private synchronized void initDataSync() {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        });
    }

    private synchronized void initData() {
        if (isIniting) {
            return;
        }
        isIniting = true;
        long time = System.currentTimeMillis();
        if (DEBUG) {
            Log.d(TAG, "============联系人相关数据初始化=============");
        }
        //查取联系人表
        mContactDetailList.clear();
        List<ContactInfo> detailBeans = ContactDataUtils.getContactDetailList(mContext);
        if (detailBeans != null) {
            mContactDetailList.addAll(detailBeans);
        }
        if (DEBUG) {
            Log.d(TAG, "initData: 加载联系人基本信息 cost=" + (System.currentTimeMillis() - time));
        }

        long time2 = System.currentTimeMillis();
        ContactDataUtils.fillContactInfos(mContext, detailBeans);
        if (DEBUG) {
            Log.d(TAG, "initData: 加载联系人详细信息 cost=" + (System.currentTimeMillis() - time2));
        }

        //查取通话记录表
        long time3 = System.currentTimeMillis();
        mRecentCallList.clear();
        List<ContactInfo> callBeans = ContactDataUtils.getRecentCalls(mContext);
        if (callBeans != null) {
            mRecentCallList.addAll(callBeans);
        }
        if (DEBUG) {
            Log.d(TAG, "initData: 加载最近通话列表 cost=" + (System.currentTimeMillis() - time3));
        }
        isIniting = false;
    }

    public synchronized final List<ContactInfo> getContactDetailList() {
        if (mContactDetailList.size() == 0) {
            //尝试再获取一次
            initData();
        }
        return new ArrayList<>(mContactDetailList);
    }

    public synchronized final List<ContactInfo> getRecentCallList() {
        if (mRecentCallList.size() == 0) {
            //尝试再获取一次
            initData();
        }
        return new ArrayList<>(mRecentCallList);
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    public void getAllContactList(ContactLoadCallback callback, int days, int count) {
        long time = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000;
        ArrayList<ContactInfo> contactList = new ArrayList<>();
        ArrayList<ContactInfo> favouriteList = new ArrayList<>();
        ArrayList<ContactInfo> recentList = new ArrayList<>();
        ArrayList<ContactInfo> nomalList = new ArrayList<>();

        List<ContactInfo> contacts = getContactDetailList();
        List<ContactInfo> recentCalls = getRecentCallList();

        List<ContactInfo> temNomalList = new ArrayList<>();
        if (contacts != null && contacts.size() > 0) {
            for (ContactInfo bean : contacts) {
                if (bean.isFavourite == ContactInfo.IS_FAVOURITE) {
                    favouriteList.add(bean);
                } else {
                    temNomalList.add(bean);
                }
            }
        }
        //最近通话中排除已收藏 ,保留最近三天,5个人
        List<ContactInfo> temRecentList = new ArrayList<>();
        if (recentCalls != null && recentCalls.size() > 0) {
            for (ContactInfo bean : recentCalls) {
                for (ContactInfo bean2 : favouriteList) {
                    if (!bean2.equals(bean) && bean.lastContactTime >= time) {
                        temRecentList.add(bean);
                        break;
                    }
                }
                if (temRecentList.size() >= 5) {
                    break;
                }
            }
        }
        //非收藏联系人中排除最近联系
        nomalList.addAll(temNomalList);
        if (temRecentList.size() > 0) {
            for (ContactInfo bean : temRecentList) {
                for (ContactInfo info : temNomalList) {
                    if (info.equals(bean)) {
                        info.lastContactTime = bean.lastContactTime;
                        info.isRecentContact = ContactInfo.IS_RECENT;
                        info.indexFlag = ContactInfo.RECENT_FLAG;
                        info.isFavourite = 0;
                        recentList.add(info);
                        nomalList.remove(info);
                        break;
                    }
                }
            }

        }
        if (favouriteList != null && favouriteList.size() > 0) {
            Collections.sort(favouriteList, new SortByFirstComparator());
            contactList.addAll(favouriteList);
        }
        if (recentList != null && recentList.size() > 0) {
            Collections.sort(recentList, new SortByTimeComparator());
            contactList.addAll(recentList);
        }
        if (nomalList != null && nomalList.size() > 0) {
            Collections.sort(nomalList, new SortByFirstComparator());
            contactList.addAll(nomalList);
        }
        if(contactList != null && contactList.size() > 0){
            callback.onContactListLoaded(contactList);
        }else{
            callback.onContactListNotAvailable();
        }
    }
}
