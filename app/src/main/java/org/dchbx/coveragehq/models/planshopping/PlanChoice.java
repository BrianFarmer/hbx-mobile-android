
package org.dchbx.coveragehq.models.planshopping;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.dchbx.coveragehq.models.fe.PersonForCoverage;
import org.joda.time.LocalDate;

import java.util.ArrayList;

public class PlanChoice {
    @SerializedName("eaid")
    @Expose
    public String eaid;
    @SerializedName("plan_hios_id")
    @Expose
    public String planHiosId;
    @SerializedName("plan_name")
    @Expose
    public String planName;
    @SerializedName("effective_date")
    @Expose
    public LocalDate effective_date;
    @SerializedName("monthly_premium")
    @Expose
    public double monthly_premium;
    @SerializedName("applicants")
    @Expose
    public ArrayList<PersonForCoverage> applicants;

}
