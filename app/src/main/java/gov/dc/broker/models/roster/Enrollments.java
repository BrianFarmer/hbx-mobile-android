
package gov.dc.broker.models.roster;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Enrollments {

    @SerializedName("active")
    @Expose
    public Active active;
    @SerializedName("renewal")
    @Expose
    public Renewal renewal;

}
