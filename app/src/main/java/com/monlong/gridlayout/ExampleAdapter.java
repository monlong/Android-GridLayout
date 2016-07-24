package com.monlong.gridlayout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * @Descirption:
 * @Author: monlong
 * @Email: 826309156@qq.com
 * @Date: 2016-07-18 09:03
 * @Version: 1.0.0
 */
public class ExampleAdapter extends BaseAdapter {

    private List<String> mStrings;
    private LayoutInflater mInflater;

    public ExampleAdapter(Context context, List<String> strings) {
        mInflater = LayoutInflater.from(context);
        mStrings = strings;
    }

    @Override
    public int getCount() {
        return mStrings.size();
    }

    @Override
    public String getItem(int position) {
        return mStrings.get(position);
    }

    public void addItem(String string) {
        mStrings.add(string);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.example_gridlayout_item, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.textview);
        textView.setText(String.valueOf(position));
        return convertView;
    }
}
