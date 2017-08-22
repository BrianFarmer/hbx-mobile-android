package org.dchbx.coveragehq.models.fe;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/*
    This file is part of DC.

    DC Health Link SmallBiz is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DC Health Link SmallBiz is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DC Health Link SmallBiz.  If not, see <http://www.gnu.org/licenses/>.
    This statement should go near the beginning of every source file, close to the copyright notices. When using the Lesser GPL, insert the word “Lesser” before “General” in all three places. When using the GNU AGPL, insert the word “Affero” before “General” in all three places.
*/
public class Field extends SchemaElement {

    @SerializedName("field")
    @Expose
    public String field;
    @SerializedName("label")
    @Expose
    public String label;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("options")
    @Expose
    public ArrayList<Option> options;
    @SerializedName("prereq_field")
    @Expose
    public String prereqField;
    @SerializedName("prereq_values")
    @Expose
    public ArrayList<Object> prereqValues;
    @SerializedName("summary_element_order")
    @Expose
    public Object summaryElementOrder;
    @SerializedName("mobile_only")
    @Expose
    public Object mobileOnly;
    @SerializedName("maxlength")
    @Expose
    public Integer maxlength;
    @SerializedName("default_value")
    @Expose
    public String defaultValue;
    @SerializedName("readonly")
    @Expose
    public String readonly;
    @SerializedName("optional")
    @Expose
    public String optional;
    @SerializedName("dependency_level")
    @Expose
    public Integer dependencyLevel;
    @SerializedName("dependent_fields")
    @Expose
    public ArrayList<Field> dependentFields = null;
    @SerializedName("subFields")
    @Expose
    public ArrayList<Field> subFields = null;

}
