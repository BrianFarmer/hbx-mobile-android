package org.dchbx.coveragehq.models.Security;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by plast on 12/15/2016.
 */

public class LoginResponse {
    @SerializedName("security_question")
    @Expose
    public String securityQuestion;
    @SerializedName("session_key")
    @Expose
    public String sessionKey;
    @SerializedName("session_value")
    @Expose
    public String sessionValue;
    @SerializedName("enrollUrl")
    @Expose
    public String enrollUrl;
    @SerializedName("broker_endpoint")
    @Expose
    public String brokerEndpoint;
    @SerializedName("employer_details_endpoint")
    @Expose
    public String employerDetailsEndpoint;
    @SerializedName("employee_roster_endpoint")
    @Expose
    public String employeeRosterEndpoint;
    @SerializedName("individual_endpoint")
    @Expose
    public String individualEndpoint;
}
