package org.dchbx.coveragehq.models.planshopping;

import org.joda.time.LocalDate;

import java.util.ArrayList;

/**
 * Created by plast on 5/9/2017.
 */

public class PlanShoppingParameters {
    public String coverageKind;
    public String activeYear;
    public String csrKind;
    public ArrayList<Integer> ages;
    public double electedAptcAmount;
    public LocalDate effectiveOn;
}
