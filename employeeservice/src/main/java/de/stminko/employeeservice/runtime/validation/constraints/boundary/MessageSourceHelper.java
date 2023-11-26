package de.stminko.employeeservice.runtime.validation.constraints.boundary;

import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper component to simplify accessing messages from a {@link MessageSource}. This
 * component provides methods to retrieve messages based on a code and optional arguments,
 * automatically considering the current Locale from the {@link LocaleContextHolder}.
 *
 * <p>
 * This class utilizes a custom {@link MessageSource} for localizing messages. If no
 * Locale is available in the {@link LocaleContextHolder}, the default system Locale is
 * used instead.
 * </p>
 *
 * @author Stéphan Minko
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSourceHelper {

	private final MessageSource customValidationMessageSource;

	/**
	 * Retrieves a localized message based on the provided message code and arguments.
	 * Uses the current Locale from the {@link LocaleContextHolder}, or if unavailable,
	 * the default system Locale.
	 * @param code The message code used to identify the message in the MessageSource.
	 * @param args An array of objects that serve as arguments for message formatting; can
	 * be null.
	 * @return The localized message as a {@link String}.
	 */
	public String getMessage(String code, Object... args) {
		log.debug("getMessage(code= [{}], args= [{}])", code, ArrayUtils.toString(args));
		LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
		if ((localeContext != null) && (localeContext.getLocale() != null)) {
			return this.customValidationMessageSource.getMessage(code, args, localeContext.getLocale());
		}
		else {
			return this.customValidationMessageSource.getMessage(code, args, Locale.getDefault());
		}
	}

	/**
	 * Retrieves a localized message for the specified message code using the current
	 * Locale. This method is a convenience overload of
	 * {@link #getMessage(String, Object[])} for cases where no formatting arguments are
	 * needed. It utilizes the current Locale from the {@link LocaleContextHolder}. If the
	 * current Locale is not available, the default system Locale is used as a fallback.
	 *
	 * <p>
	 * This method is ideal for retrieving simple messages without placeholders for
	 * dynamic content.
	 * </p>
	 * @param code The unique identifier for the desired message in the message source.
	 * @return A localized String corresponding to the message code.
	 */
	public String getMessage(String code) {
		log.debug("getMessage(code= [{}])", code);
		return getMessage(code, null);
	}

}
