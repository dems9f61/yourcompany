package de.stminko.employeeservice;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * some additional code sanity tests beyond pure Checkstyle and Code Formatting. Enforces
 * certain declarative / annotation driven Coding best practices and conventions
 *
 * @author Stéphan Minko
 */
@Slf4j
public class ArchUnitTests {

	/**
	 * An {@link ImportOption} that excludes both test classes and 'package-info.java'
	 * files from being considered in ArchUnit analyses.
	 * <p>
	 * This is particularly useful when setting up ArchUnit rules and you want to focus
	 * only on the main source code, excluding test classes and package-info descriptions
	 * which usually do not contain architecture-relevant code.
	 * </p>
	 */
	private static final ImportOption EXCLUDE_PACKAGE_INFO_AND_TEST_CLASSES = (location) -> {
		String path = location.asURI().getPath();
		return !path.contains("/test-classes/") && !location.contains("package-info");
	};

	private static final JavaClasses CLASSES = new ClassFileImporter()
		.withImportOption(EXCLUDE_PACKAGE_INFO_AND_TEST_CLASSES)
		.importPackages("de.stminko.employeeservice");

	private static final JavaClasses ENTITY_CLASSES = new ClassFileImporter()
		.withImportOption(EXCLUDE_PACKAGE_INFO_AND_TEST_CLASSES)
		.importPackages("de.stminko.employeeservice.department.entity", "de.stminko.employeeservice.employee.entity");

	/**
	 * A custom ArchUnit condition that checks if a class contains fields annotated with
	 * both {@link jakarta.persistence.ManyToOne} and
	 * {@link jakarta.validation.constraints.NotNull}, where the 'optional' attribute of
	 * the {@link jakarta.persistence.ManyToOne} annotation is set to true.
	 *
	 * <p>
	 * This condition ensures that if a field is defined for a Many-To-One relationship
	 * and is meant to be 'not null', the 'optional' attribute of the
	 * {@link jakarta.persistence.ManyToOne} annotation should be appropriately set to
	 * false.
	 * </p>
	 */
	public static final ArchCondition<JavaClass> HAVE_NOT_NULL_MANY_TO_ONE_ANNOTATION_CONDITION = new ArchCondition<>(
			" set @ManyToOne-optional to false when using @NotNull") {
		@Override
		public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
			log.info("check (javaClass= [{}] )", javaClass);
			for (JavaField field : javaClass.getFields()) {
				log.info("field.name= [{}] )", field.getName());
				if (field.isAnnotatedWith(ManyToOne.class) && field.isAnnotatedWith(NotNull.class)
						&& field.getAnnotationOfType(ManyToOne.class).optional()) {
					String message = String.format("ManyToOne optional property is not set to false on field %s",
							field.getFullName());
					conditionEvents.add(SimpleConditionEvent.violated(field, message));
				}
			}
		}
	};

	/**
	 * Condition that check if a @PostMapping, @PutMapping or @PatchMapping of
	 * a @RestController has produces = { MediaType.APPLICATION_JSON_VALUE }
	 */
	private static final ArchCondition<JavaClass> PRODUCES_METHOD_CONDITION = new ArchCondition<>(
			"should have produces = { MediaType.APPLICATION_JSON_VALUE }") {
		@Override
		public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
			log.info("check (javaClass= [{}] )", javaClass);
			Set<JavaMethod> methods = javaClass.getMethods();
			for (JavaMethod javaMethod : methods) {
				log.info("javaMethod= [{}] ", javaMethod);
				Set<JavaAnnotation<JavaMethod>> annotations = javaMethod.getAnnotations();

				List<Class<?>> requestMappingsToBeChecked = List.of(PostMapping.class);

				for (JavaAnnotation<JavaMethod> annotation : annotations) {
					log.info("annotation= [{}] ", annotation);
					if (requestMappingsToBeChecked.stream()
						.anyMatch((Class<?> classA) -> annotation.getRawType().isEquivalentTo(classA))) {
						checkRequestMappingAnnotation(javaMethod, conditionEvents, annotation);
					}
				}
			}
		}
	};

	/**
	 * Condition that checks if methods returning a Page<?> and are having the parameter
	 * Pageable, also have the annotation @PageableDefault before Pageable
	 */
	private static final ArchCondition<JavaMethod> METHOD_WITH_RETURN_TYPE_PAGE_HAS_PAGEABLE_ANNOTATION_CONDITION = new ArchCondition<>(
			"while returning a Page<?> and the method parameter Pageable is present, annotated with"
					+ " @PageableDefault and have a default value of 50 e.x. @PageableDefault(50)") {
		@Override
		public void check(JavaMethod javaMethod, ConditionEvents conditionEvents) {
			checkPageableDefaultForMethodsReturningAPage(javaMethod, conditionEvents);
		}
	};

	private static final List<String> ALLOWED_MEDIA_TYPES_FOR_REQUEST_MAPPING = List
		.of(MediaType.APPLICATION_JSON_VALUE);

	/**
	 * Checks if the correct MediaTypes are set for the produces in @RequestMappings
	 * @param javaClassOrMethod the current java class
	 * @param events the conditions of the check
	 * @param annotation the annotation of the class etc.
	 */
	private static void checkRequestMappingAnnotation(Object javaClassOrMethod, ConditionEvents events,
			JavaAnnotation<?> annotation) {
		String[] produces = (String[]) annotation.getProperties().get("produces");
		String className = annotation.getRawType().getName();
		if ((produces == null) || (produces.length == 0)) {
			events.add(SimpleConditionEvent.violated(javaClassOrMethod,
					"no produces found for @" + className + " in " + javaClassOrMethod.toString()));
		}
		else {
			while (!ALLOWED_MEDIA_TYPES_FOR_REQUEST_MAPPING.contains(Arrays.stream(produces).iterator().next())) {
				events.add(SimpleConditionEvent.violated(javaClassOrMethod,
						"wrong produces for @RequestMapping @" + className + " in " + javaClassOrMethod.toString()
								+ ". Only MediaType.APPLICATION_JSON_VALUE allowed"));
			}
		}
	}

	/**
	 * checks if @PageableDefault is present for the given methods parement Pageable
	 * @param javaMethod the current java method
	 * @param events the conditions of the check
	 */
	private static void checkPageableDefaultForMethodsReturningAPage(JavaMethod javaMethod, ConditionEvents events) {
		if (javaMethod.getRawReturnType().isEquivalentTo(Page.class)) {
			List<JavaParameter> parameter = javaMethod.getParameters();
			for (JavaParameter javaParameter : parameter) {
				if (javaParameter.getRawType().isEquivalentTo(Pageable.class)) {
					Set<JavaAnnotation<JavaParameter>> annotations = javaParameter.getAnnotations();
					// fail if @PageableDefault is missing
					if (annotations.stream()
						.noneMatch((JavaAnnotation<JavaParameter> annotation) -> annotation.getRawType()
							.isEquivalentTo(PageableDefault.class))) {
						events.add(
								SimpleConditionEvent.violated(javaMethod, "missing @PageableDefault in " + javaMethod));
					}
					else {
						for (JavaAnnotation<JavaParameter> annotation : annotations) {
							if (annotation.getRawType().isEquivalentTo(PageableDefault.class)) {
								validateAnnotation(javaMethod, annotation, events);
							}
						}
					}

				}
			}
		}
	}

	private static void validateAnnotation(JavaMethod javaMethod, JavaAnnotation<JavaParameter> annotation,
			ConditionEvents events) {
		Map<String, Object> properties = annotation.getProperties();
		if (!properties.containsKey("value")) {
			events.add(SimpleConditionEvent.violated(javaMethod, "missing value at @PageableDefault in " + javaMethod));
		}
		else {
			Integer value = (Integer) annotation.getProperties().get("value");
			if (ObjectUtils.isEmpty(value) || (value == 0)) {
				events.add(SimpleConditionEvent.violated(javaMethod,
						"value is null or 0 at @PageableDefault in " + javaMethod));
			}
		}
	}

	@DisplayName("ManyToOne and NotNull fields should always set Optional to true")
	@Test
	public void givenEntitiesWithManyToOneAndNotNull_whenChecked_thenEnsureOptionalIsTrue() {
		Assertions
			.assertThatCode(() -> ArchRuleDefinition.classes()
				.should(HAVE_NOT_NULL_MANY_TO_ONE_ANNOTATION_CONDITION)
				.allowEmptyShould(true)
				.check(ENTITY_CLASSES))
			.doesNotThrowAnyException();
	}

	@DisplayName("POST methods in Rest-Controller classes should produce JSON")
	@Test
	void givenRestControllerClasses_whenCheckingPostMethods_thenShouldProduceJson() {
		Assertions
			.assertThatCode(() -> ArchRuleDefinition.classes()
				.that()
				.areAnnotatedWith(RestController.class)
				.or()
				.areAnnotatedWith(Controller.class)
				.should(PRODUCES_METHOD_CONDITION)
				.check(CLASSES))
			.doesNotThrowAnyException();
	}

	@DisplayName("Classes annotated with RestController or Controller should end with 'Controller'")
	@Test
	void givenRestControllerClasses_whenCheckedForNamingConvention_thenMustEndWithController() {
		Assertions
			.assertThatCode(() -> ArchRuleDefinition.classes()
				.that()
				.areAnnotatedWith(RestController.class)
				.or()
				.areAnnotatedWith(Controller.class)
				.should()
				.haveSimpleNameEndingWith("Controller")
				.check(CLASSES))
			.doesNotThrowAnyException();
	}

	@DisplayName("Methods annotated with mapping annotations should only be defined in Rest-Controller classes")
	@Test
	void givenMethodsAnnotatedWithMappingAnnotations_whenChecked_thenShouldBeInRestControllerClasses() {
		ArchRuleDefinition.methods()
			.that()
			.areAnnotatedWith(GetMapping.class)
			.or()
			.areAnnotatedWith(PostMapping.class)
			.or()
			.areAnnotatedWith(RequestMapping.class)
			.or()
			.areAnnotatedWith(PutMapping.class)
			.or()
			.areAnnotatedWith(PatchMapping.class)
			.or()
			.areAnnotatedWith(DeleteMapping.class)
			.should()
			.beDeclaredInClassesThat()
			.areAnnotatedWith(Controller.class)
			.orShould()
			.beDeclaredInClassesThat()
			.areAnnotatedWith(RestController.class)
			.check(CLASSES);
	}

	@DisplayName("Mapping-annotated methods must be in RestControllers; methods returning paginated lists need @PageableDefault")
	@Test
	void givenMappingMethodsAndPageReturn_whenChecked_thenShouldBeInControllersWithPageableDefault() {
		ArchRuleDefinition.methods()
			.that()
			.areAnnotatedWith(GetMapping.class)
			.or()
			.areAnnotatedWith(PostMapping.class)
			.or()
			.areAnnotatedWith(RequestMapping.class)
			.or()
			.areAnnotatedWith(PutMapping.class)
			.or()
			.areAnnotatedWith(PatchMapping.class)
			.or()
			.areAnnotatedWith(DeleteMapping.class)
			.should()
			.beDeclaredInClassesThat()
			.areAnnotatedWith(Controller.class)
			.orShould()
			.beDeclaredInClassesThat()
			.areAnnotatedWith(RestController.class)
			.andShould(METHOD_WITH_RETURN_TYPE_PAGE_HAS_PAGEABLE_ANNOTATION_CONDITION)
			.check(CLASSES);
	}

}
