package com.example.zhulinping.contactdemo.diaplay;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zhulinping.contactdemo.MainActivity;
import com.example.zhulinping.contactdemo.R;
import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhulinping on 2017/7/28.
 */

public class ContactFragment extends Fragment implements ContactContact.View {
    View mContentView;
    ContactContact.Presenter mPresenter;
    private ContactAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ContentObserver mObserver;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPresenter.getContactList();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.contact_fragment_layout, null);
        intiView();
        mObserver = new ContactObserver(mHandler);
        getActivity().getApplicationContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI,true,mObserver);
        return mContentView;
    }

    private void intiView() {
        mRecyclerView = (RecyclerView) mContentView.findViewById(R.id.contact_lv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new ContactAdapter(new ArrayList<ContactInfo>());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(ContactContact.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showContactList(List<ContactInfo> list) {
        mAdapter.setContactList(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showErrorLayout() {

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().getContentResolver().unregisterContentObserver(mObserver);
        if(null != mHandler){
            mHandler = null;
        }
    }
}
