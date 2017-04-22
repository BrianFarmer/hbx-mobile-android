package org.dchbx.coveragehq.models.roster;

import com.google.gson.annotations.Expose;

/**
 * Created by plast on 4/14/2017.
 */

public class SummaryOfBenefits {
    @Expose
    public String service;
    @Expose
    public String copay;
    @Expose
    public String coinsurance;
}
