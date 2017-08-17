package org.dchbx.coveragehq.models.fe;

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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class FinancialAssistanceApplication {
    @SerializedName("Person")
    @Expose
    public ArrayList<HashMap<String, Object>> person;
    @SerializedName("Relationship")
    @Expose
    public ArrayList<HashMap<String, Object>> relationship;
    @SerializedName("Attestation")
    @Expose
    public ArrayList<HashMap<String, Object>> attestation;

    public FinancialAssistanceApplication(){
        person = new ArrayList<>();
        relationship = new ArrayList<>();
        attestation = new ArrayList<>();
    }
}
