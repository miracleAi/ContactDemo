package com.example.zhulinping.contactdemo.contactdisplay.diaplay;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zhulinping.contactdemo.R;
import com.example.zhulinping.contactdemo.contactdisplay.contactdata.model.ContactInfo;
import com.example.zhulinping.contactdemo.contactdisplay.view.recyclerViewAdapter.expand.StickyRecyclerHeadersAdapter;
import com.example.zhulinping.contactdemo.contactdisplay.view.slidebar.IndexAdapter;
import com.example.zhulinping.contactdemo.contactdisplay.view.recyclerViewAdapter.BaseAdapter;
import com.example.zhulinping.contactdemo.contactdisplay.view.swipitemlayout.SwipeItemLayout;

import java.util.List;

/**
 * Created by zhulinping on 2017/8/1.
 */

public class ContactIndexAdapter extends BaseAdapter<ContactInfo,ContactIndexAdapter.ContactHolder>
        implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder>,IndexAdapter{
    List<ContactInfo> contactList;
    public ContactIndexAdapter(List<ContactInfo> list){
        setContactList(list);
    }
    public void setContactList(List<ContactInfo> list){
        contactList = list;
        this.addAll(contactList);
        notifyDataSetChanged();
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item_contact_layout, null);
        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        holder.mRoot.setSwipeAble(false);
        holder.nameTv.setText(getItem(position).contactName);
        if (getItem(position).phoneNumList != null && getItem(position).phoneNumList.size() > 0) {
            holder.phoneTv.setText(getItem(position).phoneNumList.get(0).value);
        }

    }

    @Override
    public long getHeaderId(int position) {
        return getItem(position).indexFlag.charAt(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_header, parent, false);
        return new RecyclerView.ViewHolder(view) {};
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        String headStr = String.valueOf(getItem(position).indexFlag);
        if (ContactInfo.FAVOURITE_FLAG.equals(headStr)) {
            textView.setText(ContactInfo.FAVOURITE_FLAG_TXT);
        } else if (ContactInfo.RECENT_FLAG.equals(headStr)) {
            textView.setText(ContactInfo.RECENT_FLAG_TXT);
        } else {
            textView.setText(headStr);
        }
    }

    class ContactHolder extends RecyclerView.ViewHolder{

        TextView nameTv;
        ImageView headImv;
        TextView phoneTv;
        SwipeItemLayout mRoot;

        public ContactHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
            headImv = (ImageView) itemView.findViewById(R.id.head_imv);
            phoneTv = (TextView) itemView.findViewById(R.id.phone_tv);
            mRoot = (SwipeItemLayout) itemView.findViewById(R.id.item_contact_swipe_root);
        }
    }
}
