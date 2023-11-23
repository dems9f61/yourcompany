package de.stminko.eventservice.runtime.errorhandling.entity;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.springframework.http.HttpStatus;

/**
 * This is the Object that is JSON Serialized by all RestControllers in case of any
 * Exception.
 *
 * @author Stéphan Minko
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorInfo {

	private String url;

	private String urlQueryString;

	private String httpMethod;

	private HttpStatus httpStatus;

	private int httpStatusCode;

	private ZonedDateTime errorDateTime;

	private String errorMessage;

}
