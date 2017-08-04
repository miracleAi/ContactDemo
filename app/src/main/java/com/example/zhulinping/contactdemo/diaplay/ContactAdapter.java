package com.example.zhulinping.contactdemo.diaplay;


import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zhulinping.contactdemo.R;
import com.example.zhulinping.contactdemo.contactdata.model.ContactDbInfo;
import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;

import java.util.List;

/**
 * Created by zhulinping on 2017/7/28.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {
    List<ContactInfo> contactList;
    String lastLetter = "";

    public ContactAdapter(List<ContactInfo> list) {
        setContactList(list);
    }

    public void setContactList(List<ContactInfo> list) {
        contactList = list;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item_contact_layout, null);
        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        ContactInfo info = contactList.get(position);
        holder.nameTv.setText(info.contactName);
        if (info.phoneNumList != null && info.phoneNumList.size() > 0) {
            holder.phoneTv.setText(info.phoneNumList.get(0));
        }
        if (position > 0) {
            lastLetter = contactList.get(position - 1).indexFlag;
        } else {
            lastLetter = "";
        }
        if (lastLetter.equals(info.indexFlag)) {
            holder.indexTv.setVisibility(View.GONE);
        } else {
            holder.indexTv.setText(info.indexFlag);
            holder.indexTv.setVisibility(View.VISIBLE);
        }

}

    @Override
    public int getItemCount() {
        return contactList.size();
    }

class ContactHolder extends RecyclerView.ViewHolder {
    TextView nameTv;
    ImageView headImv;
    TextView phoneTv;
    TextView indexTv;

    public ContactHolder(View itemView) {
        super(itemView);
        nameTv = (TextView) itemView.findViewById(R.id.name_tv);
        headImv = (ImageView) itemView.findViewById(R.id.head_imv);
        phoneTv = (TextView) itemView.findViewById(R.id.phone_tv);
        indexTv = (TextView) itemView.findViewById(R.id.index_tv);
    }
}
}
