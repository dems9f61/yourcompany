package de.stminko.eventservice.employee.entity;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Custom JSON deserializer for {@link ZonedDateTime} objects.
 *
 * <p>
 * This deserializer converts JSON string data into {@link ZonedDateTime} objects. It is
 * particularly useful for deserializing date information that is formatted in a specific
 * way, as defined by {@code UsableDateFormat.DEFAULT.getDateFormat()}.
 * </p>
 *
 * <p>
 * During deserialization, it interprets the date string as a {@link LocalDate} and then
 * converts it to a {@link ZonedDateTime} at the start of the day in UTC time zone.
 * </p>
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
