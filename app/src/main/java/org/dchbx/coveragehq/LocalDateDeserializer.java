package org.dchbx.coveragehq;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import org.joda.time.LocalDate;

import java.lang.reflect.Type;

/**
 * Created by plast on 3/31/2017.
 */
public class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        if (primitive.isString()){
            String s = primitive.toString();
            if (s.length() == 0){
                return null;
            }
        }
        return new LocalDate(primitive.getAsString());
    }
}
