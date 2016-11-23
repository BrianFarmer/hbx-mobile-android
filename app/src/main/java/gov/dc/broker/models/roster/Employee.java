
package gov.dc.broker.models.roster;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Employee {

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
    public DateTime dateOfBirth;
    @SerializedName("ssn_masked")
    @Expose
    public String ssnMasked;
    @SerializedName("gender")
    @Expose
    public String gender;
    @SerializedName("hired_on")
    @Expose
    public DateTime hiredOn;
    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("enrollments")
    @Expose
    public Enrollments enrollments;
    @SerializedName("is_business_owner")
    @Expose
    public boolean isBusinessOwner;
    @SerializedName("dependents")
    @Expose
    public List<Dependent> dependents = new ArrayList<Dependent>();

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
