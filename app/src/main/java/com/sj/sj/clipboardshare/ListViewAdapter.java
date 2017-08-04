package com.sj.sj.clipboardshare;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    // ArrayList to store the data added to Adapter
    private ArrayList<AccountObject> AccountObjectList = new ArrayList<>() ;

    // constructor for ListViewAdapter
    public ListViewAdapter() {
    }

    // returns the number of data used by the adapter. : required implementation
    @Override
    public int getCount() {
        return AccountObjectList.size() ;
    }

    // return the View that will be used to display the data at the position on the screen. : required implementation
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

            // Inflate the "listview_item" layout to get a reference to convertView.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_account, parent, false);
        }

        // obtain a reference to the widget from the View (Layout inflated) to be displayed on the screen.
        ImageView iconImageView = (ImageView)convertView.findViewById(R.id.profile_image) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.name) ;
        TextView descTextView = (TextView) convertView.findViewById(R.id.screen_name) ;

        // obtain a data reference located at position in the Data Set(AccountObjectList)
        AccountObject AccountObject = AccountObjectList.get(position);

        // reflect data on each widget in the item.
        iconImageView.setImageDrawable(AccountObject.getIcon());
        titleTextView.setText(AccountObject.getTitle());
        String temp = AccountObject.getDesc() + "";
        descTextView.setText(temp);

        return convertView;
    }

    // return the ID of the item associated with the data at the specified position. : Required implementation
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // return the data at the specified position : required implementation
    @Override
    public AccountObject getItem(int position) {
        return AccountObjectList.get(position) ;
    }

    // function to add item data. able to customize.
    public void addItem(Drawable icon, String title, String desc) {
        AccountObject item = new AccountObject();

        item.setIcon(icon);
        item.setTitle(title);
        item.setDesc(desc);

        AccountObjectList.add(item);
    }

    // function to remove item data. able to customize.
    public void removeItem(int position) {
        AccountObjectList.remove(position);
    }
}