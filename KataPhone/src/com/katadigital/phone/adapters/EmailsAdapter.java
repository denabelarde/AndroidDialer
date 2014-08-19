package com.katadigital.phone.adapters;

import java.util.ArrayList;

import com.katadigital.phone.R;
import com.katadigital.phone.entities.EmailDto;
import com.katadigital.phone.entities.PhoneNumberDto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EmailsAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<EmailDto> emails;

	public EmailsAdapter(Context context, ArrayList<EmailDto> emails) {

		super();
		this.context = context;

		this.emails = new ArrayList<EmailDto>();
		this.emails.addAll(emails);
	}

	@Override
	public int getCount() {
		return emails.size();
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
		View rowView = inflater.inflate(R.layout.email_listitem,
				viewGroup, false);
		TextView emailType = (TextView) rowView
				.findViewById(R.id.detail_email_type);
		TextView detailEmail = (TextView) rowView
				.findViewById(R.id.detail_email);

		if (emails.get(i) != null) {
			EmailDto emailDto = emails.get(i);
			emailType.setText(emailDto.getEmailType());
			detailEmail.setText(emailDto.getEmail());
		}

		return rowView;
	}
}
