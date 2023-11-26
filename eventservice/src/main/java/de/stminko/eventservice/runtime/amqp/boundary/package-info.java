/**
 * focuses on the Advanced Message Queuing Protocol (AMQP) configurations within the event
 * service runtime. It provides the necessary configurations and components for setting up
 * and managing AMQP-based messaging, particularly with RabbitMQ, ensuring efficient and
 * reliable message handling.
 * <p>
 * Central to this package is the
 * {@link de.stminko.eventservice.runtime.amqp.boundary.AmqpConfig} class, which is
 * configured with annotations such as {@code @Data}, {@code @Configuration},
 * {@code @ConfigurationProperties}, and {@code @Validated}. This class serves as the
 * foundational setup for AMQP communication, defining crucial parameters such as: -
 * Exchange, queue, and routing key names for AMQP messaging. - Minimum and maximum
 * numbers of concurrent consumers. - ConnectionFactory for RabbitMQ connections. - Beans
 * for message conversion, listener container factory, RabbitAdmin, and various bindings.
 * <p>
 * This package demonstrates a comprehensive approach to AMQP configuration, emphasizing
 * flexibility, reliability, and scalability in the event service's messaging
 * capabilities.
 *
 * @author Stéphan Minko
 */
package de.stminko.eventservice.runtime.amqp.boundary;
