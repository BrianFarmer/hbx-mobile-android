
package org.dchbx.coveragehq.models.planshopping;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Cost {

    @SerializedName("deductible_text")
    @Expose
    public String deductibleText;
    @SerializedName("deductible")
    @Expose
    public Double deductible;
    @SerializedName("monthly_premium")
    @Expose
    public Double monthlyPremium;

}
