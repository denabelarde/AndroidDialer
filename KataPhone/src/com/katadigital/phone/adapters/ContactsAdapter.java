package com.katadigital.phone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.katadigital.phone.R;
import com.katadigital.phone.R.id;
import com.katadigital.phone.R.layout;

public class ContactsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> contactsArray;


    public ContactsAdapter(Context context, ArrayList<String> cont_arr) {

        super();
        this.context = context;

        contactsArray = new ArrayList<String>();
        contactsArray.addAll(cont_arr);
    }


    @Override
    public int getCount() {
        return contactsArray.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_custom, viewGroup, false);
        TextView textView1 = (TextView) rowView.findViewById(R.id.contacts_item_textview);

        textView1.setText(contactsArray.get(i));
        return rowView;
    }


}