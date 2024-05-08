package de.stminko.employeeservice.runtime.persistence.boundary;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class BeanToolTests {

	@Test
	void givenNullSource_whenCopyNonNullProperties_thenSucceedButDoNothing() {
		// Arrange
		SampleSourceEntity source = null;
		SampleTargetEntity target = new SampleTargetEntity();
		SampleTargetEntity targetCompare = new SampleTargetEntity();

		// Act
		BeanTool.copyNonNullProperties(source, target);

		// Assert
		Assertions.assertThat(target).isEqualTo(targetCompare);
	}

	@Test
	void givenNullSource_whenCopyNonNullPropertiesWithIgnoreProperties_thenSucceedButDoNothing() {
		// Act
		SampleSourceEntity source = null;
		SampleTargetEntity target = new SampleTargetEntity();
		SampleTargetEntity targetCompare = new SampleTargetEntity();

		// Act
		BeanTool.copyNonNullProperties(source, target, "byteProperty");

		// Assert
		Assertions.assertThat(target).isEqualTo(targetCompare);
	}

	@Test
	void givenNullTarget_whenCopyNonNullProperties_thenSucceedButDoNothing() {
		// Arrange
		SampleSourceEntity source = new SampleSourceEntity();
		SampleSourceEntity sourceCompare = new SampleSourceEntity();
		SampleTargetEntity target = null;

		// Act
		BeanTool.copyNonNullProperties(source, target);

		// Assert
		Assertions.assertThat(source).isEqualTo(sourceCompare);
	}

	@Test
	void givenNullTarget_whenCopyNonNullPropertiesWithIgnoreProperties_thenSucceedButDoNothing() {
		// Arrange
		SampleSourceEntity source = new SampleSourceEntity();
		SampleSourceEntity sourceCompare = new SampleSourceEntity();
		SampleTargetEntity target = null;

		// Act
		BeanTool.copyNonNullProperties(source, target, "byteProperty");

		// Assert
		Assertions.assertThat(source).isEqualTo(sourceCompare);
	}

	@Test
	void givenNonNullSourceAndTarget_whenCopyNonNullProperties_thenSucceedAndTargetContentsEqualSourceContents() {
		// Arrange
		SampleSourceEntity sse = SampleSourceEntity.sample();
		SampleTargetEntity ste = new SampleTargetEntity();

		// Act
		BeanTool.copyNonNullProperties(sse, ste);

		// Assert
		Assertions.assertThat(ste.isBooleanProperty()).isEqualTo(sse.isBooleanProperty());
		Assertions.assertThat(ste.getByteProperty()).isEqualTo(sse.getByteProperty());
		Assertions.assertThat(ste.getShortProperty()).isEqualTo(sse.getShortProperty());
		Assertions.assertThat(ste.getIntProperty()).isEqualTo(sse.getIntProperty());
		Assertions.assertThat(ste.getLongProperty()).isEqualTo(sse.getLongProperty());
		Assertions.assertThat(ste.getFloatProperty()).isEqualTo(sse.getFloatProperty());
		Assertions.assertThat(ste.getDoubleProperty()).isEqualTo(sse.getDoubleProperty());
		Assertions.assertThat(ste.getCharProperty()).isEqualTo(sse.getCharProperty());

		Assertions.assertThat(ste.getBooleanObjectProperty()).isEqualTo(sse.getBooleanObjectProperty());
		Assertions.assertThat(ste.getByteObjectProperty()).isEqualTo(sse.getByteObjectProperty());
		Assertions.assertThat(ste.getShortObjectProperty()).isEqualTo(sse.getShortObjectProperty());
		Assertions.assertThat(ste.getIntObjectProperty()).isEqualTo(sse.getIntObjectProperty());
		Assertions.assertThat(ste.getLongObjectProperty()).isEqualTo(sse.getLongObjectProperty());
		Assertions.assertThat(ste.getFloatObjectProperty()).isEqualTo(sse.getFloatObjectProperty());
		Assertions.assertThat(ste.getDoubleObjectProperty()).isEqualTo(sse.getDoubleObjectProperty());
		Assertions.assertThat(ste.getCharObjectProperty()).isEqualTo(sse.getCharObjectProperty());
		Assertions.assertThat(ste.getStringProperty()).isEqualTo(sse.getStringProperty());
		Assertions.assertThat(ste.getBigDecimalProperty()).isEqualTo(sse.getBigDecimalProperty());
	}

	@Test
	void givenNonNullSourceAndTarget_whenCopyNonNullPropertiesWithIgnoreProperties_thenSucceedAndTargetContentsEqualSourceContents() {
		// Arrange
		SampleSourceEntity sse = SampleSourceEntity.sample();
		SampleTargetEntity ste = new SampleTargetEntity();

		// Act
		BeanTool.copyNonNullProperties(sse, ste, "longProperty", "booleanObjectProperty", "bigDecimalProperty");

		// Assert
		Assertions.assertThat(ste.isBooleanProperty()).isEqualTo(sse.isBooleanProperty());
		Assertions.assertThat(ste.getByteProperty()).isEqualTo(sse.getByteProperty());
		Assertions.assertThat(ste.getShortProperty()).isEqualTo(sse.getShortProperty());
		Assertions.assertThat(ste.getIntProperty()).isEqualTo(sse.getIntProperty());
		Assertions.assertThat(ste.getLongProperty()).isZero();
		Assertions.assertThat(ste.getFloatProperty()).isEqualTo(sse.getFloatProperty());
		Assertions.assertThat(ste.getDoubleProperty()).isEqualTo(sse.getDoubleProperty());
		Assertions.assertThat(ste.getCharProperty()).isEqualTo(sse.getCharProperty());

		Assertions.assertThat(ste.getBooleanObjectProperty()).isNull();
		Assertions.assertThat(ste.getByteObjectProperty()).isEqualTo(sse.getByteObjectProperty());
		Assertions.assertThat(ste.getShortObjectProperty()).isEqualTo(sse.getShortObjectProperty());
		Assertions.assertThat(ste.getIntObjectProperty()).isEqualTo(sse.getIntObjectProperty());
		Assertions.assertThat(ste.getLongObjectProperty()).isEqualTo(sse.getLongObjectProperty());
		Assertions.assertThat(ste.getFloatObjectProperty()).isEqualTo(sse.getFloatObjectProperty());
		Assertions.assertThat(ste.getDoubleObjectProperty()).isEqualTo(sse.getDoubleObjectProperty());
		Assertions.assertThat(ste.getCharObjectProperty()).isEqualTo(sse.getCharObjectProperty());
		Assertions.assertThat(ste.getStringProperty()).isEqualTo(sse.getStringProperty());
		Assertions.assertThat(ste.getBigDecimalProperty()).isNull();
	}

	@Test
	void givenNonNullSourceWithDefaultDataAndTargetEntityWithData_whenCopyNonNullPropertiesWithIgnoreProperties_thenSucceedAndTargetContentsRemainsUntouched() {
		// Arrange
		SampleSourceEntity sse = new SampleSourceEntity();
		SampleTargetEntity ste = SampleTargetEntity.sample();

		// Act
		BeanTool.copyNonNullProperties(sse, ste);

		// Assert
		Assertions.assertThat(ste.isBooleanProperty()).isFalse();
		Assertions.assertThat(ste.getByteProperty()).isEqualTo((byte) 0);
		Assertions.assertThat(ste.getShortProperty()).isEqualTo((short) 0);
		Assertions.assertThat(ste.getIntProperty()).isZero();
		Assertions.assertThat(ste.getLongProperty()).isZero();
		Assertions.assertThat(ste.getFloatProperty()).isEqualTo(0f);
		Assertions.assertThat(ste.getDoubleProperty()).isEqualTo(0.0);
		Assertions.assertThat(ste.getCharProperty()).isEqualTo((char) 0);

		Assertions.assertThat(ste.getBooleanObjectProperty()).isNotNull();
		Assertions.assertThat(ste.getByteObjectProperty()).isNotNull();
		Assertions.assertThat(ste.getShortObjectProperty()).isNotNull();
		Assertions.assertThat(ste.getIntObjectProperty()).isNotNull();
		Assertions.assertThat(ste.getLongObjectProperty()).isNotNull();
		Assertions.assertThat(ste.getFloatObjectProperty()).isNotNull();
		Assertions.assertThat(ste.getDoubleObjectProperty()).isNotNull();
		Assertions.assertThat(ste.getCharObjectProperty()).isNotNull();
		Assertions.assertThat(ste.getStringProperty()).isNotNull();
		Assertions.assertThat(ste.getBigDecimalProperty()).isNotNull();
	}

	@Test
	void givenNonNullSourceAsRecord_whenCopyNonNullProperties_thenSucceed() {
		// Arrange
		long longObjectProperty = 1234L;
		char charObjectProperty = 'a';
		SampleRecordDto sse = new SampleRecordDto(null, longObjectProperty, charObjectProperty);
		SampleTargetEntity ste = SampleTargetEntity.sample();

		// Act
		BeanTool.copyNonNullProperties(sse, ste);

		// Assert
		Assertions.assertThat(ste.getLongObjectProperty()).isEqualTo(longObjectProperty);
		Assertions.assertThat(ste.getCharObjectProperty()).isEqualTo(charObjectProperty);
		Assertions.assertThat(ste.getStringProperty()).isNotNull();
	}

}
