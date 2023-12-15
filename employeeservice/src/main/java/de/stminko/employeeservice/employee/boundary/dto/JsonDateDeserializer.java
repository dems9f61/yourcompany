package de.stminko.employeeservice.employee.boundary.dto;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import de.stminko.employeeservice.runtime.SpringContextProvider;
import de.stminko.employeeservice.runtime.validation.constraints.boundary.MessageSourceHelper;

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
public final class JsonDateDeserializer extends JsonDeserializer<ZonedDateTime> {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter
		.ofPattern(UsableDateFormat.DEFAULT.getDateFormat());

	@Override
	public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException {
		String dateValue = jsonParser.getValueAsString();
		try {
			LocalDate localDate = LocalDate.parse(dateValue, FORMATTER);
			return localDate.atStartOfDay(ZoneOffset.UTC);
		}
		catch (DateTimeParseException caught) {
			MessageSourceHelper messageSourceHelper = SpringContextProvider.getApplicationContext()
				.getBean(MessageSourceHelper.class);
			String errorMessage = messageSourceHelper.getMessage("errors.date.not-parseable", dateValue,
					UsableDateFormat.DEFAULT.getDateFormat());
			throw InvalidFormatException.from(jsonParser, errorMessage, jsonParser.getText(), ZonedDateTime.class);
		}

	}

}
