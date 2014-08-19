package com.katadigital.phone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class VoicemailActivity extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.voicemail, container, false);
		((MainActivity) getActivity()).currentTab = 4;
		getActivity().invalidateOptionsMenu();

		
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse(String.valueOf("voicemail:")));
		startActivity(callIntent);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

}
