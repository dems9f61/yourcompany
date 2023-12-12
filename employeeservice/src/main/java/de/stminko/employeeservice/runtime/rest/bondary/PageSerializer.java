package de.stminko.employeeservice.runtime.rest.bondary;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Component;

/**
 * custom serializer for Spring Data's {@link Page} interface. this serializer customizes
 * the JSON output for {@link Page} objects, which are commonly used in Spring Data to
 * represent paginated data.
 *
 * <p>
 * It provides a detailed JSON structure including metadata about the pagination such as
 * page size, total elements, and whether it's the first or last page, among other
 * details.
 * </p>
 *
 * @author Stéphan Minko
 */
@Slf4j
@Component
public class PageSerializer extends StdSerializer<Page> {

	/**
	 * Default constructor for PageSerializer.
	 */
	public PageSerializer() {
		super(Page.class);
	}

	/**
	 * Serializes a {@link Page} object to JSON. This method defines the JSON structure
	 * for the serialized Page.
	 * @param page the page object to serialize.
	 * @param jsonGenerator the generator used to write the JSON content.
	 * @param provider the serializer provider.
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void serialize(Page page, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
		final JsonGenerator jsonGen = provider.getGenerator();
		final ObjectMapper mapper = (ObjectMapper) jsonGen.getCodec();
		jsonGen.writeStartObject();
		jsonGen.writeFieldName("size");
		jsonGen.writeNumber(page.getSize());
		jsonGen.writeFieldName("number");
		jsonGen.writeNumber(page.getNumber());
		jsonGen.writeFieldName("totalElements");
		jsonGen.writeNumber(page.getTotalElements());
		jsonGen.writeFieldName("last");
		jsonGen.writeBoolean(page.isLast());
		jsonGen.writeFieldName("totalPages");
		jsonGen.writeNumber(page.getTotalPages());
		jsonGen.writeObjectField("sort", page.getSort());
		jsonGen.writeFieldName("first");
		jsonGen.writeBoolean(page.isFirst());
		jsonGen.writeFieldName("numberOfElements");
		jsonGen.writeNumber(page.getNumberOfElements());
		jsonGen.writeFieldName("pageable");
		jsonGen.writeObject(page.getPageable());
		final List<?> content = page.getContent();
		final Object firstEntry = content.stream().findFirst().orElse(null);
		if (firstEntry instanceof Revision) {
			jsonGen.writeObjectField("content", content);
		}
		else {
			jsonGen.writeFieldName("content");
			jsonGen.writeRawValue(mapper.writerWithView(provider.getActiveView()).writeValueAsString(content));
		}
		jsonGen.writeEndObject();
	}

}
