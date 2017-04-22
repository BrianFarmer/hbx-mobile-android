package org.dchbx.coveragehq.models.roster;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Address {
    @Expose
    public String kind;
    @Expose
    @SerializedName("address_1")
    public String address1;
    @Expose
    @SerializedName("address_2")
    public String address2;
    @Expose
    public String city;
    @Expose
    public String county;
    @Expose
    public String state;
    @Expose
    @SerializedName("location_state_code")
    public String locationStateCode;
    @Expose
    public String zip;
    @Expose
    @SerializedName("country_name")
    public String countryName;
}
