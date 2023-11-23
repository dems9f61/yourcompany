package de.stminko.employeeservice.runtime.rest.bondary;

/**
 * Simple interfaces used to declare REST oriented JsonViews on the Base Entities provided
 * by this library. Enables a single Entity domain over Server and REST layers of an
 * application
 *
 * @author Stéphan Minko
 */
public interface DataView {

    /**
     * Interface for HTTP GET Requests.
     */
    interface GET extends DataView {

    }

    /**
     * Interface for HTTP POST Requests.
     */
    interface POST extends DataView {

    }

    /**
     * Interface for HTTP PUT Requests.
     */

    interface PUT extends DataView {

    }

    /**
     * Interface for HTTP PATCH Requests.
     */
    interface PATCH extends DataView {

    }

}
