
package gov.dc.broker.models.brokerclient;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class EmployerContributionByRelationship_ {

    @SerializedName("employee")
    @Expose
    public Integer employee;
    @SerializedName("spouse")
    @Expose
    public Integer spouse;
    @SerializedName("domestic_partner")
    @Expose
    public Integer domesticPartner;
    @SerializedName("child_under_26")
    @Expose
    public Integer childUnder26;

}
