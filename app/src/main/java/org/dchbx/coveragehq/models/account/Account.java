package org.dchbx.coveragehq.models.account;

import android.util.Log;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.io.Serializable;

import static org.dchbx.coveragehq.Utilities.DateAsString;

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
public class Account implements Serializable {
    private static String TAG = "Account";

    public Account(){
        this.address = new Address();
        this.gender = "female";
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setGenderToMale() {
        this.gender = "male";
    }

    public void setGenderToFemale() {
        this.gender = "female";
    }

    public boolean isMale(){
        if (gender == null){
            return false;
        }
        return gender.compareToIgnoreCase("male") == 0;
    }

    public String getGender(){
        return gender;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getBirthdateString() {
        if (birthdate != null){
            return DateAsString(birthdate);
        } else {
            return "";
        }
    }

    public boolean setBirthdateString(String birthdateString) {
        if (birthdateString == null
            || birthdateString.length() != 10){
            this.birthdate = null;
            return false;
        } else {
            try {
                this.birthdate = LocalDate.parse(birthdateString, DateTimeFormat.forPattern("MM/dd/yyyy"));
            } catch (Throwable t){
                Log.e(TAG, "Exception setting birthdate");
            }
            return true;
        }
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isIncarcerated() {
        return incarcerated;
    }

    public void setIncarcerated(boolean incarcerated) {
        this.incarcerated = incarcerated;
    }

    public void setExperianConsent(boolean consented){
        experianConsent = consented;
    }

    public String firstName;
    public String lastName;
    public String emailAddress;
    public String accountName;
    public String password;
    public LocalDate birthdate;
    public String ssn;
    public Address address;
    public boolean incarcerated;
    public boolean experianConsent;
    public String gender;

    public String getFirstName() {
        return this.firstName;
    }
    public String getLastName() {
        return this.lastName;
    }

}
