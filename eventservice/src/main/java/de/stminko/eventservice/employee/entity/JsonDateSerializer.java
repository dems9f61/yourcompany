package de.stminko.eventservice.employee.entity;

import java.io.IOException;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom JSON serializer for {@link ZonedDateTime} objects.
 *
 * <p>This serializer is responsible for converting {@link ZonedDateTime} objects into a JSON string format.
 * It specifically formats the datetime as a local date string, omitting time and timezone information.</p>
 *
 * <p>The serializer extracts the local date part of the {@link ZonedDateTime} and converts it to a string
 * representation, which is then written as JSON output.</p>
 *
 * @author Stéphan Minko
 */
public class JsonDateSerializer extends JsonSerializer<ZonedDateTime> {

    @Override
    public void serialize(ZonedDateTime value, JsonGenerator jsonGenerator, SerializerProvider provider)
            throws IOException {
        jsonGenerator.writeString(value.toLocalDate().toString());
    }

}
