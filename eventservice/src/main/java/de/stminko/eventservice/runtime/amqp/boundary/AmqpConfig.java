package de.stminko.eventservice.runtime.amqp.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.stminko.eventservice.runtime.pagination.boundary.PageModule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for AMQP (Advanced Message Queuing Protocol) setup.
 *
 * <p>
 * This class configures various AMQP resources such as queues, exchanges, bindings, and
 * listeners for RabbitMQ message handling. It defines necessary beans and settings for
 * efficient message processing in the application.
 * </p>
 *
 * <p>
 * Marked with {@code @Configuration} to indicate that it is a source of bean definitions.
 * The {@code @ConfigurationProperties(prefix = "amqp")} annotation indicates that its
 * fields are bound to properties prefixed with "amqp".
 * </p>
 *
 * @author Stéphan Minko
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "amqp")
@Validated
public class AmqpConfig {

	private static final String ERROR = "error";

	private static final String DEAD_LETTER = "deadLetter";

	/**
	 * Exchange name for AMQP messaging.
	 */
	@NotBlank
	private String exchangeName;

	/**
	 * Queue name for AMQP messaging.
	 */
	@NotBlank
	private String queueName;

	/**
	 * Routing key for AMQP messaging.
	 */
	@NotBlank
	private String routingKey;

	/**
	 * Minimum number of concurrent consumers for RabbitMQ.
	 */
	@Min(value = 1)
	private int concurrentConsumers;

	/**
	 * Maximum number of concurrent consumers for RabbitMQ.
	 */
	@Max(value = 20)
	private int maxConcurrentConsumers;

	/**
	 * Connection factory for creating connections to the RabbitMQ broker.
	 */
	private final ConnectionFactory connectionFactory;

	/**
	 * Constructor for AmqpConfig class.
	 * <p>
	 * This constructor initializes the AmqpConfig class with an instance of
	 * {@link ConnectionFactory}. The ConnectionFactory is used to establish connections
	 * to an AMQP broker (such as RabbitMQ) and is essential for configuring AMQP
	 * communication within a Spring application.
	 * </p>
	 * @param connectionFactory the ConnectionFactory instance used for creating
	 * connections to the AMQP broker. It is a crucial component in setting up the AMQP
	 * messaging infrastructure.
	 */
	@Autowired
	public AmqpConfig(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * Creates a {@link MappingJackson2HttpMessageConverter} bean with custom
	 * {@link ObjectMapper}. This converter is used for serializing and deserializing
	 * message payloads.
	 * @param objectMapper the ObjectMapper to use for JSON processing
	 * @return the configured message converter
	 */
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
		objectMapper.registerModule(new PageModule());
		return mappingJackson2HttpMessageConverter;
	}

	/**
	 * Creates a {@link Jackson2JsonMessageConverter} bean for message conversion. This
	 * converter is used to serialize and deserialize messages to and from JSON.
	 * @param classMapper the ClassMapper to use for type conversion
	 * @param objectMapper the ObjectMapper for JSON processing
	 * @return the configured JSON message converter
	 */
	@Bean
	public Jackson2JsonMessageConverter jsonMessageConverter(ClassMapper classMapper, ObjectMapper objectMapper) {
		Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter(objectMapper);
		jsonConverter.setClassMapper(classMapper);
		jsonConverter.setCreateMessageIds(true);
		return jsonConverter;
	}

	/**
	 * Provides a {@link ClassMapper} bean for type mapping in message conversion.
	 * @return the default Jackson2JavaTypeMapper
	 */
	@Bean
	public ClassMapper classMapper() {
		return new DefaultJackson2JavaTypeMapper();
	}

	/**
	 * Configures a {@link SimpleRabbitListenerContainerFactory} to create listener
	 * containers. This factory sets up message listener containers with specified
	 * configurations.
	 * @param connectionFactory the ConnectionFactory for creating connections
	 * @param converter the message converter
	 * @param retryOperationsInterceptor the interceptor for retry operations
	 * @return the configured listener container factory
	 */
	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
			Jackson2JsonMessageConverter converter, MethodInterceptor retryOperationsInterceptor) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(converter);
		factory.setConcurrentConsumers(this.concurrentConsumers);
		factory.setMaxConcurrentConsumers(this.maxConcurrentConsumers);
		factory.setAdviceChain(retryOperationsInterceptor);
		return factory;
	}

	/**
	 * Creates a {@link RabbitAdmin} bean for managing RabbitMQ resources.
	 * @return the RabbitAdmin instance
	 */
	@Bean
	public RabbitAdmin admin() {
		RabbitAdmin rabbitAdmin = new RabbitAdmin(this.connectionFactory);
		rabbitAdmin.afterPropertiesSet();
		return rabbitAdmin;
	}

	/**
	 * Creates a primary {@link MethodInterceptor} bean for retry operations with
	 * RabbitMQ. This interceptor implements retry logic with an exponential back-off
	 * policy.
	 * @param rabbitTemplate the RabbitTemplate used for message operations
	 * @return the retry operations interceptor
	 */
	@Primary
	@Bean
	public MethodInterceptor interceptor(RabbitTemplate rabbitTemplate) {
		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(1000);

		return RetryInterceptorBuilder.stateless().backOffPolicy(backOffPolicy).maxAttempts(3)
				.recoverer(new RepublishMessageRecoverer(rabbitTemplate, errorQueue().getName(), this.routingKey))
				.build();
	}

	/**
	 * Defines the primary AMQP exchange as a {@link TopicExchange}.
	 * @return the configured topic exchange
	 */
	@Bean
	public Exchange exchange() {
		TopicExchange deadLetterExchange = new TopicExchange(this.exchangeName, true, false);
		deadLetterExchange.setAdminsThatShouldDeclare(admin());
		return deadLetterExchange;
	}

	/**
	 * Defines the dead-letter exchange as a {@link DirectExchange}.
	 * @return the configured dead-letter exchange
	 */
	@Bean
	public Exchange deadLetterExchange() {
		DirectExchange deadLetterExchange = new DirectExchange(this.exchangeName + "." + DEAD_LETTER, true, false);
		deadLetterExchange.setAdminsThatShouldDeclare(admin());
		return deadLetterExchange;
	}

	/**
	 * Defines an error exchange as a {@link DirectExchange}.
	 * @return the configured error exchange
	 */
	@Bean
	public Exchange errorExchange() {
		DirectExchange errorExchange = new DirectExchange(this.exchangeName + "." + ERROR, true, false);
		errorExchange.setAdminsThatShouldDeclare(admin());
		return errorExchange;
	}

	/**
	 * Creates the primary queue with dead-letter and TTL configurations.
	 * @return the configured primary queue
	 */
	@Bean
	public Queue queue() {
		Queue queue = QueueBuilder.durable(this.queueName).withArgument("x-message-ttl", 10000)
				.withArgument("x-dead-letter-exchange", this.exchangeName + "." + DEAD_LETTER)
				.withArgument("x-dead-letter-routing-key", this.routingKey).build();
		queue.setAdminsThatShouldDeclare(admin());
		return queue;
	}

	/**
	 * Creates a dead-letter queue.
	 * @return the configured dead-letter queue
	 */
	@Bean
	public Queue deadLetterQueue() {
		Queue queue = QueueBuilder.durable(this.queueName + "." + DEAD_LETTER).build();
		queue.setAdminsThatShouldDeclare(admin());
		return queue;
	}

	/**
	 * Creates an error queue.
	 * @return the configured error queue
	 */
	@Bean
	public Queue errorQueue() {
		Queue queue = QueueBuilder.durable(this.queueName + "." + ERROR).build();
		queue.setAdminsThatShouldDeclare(admin());
		return queue;
	}

	/**
	 * Binds the primary queue to the primary exchange with the specified routing key.
	 * @return the binding between the primary queue and exchange
	 */
	@Bean
	public Binding queueBinding() {
		return BindingBuilder.bind(queue()).to(exchange()).with(this.routingKey).noargs();
	}

	/**
	 * Binds the dead-letter queue to the dead-letter exchange.
	 * @return the binding between the dead-letter queue and exchange
	 */
	@Bean
	public Binding queueBindingDlx() {
		return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(this.routingKey).noargs();
	}

	/**
	 * Binds the error queue to the error exchange.
	 * @return the binding between the error queue and exchange
	 */
	@Bean
	public Binding queueBindingError() {
		return BindingBuilder.bind(errorQueue()).to(errorExchange()).with(this.routingKey).noargs();
	}

}
