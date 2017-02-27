package org.dchbx.coveragehq.models.roster;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalDate;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Enrollment {

    @SerializedName("start_on")
    @Expose
    public LocalDate startOn;
    @SerializedName("health")
    @Expose
    public Health health;
    @SerializedName("dental")
    @Expose
    public Health dental;

}