package org.dchbx.coveragehq;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

/**
 * Created by plast on 3/31/2017.
 */
public class DateTimeDeserializer implements JsonDeserializer<DateTime>, JsonSerializer<DateTime>

{
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        if (primitive.isString()){
            String s = primitive.toString();
            if (s.length() == 0){
                return null;
            }
        }
        return new DateTime(primitive.getAsString());
    }

    @Override
    public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
        String retVal;
        if (src == null) {
            retVal = "";
        }
        else {
            final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
            retVal = fmt.print(src);
        }
        return new JsonPrimitive(retVal);
    }
}
