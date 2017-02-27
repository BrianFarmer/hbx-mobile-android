package org.dchbx.coveragehq.models.roster;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalDate;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Dependent {

    @SerializedName("first_name")
    @Expose
    public String firstName;
    @SerializedName("middle_name")
    @Expose
    public String middleName;
    @SerializedName("last_name")
    @Expose
    public String lastName;
    @SerializedName("name_suffix")
    @Expose
    public String nameSuffix;
    @SerializedName("date_of_birth")
    @Expose
    public LocalDate dateOfBirth;
    @SerializedName("ssn_masked")
    @Expose
    public String ssnMasked;
    @SerializedName("gender")
    @Expose
    public String gender;

}