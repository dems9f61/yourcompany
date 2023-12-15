package de.stminko.employeeservice.runtime.persistence.boundary;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * A BeanPostProcessor that wraps DataSource beans in a proxy to enable additional
 * features like logging and query analysis.
 *
 * <p>
 * This post-processor is conditionally activated based on the presence and value of the
 * property 'datasource-debug.enabled'. When enabled, it wraps any DataSource bean that is
 * not already a ProxyDataSource with a new proxy. This proxy provides enhanced debugging
 * and logging capabilities for the DataSource.
 * </p>
 *
 * <p>
 * The proxy is created using a ProxyFactory and is augmented with a
 * {@code ProxyDataSourceInterceptor}. This interceptor wraps the original DataSource with
 * a {@code ProxyDataSourceBuilder}, adding functionalities like multiline logging, SLF4J
 * logging at INFO level, and a listener for query count analysis.
 * </p>
 *
 * <p>
 * Use this post-processor to gain insights into database operations, particularly useful
 * during development and debugging phases.
 * </p>
 *
 * @author Stéphan Minko
 */
@ConditionalOnProperty(name = "datasource-debug.enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
public class DatasourceProxyBeanPostProcessor implements BeanPostProcessor {

	/**
	 * this method is called after the initialization of a bean. It checks if the bean is
	 * an instance of DataSource and not an instance of ProxyDataSource. If the conditions
	 * are true, it creates a ProxyFactory and adds a ProxyDataSourceInterceptor to the
	 * factory. The ProxyDataSourceInterceptor wraps the original DataSource with a
	 * ProxyDataSourceBuilder, providing additional functionalities like logging and query
	 * analysis. Finally, it returns the proxy object if the conditions are met, otherwise
	 * it returns the original bean.
	 * @param bean The bean object being initialized.
	 * @param beanName The name of the bean.
	 * @return The initialized bean object, either the original bean or the proxy bean.
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		if ((bean instanceof DataSource source) && !(bean instanceof ProxyDataSource)) {
			final ProxyFactory factory = new ProxyFactory(bean);
			factory.setProxyTargetClass(true);
			factory.addAdvice(new ProxyDataSourceInterceptor(source));
			return factory.getProxy();
		}
		return bean;
	}

	private record ProxyDataSourceInterceptor(DataSource dataSource) implements MethodInterceptor {

		private ProxyDataSourceInterceptor(final DataSource dataSource) {
			this.dataSource = ProxyDataSourceBuilder.create(dataSource).name("MyDS").multiline()
					.logQueryBySlf4j(SLF4JLogLevel.INFO).listener(new DataSourceQueryCountListener()).build();
		}

		@Override
		public Object invoke(final MethodInvocation invocation) throws Throwable {
			final Method proxyMethod = ReflectionUtils.findMethod(this.dataSource.getClass(),
					invocation.getMethod().getName());
			if (proxyMethod != null) {
				return proxyMethod.invoke(this.dataSource, invocation.getArguments());
			}
			return invocation.proceed();
		}

	}

}
