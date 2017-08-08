package com.example.zhulinping.contactdemo.diaplay;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zhulinping.contactdemo.R;
import com.example.zhulinping.contactdemo.contactdata.AlphabeticIndexCompat;
import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;
import com.example.zhulinping.contactdemo.view.recyclerViewAdapter.DividerDecoration;
import com.example.zhulinping.contactdemo.view.recyclerViewAdapter.expand.StickyRecyclerHeadersDecoration;
import com.example.zhulinping.contactdemo.view.slidebar.ZSideBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhulinping on 2017/8/1.
 */

public class ContactIndexFragment extends Fragment implements ContactContact.View {
    View mContentView;
    ContactContact.Presenter mPresenter;
    private ContactIndexAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ZSideBar mSlidBar;
    AlphabeticIndexCompat compat;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.index_fragment_layout, null);
        intiView();
        compat = new AlphabeticIndexCompat(getActivity());
        Log.d("mytest",compat.computeSectionName("こなん"));
        Log.d("mytest",compat.computeSectionName("한국어"));
        Log.d("mytest",compat.computeSectionName("hello"));
        Log.d("mytest",compat.computeSectionName("你"));

        return mContentView;
    }

    private void intiView() {
        mSlidBar = (ZSideBar) mContentView.findViewById(R.id.contact_zsidebar);
        mRecyclerView = (RecyclerView) mContentView.findViewById(R.id.contact_lv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new ContactIndexAdapter(new ArrayList<ContactInfo>());
        mRecyclerView.setAdapter(mAdapter);
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(mAdapter);
        mRecyclerView.addItemDecoration(headersDecor);
        mRecyclerView.addItemDecoration(new DividerDecoration(getActivity()));
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                headersDecor.invalidateHeaders();
            }
        });
        mSlidBar.setupWithRecycler(mRecyclerView);
    }
    public void testIndex(){

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
    }

    @Override
    public void showErrorLayout() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
