package com.al.websocketchat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MessageListAdapter extends BaseAdapter {
    private Context mContext;
    private List<Message> mMessagesItems;


    public MessageListAdapter(Context context, List<Message> navDrawerItems) {
        this.mContext = context;
        this.mMessagesItems = navDrawerItems;
    }


    @Override
    public int getCount() {
        return mMessagesItems.size();
    }


    @Override
    public Object getItem(int position) {
        return mMessagesItems.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = mMessagesItems.get(position);

        LayoutInflater mInflater = (LayoutInflater) mContext
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (mMessagesItems.get(position).isSelf())
            convertView = mInflater.inflate(R.layout.list_item_message_right,null);
        else
            convertView = mInflater.inflate(R.layout.list_item_message_left,null);

        TextView lblFrom = convertView.findViewById(R.id.txv_msgFrom);
        TextView txtMsg = convertView.findViewById(R.id.txv_msg);

        txtMsg.setText(message.getMessage());
        lblFrom.setText(message.getFromName());

        return convertView;
    }
}