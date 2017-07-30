package com.example.zhulinping.contactdemo.diaplay;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zhulinping.contactdemo.R;
import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.contact_fragment_layout, null);
        intiView();
        return mContentView;
    }

    private void intiView() {
        mRecyclerView = (RecyclerView) mContentView.findViewById(R.id.contact_lv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new ContactAdapter();
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
}
