package org.dchbx.coveragehq.models.services;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Service {
    @SerializedName("service")
    @Expose
    public String service;
    @SerializedName("copay")
    @Expose
    public String copay;
    @SerializedName("coinsurance")
    @Expose
    public String coinsurance;
}