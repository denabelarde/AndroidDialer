package com.katadigital.phone.adapters;

import java.util.ArrayList;
import java.util.Currency;

import com.katadigital.phone.R;
import com.katadigital.phone.entities.PhoneNumberDto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LogsExistingAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<PhoneNumberDto> phoneNumbers;
	private String numberCalled;

	public LogsExistingAdapter(Context context,
			ArrayList<PhoneNumberDto> phoneNumbers, String numberCalled) {

		super();
		this.context = context;
		this.numberCalled = numberCalled;
		this.phoneNumbers = new ArrayList<PhoneNumberDto>();
		this.phoneNumbers.addAll(phoneNumbers);
	}

	@Override
	public int getCount() {
		return phoneNumbers.size();
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
		View rowView = inflater.inflate(R.layout.contactnumber_listitem,
				viewGroup, false);
		TextView detailNumberType = (TextView) rowView
				.findViewById(R.id.detail_number_type);
		TextView detailNumber = (TextView) rowView
				.findViewById(R.id.detail_number);

		if (phoneNumbers.get(i) != null) {
			PhoneNumberDto phoneNumberDto = phoneNumbers.get(i);
			if(phoneNumberDto.getNumber().equalsIgnoreCase(numberCalled)){
				detailNumberType.setText(phoneNumberDto.getNumberType()+" ***");
			}else{
				detailNumberType.setText(phoneNumberDto.getNumberType());
			}
		
			
			detailNumber.setText(phoneNumberDto.getNumber());
		}

		return rowView;
	}
}
