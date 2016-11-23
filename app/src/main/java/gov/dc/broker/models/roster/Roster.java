
package gov.dc.broker.models.roster;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Roster {

    @SerializedName("employer_name")
    @Expose
    public String employerName;
    @SerializedName("roster")
    @Expose
    public List<Employee> roster = new ArrayList<Employee>();

}
