
package org.dchbx.coveragehq.models.employer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class EmployerContributionByRelationship {

    @SerializedName("employee")
    @Expose
    public Double employee;
    @SerializedName("spouse")
    @Expose
    public Double spouse;
    @SerializedName("domestic_partner")
    @Expose
    public Double domesticPartner;
    @SerializedName("child_under_26")
    @Expose
    public Double childUnder26;

}
