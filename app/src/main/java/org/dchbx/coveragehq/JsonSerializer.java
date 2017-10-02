package org.dchbx.coveragehq;

import com.google.gson.Gson;

import org.joda.time.base.BaseDateTime;

/**
 * Created by plast on 5/9/2017.
 */

public class JsonSerializer<D extends BaseDateTime> {
    public static String serialize(PlanShoppingParameters planShoppingParameters){
        Gson gson = new Gson();
        return gson.toJson(planShoppingParameters);
    }
}
