package com.katadigital.phone.adapters;

import java.util.ArrayList;

import com.katadigital.phone.R;
import com.katadigital.phone.entities.AddressDto;
import com.katadigital.phone.entities.EmailDto;
import com.katadigital.phone.entities.PhoneNumberDto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AddressesAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<AddressDto> addresses;

	public AddressesAdapter(Context context, ArrayList<AddressDto> addresses) {

		super();
		this.context = context;

		this.addresses = new ArrayList<AddressDto>();
		this.addresses.addAll(addresses);
	}

	@Override
	public int getCount() {
		return addresses.size();
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
		View rowView = inflater.inflate(R.layout.address_listitem, viewGroup,
				false);
		TextView addressType = (TextView) rowView
				.findViewById(R.id.detail_address_type);
		TextView addressDetails = (TextView) rowView
				.findViewById(R.id.detail_address);

		if (addresses.get(i) != null) {
			AddressDto addressDto = addresses.get(i);
			addressType.setText(addressDto.getAddressType());
			addressDetails.setText((addressDto.getStreetStr().isEmpty() ? ""
					: addressDto.getStreetStr() + "\n")
					+ (addressDto.getNeigborhoodStr().isEmpty() ? ""
							: addressDto.getNeigborhoodStr() + "\n")
					+ (addressDto.getCityStr().isEmpty() ? "" : addressDto
							.getCityStr() + "\n")
					+ (addressDto.getCountryStr().isEmpty() ? "" : addressDto
							.getCountryStr() + "\n")
					+ (addressDto.getZipCodeStr().isEmpty() ? "" : addressDto
							.getZipCodeStr()));
		}

		return rowView;
	}
}
