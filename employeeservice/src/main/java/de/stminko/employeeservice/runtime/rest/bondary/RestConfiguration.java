package de.stminko.employeeservice.runtime.rest.bondary;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.history.Revision;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * configuration class for REST-related settings. It's responsible for customizing the
 * Jackson Object Mapper used by Spring MVC to serialize and deserialize JSON data.
 *
 * <p>
 * It includes custom serializer configurations for certain types like {@link PageImpl}
 * and {@link Revision} to tailor the JSON output according to specific application needs.
 * </p>
 *
 * @author Stéphan Minko
 */
@Slf4j
@Configuration
public class RestConfiguration {

	/**
	 * customizes the Jackson2ObjectMapperBuilder with application-specific serializers.
	 * <p>
	 * This method registers custom serializers for types like {@link PageImpl} and
	 * {@link Revision} to control how these types are serialized to JSON. This
	 * customization is essential for ensuring the JSON output conforms to the
	 * application's data structure and front-end requirements.
	 * </p>
	 * @param pageSerializer the serializer to use for {@link PageImpl} objects.
	 * @param revisionSerializer the serializer to use for {@link Revision} objects.
	 * @return a {@link Jackson2ObjectMapperBuilderCustomizer} that customizes the JSON
	 * mapping.
	 *
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer restSupportBuilderCustomizer(PageSerializer pageSerializer,
			RevisionSerializer revisionSerializer) {
		return (Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) -> {
			log.info(
					"Registering Custom Serializers for: [{}], [{}] with Springs default Jackson2ObjectMapperBuilder ...",
					PageImpl.class.getSimpleName(), Revision.class.getSimpleName());
			jackson2ObjectMapperBuilder.serializerByType(PageImpl.class, pageSerializer);
			jackson2ObjectMapperBuilder.serializerByType(Revision.class, revisionSerializer);
		};
	}

}
