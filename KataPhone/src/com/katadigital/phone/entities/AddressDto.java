package com.katadigital.phone.entities;

public class AddressDto {

	int id;
	String addressType="";
	String streetStr="";
	String neigborhoodStr="";
	String cityStr="";
	String zipCodeStr="";
	String countryStr="";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAddressType() {
		return addressType;
	}

	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}

	public String getStreetStr() {
		return streetStr;
	}

	public void setStreetStr(String streetStr) {
		this.streetStr = streetStr;
	}

	public String getNeigborhoodStr() {
		return neigborhoodStr;
	}

	public void setNeigborhoodStr(String neigborhoodStr) {
		this.neigborhoodStr = neigborhoodStr;
	}

	public String getCityStr() {
		return cityStr;
	}

	public void setCityStr(String cityStr) {
		this.cityStr = cityStr;
	}

	public String getZipCodeStr() {
		return zipCodeStr;
	}

	public void setZipCodeStr(String zipCodeStr) {
		this.zipCodeStr = zipCodeStr;
	}

	public String getCountryStr() {
		return countryStr;
	}

	public void setCountryStr(String countryStr) {
		this.countryStr = countryStr;
	}

}
