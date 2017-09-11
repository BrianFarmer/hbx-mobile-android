package org.dchbx.coveragehq.models.fe;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

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
public class Schema extends SchemaElement {

    @Expose
    @SerializedName("Relationship")
    public ArrayList<Field> Relationship;
    @Expose
    @SerializedName("Attestation")
    public ArrayList<Field> Attestation;
    @Expose
    @SerializedName("Person")
    public ArrayList<Field> Person;

    public enum FieldTypes {
        text,
        id,
        numeric,
        date,
        dropdown,
        multidropdown,
        section,
        ssn,
        yesnoradio,
        zip,
        hardwired,
        idgen
    }

    public static HashMap<String, FieldTypes> fieldTypes = initFieldStypes();

    private static HashMap<String, FieldTypes> initFieldStypes(){
        HashMap<String, FieldTypes> hashMap = new HashMap<>();
        hashMap.put("text", FieldTypes.text);
        hashMap.put("id", FieldTypes.id);
        hashMap.put("numeric", FieldTypes.numeric);
        hashMap.put("date", FieldTypes.date);
        hashMap.put("dropdown", FieldTypes.dropdown);
        hashMap.put("multidropdown", FieldTypes.multidropdown);
        hashMap.put("section", FieldTypes.section);
        hashMap.put("ssn", FieldTypes.ssn);
        hashMap.put("yesnoradio", FieldTypes.yesnoradio);
        hashMap.put("zip", FieldTypes.zip);
        hashMap.put("hardwired", FieldTypes.hardwired);
        hashMap.put("idgen", FieldTypes.idgen);
        return hashMap;
    }

}