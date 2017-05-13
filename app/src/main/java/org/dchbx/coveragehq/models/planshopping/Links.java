
package org.dchbx.coveragehq.models.planshopping;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Links {

    @SerializedName("provider_directory")
    @Expose
    public String providerDirectory;
    @SerializedName("rx_formulary")
    @Expose
    public String rxFormulary;
    @SerializedName("services_rates")
    @Expose
    public String servicesRates;
    @SerializedName("summary_of_benefits")
    @Expose
    public String summaryOfBenefits;
    @SerializedName("carrier_logo")
    @Expose
    public String carrierLogo;

}
