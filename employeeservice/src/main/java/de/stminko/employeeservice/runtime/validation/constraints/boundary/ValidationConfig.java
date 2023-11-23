package de.stminko.employeeservice.runtime.validation.constraints.boundary;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuration class for custom validation message sources in Spring.
 *
 * <p>This configuration class provides custom beans for validation purposes in a Spring application.
 * It sets up a {@code LocalValidatorFactoryBean} and a {@code MessageSource} to customize validation messages.</p>
 *
 * <p>The {@code customValidationFactoryBean} bean is configured to use a custom message source
 * for validation messages, allowing for more control over validation message customization and localization.</p>
 *
 * <p>The {@code customValidationMessageSource} bean defines a {@code ReloadableResourceBundleMessageSource}
 * that is set to load messages from a specified path. This path is stored in the {@code VALIDATION_MESSAGES_CATALOG_NAME} constant.
 * This setup ensures that validation messages are easily manageable and can be updated without restarting the application.</p>
 *
 * <p>Use this configuration to customize the validation messages in your application,
 * particularly if you need to support specific localization or formatting requirements.</p>
 *
 * @author Stéphan Minko
 */
@Configuration
public class ValidationConfig {

    static final String VALIDATION_MESSAGES_CATALOG_NAME = "classpath:stminko-validation-messages";

    @Bean
    LocalValidatorFactoryBean customValidationFactoryBean(MessageSource customValidationMessageSource) {
        LocalValidatorFactoryBean retVal = new LocalValidatorFactoryBean();
        retVal.setValidationMessageSource(customValidationMessageSource);
        return retVal;
    }

    @Bean
    MessageSource customValidationMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Set our own validation message property so we only have english version
        List<String> finalMessageCatalogList = new ArrayList<>();
        finalMessageCatalogList.add(VALIDATION_MESSAGES_CATALOG_NAME);
        messageSource.setBasenames(finalMessageCatalogList.toArray(new String[0]));
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        messageSource.setDefaultLocale(Locale.ENGLISH);
        return messageSource;
    }

}
