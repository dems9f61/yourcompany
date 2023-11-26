package de.stminko.employeeservice.runtime.amqp.boundary;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.validation.annotation.Validated;

/**
 * configuration class for setting up AMQP (Advanced Message Queuing Protocol) with
 * RabbitMQ in Spring. This class defines beans for RabbitTemplate, message converters,
 * retry policies, and other related settings.
 * <p>
 * Annotations: - @Configuration: Marks this class as a source of bean definitions for the
 * application context. - @ConfigurationProperties: Binds and validates external
 * configurations, prefixed with "amqp". - @Validated: Ensures that properties are
 * validated. - @Data: Lombok annotation to generate getters, setters, equals, hashCode,
 * and toString methods.
 * <p>
 * Beans: - rabbitTemplate: Configures RabbitTemplate with connection factory, message
 * converter, and post processors. - messagePostProcessor: Sets a timestamp header for
 * AMQP messages. - retryTemplate: Defines retry behavior with a simple policy and
 * exponential backoff policy. - jsonMessageConverter: Configures Jackson's JSON message
 * converter with a custom class mapper and object mapper. - classMapper: Provides a
 * default Jackson Java type mapper for AMQP messages.
 * <p>
 * Properties: - exchangeName: The name of the exchange used in RabbitMQ. - routingKey:
 * The routing key for messages.
 * <p>
 * Example Usage: <pre>
 * &#64;Autowired
 * private AmqpConfig amqpConfig;
 *
 * // Use amqpConfig's methods and properties
 * </pre>
 *
 * @author Stéphan Minko
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "amqp")
@Validated
public class AmqpConfig {

	/**
	 * The name of the AMQP exchange used for message routing in RabbitMQ. This property
	 * is essential for defining the destination exchange where messages will be sent.
	 * <p>
	 * Note: The specific type of exchange (e.g., direct, topic, fanout, headers) and its
	 * configuration are managed within RabbitMQ and are outside the scope of this
	 * property.
	 */
	@NotBlank
	private String exchangeName;

	/**
	 * The routing key used in conjunction with the exchange to determine how messages are
	 * routed to queues in RabbitMQ. This property specifies the routing key to be used
	 * when sending messages through the RabbitTemplate. Note: The effectiveness and
	 * behavior of the routing key depend on the exchange type and the bindings
	 * established in RabbitMQ.
	 */
	@NotBlank
	private String routingKey;

	/**
	 * Configures and provides a {@link RabbitTemplate} bean for AMQP messaging. The
	 * RabbitTemplate is configured with a ConnectionFactory, a custom MessageConverter,
	 * and a MessagePostProcessor.
	 * <p>
	 * This template handles the operations for sending and receiving messages with
	 * RabbitMQ, including retries, message conversion, and pre-publish processing.
	 * @param connectionFactory the factory for creating connections to the RabbitMQ
	 * broker.
	 * @param messageConverter the converter used for converting between Java objects and
	 * AMQP messages.
	 * @param messagePostProcessor processor that modifies messages before they are
	 * published.
	 * @return a configured instance of {@link RabbitTemplate}.
	 */
	@Bean
	@Primary
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
			Jackson2JsonMessageConverter messageConverter, MessagePostProcessor messagePostProcessor) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setRetryTemplate(retryTemplate());
		template.setMessageConverter(messageConverter);
		template.setBeforePublishPostProcessors(messagePostProcessor);
		return template;
	}

	/**
	 * Provides a {@link MessagePostProcessor} that adds a timestamp header to each AMQP
	 * message. This post-processor is used in conjunction with RabbitTemplate to
	 * automatically add a timestamp indicating when the message was sent.
	 * @return a {@link MessagePostProcessor} that modifies the message properties.
	 */
	@Bean
	public MessagePostProcessor messagePostProcessor() {
		return (Message message) -> {
			MessageProperties messageProperties = message.getMessageProperties();
			messageProperties.setHeader("timestamp", ZonedDateTime.now());
			return message;
		};
	}

	/**
	 * Defines and configures a {@link RetryTemplate} for use with RabbitMQ operations.
	 * This template specifies the retry policy and backoff policy for operations that may
	 * require retrying, such as message sending in case of network issues or broker
	 * unavailability.
	 * <p>
	 * The retry policy is a simple retry policy with a maximum number of attempts. The
	 * backoff policy uses an exponential strategy to increase the delay between retries.
	 * @return a configured {@link RetryTemplate} instance.
	 */
	@Bean
	public RetryTemplate retryTemplate() {
		RetryTemplate retryTemplate = new RetryTemplate();

		SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
		retryPolicy.setMaxAttempts(5);
		retryTemplate.setRetryPolicy(retryPolicy);

		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(500);
		backOffPolicy.setMultiplier(10.0);
		backOffPolicy.setMaxInterval(10000);
		retryTemplate.setBackOffPolicy(backOffPolicy);

		return retryTemplate;
	}

	/**
	 * Configures and provides a {@link Jackson2JsonMessageConverter} for AMQP messaging.
	 * This converter is used to serialize and deserialize Java objects to and from JSON
	 * for RabbitMQ messages.
	 * <p>
	 * It uses a custom {@link ClassMapper} for type mapping and an {@link ObjectMapper}
	 * for JSON processing, which is configured to handle Java time objects properly.
	 * @param classMapper the mapper used for class type information in messages.
	 * @param objectMapper the Jackson object mapper configured for JSON processing.
	 * @return a configured instance of {@link Jackson2JsonMessageConverter}.
	 * @author Stéphan Minko
	 */
	@Bean
	public Jackson2JsonMessageConverter jsonMessageConverter(ClassMapper classMapper, ObjectMapper objectMapper) {
		objectMapper.registerModule(new JavaTimeModule());
		Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter(objectMapper);
		jackson2JsonMessageConverter.setClassMapper(classMapper);
		return jackson2JsonMessageConverter;
	}

	/**
	 * Provides a {@link ClassMapper} for use with Jackson JSON message conversion in AMQP
	 * messaging. This mapper is used by the {@link Jackson2JsonMessageConverter} to
	 * include and process class type information within AMQP messages, facilitating the
	 * correct serialization and deserialization of objects.
	 * @return an instance of {@link DefaultJackson2JavaTypeMapper}, a default
	 * implementation of ClassMapper.
	 */
	@Bean
	public ClassMapper classMapper() {
		return new DefaultJackson2JavaTypeMapper();
	}

}
