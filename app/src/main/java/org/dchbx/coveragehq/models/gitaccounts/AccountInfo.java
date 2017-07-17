package org.dchbx.coveragehq.models.gitaccounts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by plast on 3/13/2017.
 */

public class AccountInfo {
    @SerializedName("broker_endpoint")
    @Expose
    public String brokerEndpoint;

    @SerializedName("employer_details_endpoint_path")
    @Expose
    public String employerDetailsEndpointPath;

    @SerializedName("employee_roster_endpoint_path")
    @Expose
    public String employeeRosterEndpointPath;

    @SerializedName("individual_endpoint_path")
    @Expose
    public String individualEndpointPath;

    @SerializedName("endpoints_path")
    @Expose
    public String endpoints;

    public String name;
}
