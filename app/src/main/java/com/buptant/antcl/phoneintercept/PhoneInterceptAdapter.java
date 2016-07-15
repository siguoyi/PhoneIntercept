package com.buptant.antcl.phoneintercept;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by antcl on 2016/4/18.
 */
public class PhoneInterceptAdapter extends BaseAdapter{
    private static final String TAG = PhoneInterceptAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<String> date;
    private ArrayList<String> contactsNumber;

    public PhoneInterceptAdapter(Context context, ArrayList<String> date, ArrayList<String> contactsNumber){
        this.context = context;
        this.date = date;
        this.contactsNumber = contactsNumber;
    }

    @Override
    public int getCount() {
        return date.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.list_item,null);

            viewHolder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            viewHolder.tv_tel = (TextView) convertView.findViewById(R.id.tv_tel);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tv_date.setText(date.get(position));
        viewHolder.tv_tel.setText(contactsNumber.get(position));

        return convertView;
    }

    static class ViewHolder{
        TextView tv_date;
        TextView tv_tel;
    }
}
