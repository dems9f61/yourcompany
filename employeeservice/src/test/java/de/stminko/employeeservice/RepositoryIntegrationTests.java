package de.stminko.employeeservice;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.support.Repositories;
import org.springframework.test.util.ReflectionTestUtils;

@Slf4j
public class RepositoryIntegrationTests extends AbstractIntegrationTestSuite {

	private static final String[] PACKAGE_NAMES = { "de.stminko.employeeservice.department.control",
			"de.stminko.employeeservice.employee.control" };

	@Autowired
	private ApplicationContext context;

	@DisplayName("Calling methods of all repository succeeds and does not throw any Exception")
	@Test
	@SneakyThrows
	void givenAllDomainRepositories_whenCallAllDeclaredMethods_ThenSucceedAndDoNotThrowAnyException() {
		Repositories repositories = new Repositories(this.context);

		try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(PACKAGE_NAMES).scan()) {
			ClassInfoList classInfos = scanResult
				.getClassesImplementing("org.springframework.data.jpa.repository.JpaRepository");

			classInfos.stream().map(ClassInfo::loadClass).forEach((loadClass) -> {

				Optional<RepositoryInformation> repositoryInformation = repositories
					.getRepositoryInformation(loadClass);
				Class<?> domainClass = repositoryInformation.orElseThrow().getDomainType();
				Arrays.stream(loadClass.getDeclaredMethods())
					.filter((method) -> !method.isSynthetic() && !method.getName().equals("save"))
					.forEach((method) -> {
						log.info("current method= [{}]", method);
						String classSimpleName = loadClass.getSimpleName();
						String methodName = method.getName();
						Type[] genericParameterTypes = method.getGenericParameterTypes();
						Object[] arguments = new Object[genericParameterTypes.length];

						for (int i = 0; i < genericParameterTypes.length; i++) {
							Type parameterType = genericParameterTypes[i];
							boolean isList = isList(parameterType);
							String aClass = extractType(parameterType);
							Object arg = switch (aClass) {
								case "java.lang.Long", "java.lang.Object" -> 1L;
								case "java.lang.Integer" -> 1;
								case "java.math.BigDecimal" -> new BigDecimal("1");
								case "java.time.ZonedDateTime" -> ZonedDateTime.now();
								case "java.lang.Class<T>" -> domainClass;
								case "java.lang.String" -> "1";
								case "java.lang.Boolean" -> true;
								case "java.util.Date" -> new Date();
								case "org.springframework.data.domain.Pageable" -> Pageable.unpaged();
								default -> {
									try {
										if (Class.forName(aClass).isEnum()) {
											Object[] enumConstants = Class.forName(aClass).getEnumConstants();
											yield enumConstants[0];
										}
										else {
											yield null;
										}
									}
									catch (ClassNotFoundException ex) {
										Assertions.fail("%s %s argument for %s %s not supported", aClass, i, methodName,
												domainClass);
										yield null;
									}
								}
							};

							Assertions.assertThat(arg).isNotNull();
							arguments[i] = isList ? List.of(arg) : arg;
						}

						Object targetClass = repositories.getRepositoryFor(domainClass).orElseThrow();
						log.info("Calling {}.{}({}) for domain [{}]", classSimpleName, methodName, arguments,
								domainClass);
						Assertions
							.assertThatCode(() -> ReflectionTestUtils.invokeMethod(targetClass, methodName, arguments))
							.doesNotThrowAnyException();
					});
			});
		}
	}

	private boolean isList(Type type) {
		boolean result = false;
		if (type instanceof ParameterizedType parameterizedType) {
			result = List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType());
		}
		return result;
	}

	private String extractType(Type type) {
		if (type instanceof ParameterizedType parameterizedType) {
			Type[] genTypes = parameterizedType.getActualTypeArguments();
			if ((genTypes.length == 1) && (genTypes[0] instanceof Class<?> aClass)) {
				return aClass.getTypeName();
			}
			else if ((genTypes.length == 2) && (genTypes[1] instanceof Class<?> aClass)) {
				return aClass.getTypeName();
			}
		}

		return type.getTypeName();
	}

}
