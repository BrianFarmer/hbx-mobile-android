package org.dchbx.coveragehq;

import com.google.gson.Gson;

import org.dchbx.coveragehq.models.planshopping.PlanShoppingParameters;

/**
 * Created by plast on 5/9/2017.
 */

public class JsonSerializer {
    public static String serialize(PlanShoppingParameters planShoppingParameters){
        Gson gson = new Gson();
        return gson.toJson(planShoppingParameters);
    }
}
