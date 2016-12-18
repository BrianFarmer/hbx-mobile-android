
package gov.dc.broker.models.roster;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class RosterEntry {

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
    @SerializedName("hired_on")
    @Expose
    public LocalDate hiredOn;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("enrollments")
    @Expose
    public List<Enrollment> enrollments = new ArrayList<Enrollment>();
    @SerializedName("is_business_owner")
    @Expose
    public Boolean isBusinessOwner;
    @SerializedName("dependents")
    @Expose
    public List<Dependent> dependents = new ArrayList<>();
}
