package de.stminko.employeeservice.runtime.errorhandling.boundary;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Konfigurationsklasse für automatische Fehlerbehandlung in REST-APIs.
 *
 * <p>
 * Diese Klasse ist als Spring-Konfiguration gekennzeichnet und führt einen
 * Komponentenscan im aktuellen Paket und in den Unterpaketen durch. Sie lädt
 * Eigenschaften aus der Datei {@code rest-errorhandling.properties} im Klassenpfad. Diese
 * Konfiguration ist nützlich, um eine zentrale Fehlerbehandlungslogik für eine REST-API
 * bereitzustellen, indem sie gemeinsame Eigenschaften und Beans definiert, die für die
 * Fehlerbehandlung erforderlich sind.
 * </p>
 *
 * <p>
 * Verwenden Sie diese Konfiguration, indem Sie sie in Ihren Spring-Anwendungskontext
 * einbeziehen, entweder über automatische Konfiguration oder explizite Importanweisungen
 * in Ihrer Hauptkonfigurationsklasse.
 * </p>
 *
 * @author Stéphan Minko
 */
@Configuration
@PropertySource("classpath:config/stminko-errorhandling.properties")
public class RestErrorHandlingAutoConfiguration {

}
