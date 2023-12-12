package de.stminko.employeeservice.runtime.rest.bondary;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.stereotype.Component;

/**
 * dedicated Jackson serializer for Revision information.
 *
 * @author Thomas Zirke
 */
@Slf4j
@Component
public class RevisionSerializer extends StdSerializer<Revision> {

	public RevisionSerializer() {
		super(Revision.class);
	}

	@Override
	public void serialize(Revision value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
		final JsonGenerator jsonGen = provider.getGenerator();
		final RevisionMetadata<?> metadata = value.getMetadata();
		final Optional<?> revisionNumber = value.getRevisionNumber();
		final Object entity = value.getEntity();
		jsonGen.writeStartObject();
		jsonGen.writeObjectField("entity", entity);
		jsonGen.writeObjectField("metadata", metadata);
		if (revisionNumber.isPresent()) {
			jsonGen.writeFieldName("revisionNumber");
			jsonGen.writeNumber(Long.parseLong(revisionNumber.get().toString()));
		}
		jsonGen.writeEndObject();
	}

}
