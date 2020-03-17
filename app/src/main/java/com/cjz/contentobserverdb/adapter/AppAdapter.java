package com.cjz.contentobserverdb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cjz.contentobserverdb.MainActivity;
import com.cjz.contentobserverdb.R;
import com.cjz.contentobserverdb.bean.Information;

import java.util.List;

public class AppAdapter extends BaseAdapter {

    private MainActivity main;
    private List<Information> informations;

    public AppAdapter(MainActivity main) {
        this.main = main;
    }

    @Override
    public int getCount() {
        informations = main.getInformations();
        return informations.size();
    }

    @Override
    public Object getItem(int position) {
        return informations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Information information = informations.get(position);
        ViewHold hold;
        if (convertView == null) {
            convertView = LayoutInflater.from(main).inflate(R.layout.main_item, null);
            hold = new ViewHold();
            hold.textView = convertView.findViewById(R.id.tv_sms);
            convertView.setTag(hold);
        } else {
            hold = (ViewHold) convertView.getTag();
        }
        hold.textView.setText(information.getInfo());
        return convertView;
    }

    class ViewHold {
        TextView textView;
    }

}
