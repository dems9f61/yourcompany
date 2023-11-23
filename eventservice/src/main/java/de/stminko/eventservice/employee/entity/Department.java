package de.stminko.eventservice.employee.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

/**
 * Represents a department entity.
 *
 * <p>This class models the department data. It is designed to be serializable to allow for easy storage and retrieval
 * from various data streams, and it is integrated with Jackson for JSON processing.</p>
 *
 * @author Stéphan Minko
 */
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Department implements Serializable {

    private long id;

    private String departmentName;

}
