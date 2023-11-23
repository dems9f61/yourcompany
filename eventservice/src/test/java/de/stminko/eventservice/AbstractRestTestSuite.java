package de.stminko.eventservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public abstract class AbstractRestTestSuite extends AbstractIntegrationTestSuite {

	@Autowired
	protected TestRestTemplate testRestTemplate;

	protected HttpHeaders defaultHttpHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return httpHeaders;
	}

}
