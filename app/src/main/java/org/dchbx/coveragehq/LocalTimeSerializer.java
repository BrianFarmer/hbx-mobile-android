package org.dchbx.coveragehq;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

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
public class LocalTimeSerializer implements JsonDeserializer<LocalTime>, JsonSerializer<LocalTime>
{

    private static final DateTimeFormatter TIME_FORMAT = ISODateTimeFormat.timeNoMillis();

    @Override
    public LocalTime deserialize(final JsonElement je, final Type type,
                                 final JsonDeserializationContext jdc) throws JsonParseException
    {
        final String dateAsString = je.getAsString();
        if (dateAsString.length() == 0)
        {
            return null;
        }
        else
        {
            return TIME_FORMAT.parseLocalTime(dateAsString);
        }
    }

    @Override
    public JsonElement serialize(final LocalTime src, final Type typeOfSrc,
                                 final JsonSerializationContext context)
    {
        String retVal;
        if (src == null)
        {
            retVal = "";
        }
        else
        {
            retVal = TIME_FORMAT.print(src);
        }
        return new JsonPrimitive(retVal);
    }

}
