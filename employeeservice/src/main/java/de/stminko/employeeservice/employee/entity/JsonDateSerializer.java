package de.stminko.employeeservice.employee.entity;

import java.io.IOException;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * custom serializer for converting {@link ZonedDateTime} objects to JSON date strings.
 * Extends {@link JsonSerializer} and is responsible for formatting {@link ZonedDateTime}
 * objects into string representations suitable for JSON.
 * <p>
 * Method: - serialize: Converts a {@link ZonedDateTime} object into a string
 * representation. The output format is the local date in ISO-8601 format (yyyy-MM-dd).
 * <p>
 * Usage: This serializer can be applied to ZonedDateTime fields in data transfer objects
 * using Jackson's {@code @JsonSerialize} annotation to specify custom serialization
 * behavior.
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
