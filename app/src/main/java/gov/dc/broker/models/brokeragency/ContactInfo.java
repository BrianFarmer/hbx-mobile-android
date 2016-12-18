
package gov.dc.broker.models.brokeragency;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ContactInfo implements Serializable{

    @SerializedName("first")
    @Expose
    public String first;
    @SerializedName("last")
    @Expose
    public String last;
    @SerializedName("phone")
    @Expose
    public String phone;
    @SerializedName("mobile")
    @Expose
    public String mobile;
    @SerializedName("emails")
    @Expose
    public List<String> emails = new ArrayList<String>();
    @SerializedName("address_1")
    @Expose
    public String address1;
    @SerializedName("address_2")
    @Expose
    public String address2;
    @SerializedName("city")
    @Expose
    public String city;
    @SerializedName("state")
    @Expose
    public String state;
    @SerializedName("zip")
    @Expose
    public String zip;

}
