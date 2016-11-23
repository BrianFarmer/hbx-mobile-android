package gov.dc.broker.models.roster;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

/**
 * Created by plast on 11/10/2016.
 */

public class Dependent {
    @SerializedName("first_name")
    public String firstName;

    @SerializedName("middle_name")
    public String middleName;

    @SerializedName("last_name")
    public String last_name;

    @SerializedName("name_suffix")
    public String name_suffix;

    @SerializedName("date_of_birth")
    public DateTime dateOfBirth;

    @SerializedName("ssn_masked")
    public String ssnMasked;

    @SerializedName("gender")
    public String gender;
}
