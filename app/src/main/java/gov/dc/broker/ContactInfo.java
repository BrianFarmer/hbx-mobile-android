package gov.dc.broker;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by plast on 7/29/2016.
 */
public class ContactInfo {
    @SerializedName("first")
    public String firstName;

    @SerializedName("last")
    public String lastName;

    @SerializedName("phone")
    public String phone;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("emails")
    public List<String> emails;

    @SerializedName("address_1")
    public String addres1;

    @SerializedName("address_2")
    public String address2;

    @SerializedName("city")
    public String city;

    @SerializedName("state")
    public String state;

    @SerializedName("zip")
    public String zip;
}
