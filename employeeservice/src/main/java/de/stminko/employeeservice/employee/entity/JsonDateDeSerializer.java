package de.stminko.employeeservice.employee.entity;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * custom deserializer for converting JSON date strings into {@link ZonedDateTime}
 * objects. Extends {@link JsonDeserializer} and utilizes {@link DateTimeFormatter} for
 * parsing date strings according to the specified format in {@link UsableDateFormat}.
 * <p>
 * Method: - deserialize: Parses a JSON string and converts it into a
 * {@link ZonedDateTime}. Assumes the date is in local date format and converts it to the
 * start of the day in UTC.
 * <p>
 * Usage: Apply this deserializer to ZonedDateTime fields in data transfer objects using
 * Jackson's {@code @JsonDeserialize} annotation.
 *
 * @author Stéphan Minko
 */
public class JsonDateDeSerializer extends JsonDeserializer<ZonedDateTime> {

	@Override
	public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(UsableDateFormat.DEFAULT.getDateFormat());
		LocalDate localDate = LocalDate.parse(jsonParser.getValueAsString(), dateFormatter);
		return localDate.atStartOfDay(ZoneOffset.UTC);
	}

}
