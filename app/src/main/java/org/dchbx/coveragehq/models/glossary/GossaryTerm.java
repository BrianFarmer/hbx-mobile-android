package org.dchbx.coveragehq.models.glossary;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GossaryTerm {

    @SerializedName("term")
    @Expose
    public String term;
    @SerializedName("description")
    @Expose
    public String description;

}