package de.stminko.eventservice.runtime.pagination.boundary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Custom deserializer for {@link Page} objects.
 *
 * <p>
 * This deserializer handles the conversion of JSON data into {@link Page} objects. It is
 * specifically tailored to deserialize JSON structures representing paginated data,
 * including elements such as content, page number, page size, and total elements.
 * </p>
 *
 * <p>
 * It implements {@link ContextualDeserializer} to dynamically handle various types of
 * content within the pages.
 * </p>
 *
 * @author Stéphan Minko
 * @see com.fasterxml.jackson.databind.JsonDeserializer
 * @see org.springframework.data.domain.Page
 */
class PageDeserializer extends JsonDeserializer<Page<?>> implements ContextualDeserializer {

	private static final String CONTENT = "content";

	private static final String NUMBER = "number";

	private static final String SIZE = "size";

	private static final String TOTAL_ELEMENTS = "totalElements";

	private JavaType valueType;

	/**
	 * Deserializes JSON data into a {@link Page} object.
	 *
	 * <p>
	 * This method reads JSON fields such as 'content', 'number', 'size', and
	 * 'totalElements' to construct a {@link Page} object. It is designed to work with a
	 * predefined JSON structure specific to paginated data.
	 * </p>
	 * @param jsonParser the JSON parser
	 * @param deserializationContext the context of deserialization
	 * @return a deserialized {@link Page} object
	 * @throws IOException if an error occurs during reading from the {@code jsonParser}
	 * @author Stéphan Minko
	 */
	@Override
	public Page<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException {
		final CollectionType valuesListType = deserializationContext.getTypeFactory()
			.constructCollectionType(List.class, this.valueType);

		List<?> list = new ArrayList<>();
		int pageNumber = 0;
		int pageSize = 0;
		long total = 0;
		if (jsonParser.isExpectedStartObjectToken()) {
			jsonParser.nextToken();
			if (jsonParser.hasTokenId(JsonTokenId.ID_FIELD_NAME)) {
				String propName = jsonParser.getCurrentName();
				do {
					jsonParser.nextToken();
					switch (propName) {
						case CONTENT:
							list = deserializationContext.readValue(jsonParser, valuesListType);
							break;
						case NUMBER:
							pageNumber = deserializationContext.readValue(jsonParser, Integer.class);
							break;
						case SIZE:
							pageSize = deserializationContext.readValue(jsonParser, Integer.class);
							break;
						case TOTAL_ELEMENTS:
							total = deserializationContext.readValue(jsonParser, Long.class);
							break;
						default:
							jsonParser.skipChildren();
							break;
					}
				}
				while (((propName = jsonParser.nextFieldName())) != null);
			}
			else {
				deserializationContext.handleUnexpectedToken(handledType(), jsonParser);
			}
		}
		else {
			deserializationContext.handleUnexpectedToken(handledType(), jsonParser);
		}

		// Note that Sort field of Page is ignored here.
		// Feel free to add more switch cases above to deserialize it as well.
		return new PageImpl<>(list, PageRequest.of(pageNumber, pageSize), total);
	}

	/**
	 * Creates a deserializer context for the {@link Page} type.
	 *
	 * <p>
	 * This method is called to obtain a deserializer that is contextualized to
	 * deserialize a specific generic type that the {@link Page} object will contain. It
	 * extracts the contained type information from the {@code wrapperType}.
	 * </p>
	 * @param ctxt the deserialization context
	 * @param property the property being deserialized
	 * @return a contextualized deserializer
	 */
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
		// This is the Page actually
		final JavaType wrapperType = ctxt.getContextualType();
		final PageDeserializer deserializer = new PageDeserializer();
		// This is the parameter of Page
		deserializer.valueType = wrapperType.containedType(0);
		return deserializer;
	}

}
