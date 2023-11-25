package de.stminko.eventservice.runtime.pagination.boundary;

import com.fasterxml.jackson.databind.module.SimpleModule;

import org.springframework.data.domain.Page;

/**
 * Jackson module for custom deserialization of {@link Page} objects.
 *
 * <p>
 * This class extends {@link SimpleModule} to provide custom deserialization behavior for
 * {@link Page} objects. It is used to register the custom deserializer,
 * {@link PageDeserializer}, to handle the JSON deserialization of Spring Data's
 * {@link Page} type, especially for complex scenarios involving pagination data.
 * </p>
 *
 * <p>
 * Typically, this module is added to the
 * {@link com.fasterxml.jackson.databind.ObjectMapper} to enable the custom
 * deserialization process within the application's context.
 * </p>
 *
 * @author Stéphan Minko
 */
public class PageModule extends SimpleModule {

	public PageModule() {
		addDeserializer(Page.class, new PageDeserializer());
	}

}
