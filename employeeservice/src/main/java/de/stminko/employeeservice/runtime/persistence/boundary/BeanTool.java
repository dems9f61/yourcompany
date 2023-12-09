package de.stminko.employeeservice.runtime.persistence.boundary;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;

/**
 * simple BeanUtil extension copying all non null data from source to data. All null value
 * fields in the source object are added to the ignoreProperties list upon delegation to
 * {@link BeanUtils} class.
 *
 * @author Stéphan Minko
 */
@Slf4j
public final class BeanTool {

	private BeanTool() {
		throw new AssertionError("This is not meant to be instantiated");
	}

	public static void copyNonNullProperties(Object source, Object target) {
		copyNonNullProperties(source, target, (String[]) null);
	}

	public static void copyNonNullProperties(Object source, Object target, String... ignoreProperties) {
		if (source == null) {
			log.debug("Source object is null");
			return;
		}
		if (target == null) {
			log.debug("Target object is null");
			return;
		}
		Set<String> ignoreFieldNames = new HashSet<>();
		if (ignoreProperties != null) {
			ignoreFieldNames.addAll(Arrays.asList(ignoreProperties));
		}
		Set<String> ignoreAndNullValueFieldNames = getAllIgnoreAndNullFields(source, ignoreFieldNames,
				source.getClass());
		BeanUtils.copyProperties(source, target, ignoreAndNullValueFieldNames.toArray(new String[0]));
	}

	private static Set<String> getAllIgnoreAndNullFields(Object source, Set<String> nullOrIgnoreFields, Class<?> type) {
		MethodHandles.Lookup lookup;
		try {
			lookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
		}
		catch (IllegalAccessException illegalAccessException) {
			throw new RuntimeException(illegalAccessException);
		}

		while (type != null) {
			for (Field field : type.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				try {
					VarHandle varHandle = lookup.unreflectVarHandle(field);
					if (varHandle.get(source) == null) {
						nullOrIgnoreFields.add(field.getName());
					}
				}
				catch (IllegalAccessException illegalAccessException) {
					throw new RuntimeException(illegalAccessException);
				}
			}
			type = type.getSuperclass();
		}
		return nullOrIgnoreFields;
	}

}
