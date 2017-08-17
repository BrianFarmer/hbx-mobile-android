package org.dchbx.coveragehq;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.HashMap;
import java.util.UUID;

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
public class ApplicationUtilities {
    public static String Id = "eapersonid";
    public static String DateOfBirth = "persondob";
    public static String FirstName = "personfirstname";
    public static String LastName = "personlastname";

    public static int getAge(HashMap<String, Object> person){
        return Period.fieldDifference(new LocalDate((String)person.get(DateOfBirth)), LocalDate.now()).getYears();
    }

    public static String getFullName(HashMap<String, Object> person){
        return person.get(FirstName) + " " + person.get(LastName);
    }

    public static HashMap<String, Object> getNewPerson() {
        UUID uuid = UUID.randomUUID();
        String eaPersonId = uuid.toString();
        HashMap<String, Object> person = new HashMap<>();
        person.put(Id, eaPersonId);
        return person;
    }
}
