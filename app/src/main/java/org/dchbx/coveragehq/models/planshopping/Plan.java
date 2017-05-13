
package org.dchbx.coveragehq.models.planshopping;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Plan {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("active_year")
    @Expose
    public Integer activeYear;
    @SerializedName("coverage_kind")
    @Expose
    public String coverageKind;
    @SerializedName("dc_in_network")
    @Expose
    public Boolean dcInNetwork;
    @SerializedName("dental_level")
    @Expose
    public String dentalLevel;
    @SerializedName("is_active")
    @Expose
    public Boolean isActive;
    @SerializedName("is_standard_plan")
    @Expose
    public Boolean isStandardPlan;
    @SerializedName("market")
    @Expose
    public String market;
    @SerializedName("maximum_age")
    @Expose
    public Integer maximumAge;
    @SerializedName("metal_level")
    @Expose
    public String metalLevel;
    @SerializedName("minimum_age")
    @Expose
    public Integer minimumAge;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("nationwide")
    @Expose
    public Boolean nationwide;
    @SerializedName("plan_type")
    @Expose
    public String planType;
    @SerializedName("provider")
    @Expose
    public String provider;
    @SerializedName("links")
    @Expose
    public Links links;
    @SerializedName("cost")
    @Expose
    public Cost cost;
    @SerializedName("hios")
    @Expose
    public Hios hios;

}
