package de.stminko.eventservice;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.HttpMessageConversionException;

/**
 * custom implementation of {@link PageImpl} to handle pagination in a test application.
 * This class extends {@link PageImpl} and provides additional constructors for JSON
 * deserialization.
 * <p>
 * This class prevents from getting a {@link HttpMessageConversionException}, since
 * PageImpl class has no default constructor and no @JsonCreator annotation in any of the
 * existing constructors.
 * </p>
 *
 * @param <T> the type of object in the page content
 */
public class CustomPageImpl<T> extends PageImpl<T> {

	/**
	 * creates a {@code CustomPageImpl} object with the specified content and page
	 * details. This constructor is primarily used for JSON deserialization.
	 * @param content the content of the page
	 * @param number the current page number
	 * @param size the size of the page
	 * @param totalElements the total number of elements across all pages
	 * @param pageable the pageable JSON node
	 * @param last flag indicating if this is the last page
	 * @param totalPages the total number of pages
	 * @param sort the sort JSON node
	 * @param numberOfElements the number of elements in the current page
	 */
	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public CustomPageImpl(@JsonProperty("content") List<T> content, @JsonProperty("number") int number,
			@JsonProperty("size") int size, @JsonProperty("totalElements") Long totalElements,
			@JsonProperty("pageable") JsonNode pageable, @JsonProperty("last") boolean last,
			@JsonProperty("totalPages") int totalPages, @JsonProperty("sort") JsonNode sort,
			@JsonProperty("numberOfElements") int numberOfElements) {
		super(content, PageRequest.of(number, 1), 10);
	}

	/**
	 * creates a {@code CustomPageImpl} object with the specified content, pageable, and
	 * total elements count. This constructor is used for creating a page implementation
	 * from existing data.
	 * @param content the content of the page
	 * @param pageable the pagination information
	 * @param total the total number of elements across all pages
	 */
	public CustomPageImpl(List<T> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	/**
	 * creates a {@code CustomPageImpl} object with the specified content. This
	 * constructor is used when only the content is known, and no pagination details are
	 * provided.
	 * @param content the content of the page
	 */
	public CustomPageImpl(List<T> content) {
		super(content);
	}

	/**
	 * creates an empty {@code CustomPageImpl} object. This constructor is used to create
	 * an empty page.
	 */
	public CustomPageImpl() {
		super(new ArrayList<>());
	}

}
