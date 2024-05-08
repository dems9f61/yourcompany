package de.stminko.employeeservice.runtime.persistence.boundary;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class SampleTargetEntity {

	private boolean booleanProperty;

	private byte byteProperty;

	private short shortProperty;

	private int intProperty;

	private long longProperty;

	private float floatProperty;

	private double doubleProperty;

	private char charProperty;

	private Boolean booleanObjectProperty;

	private Byte byteObjectProperty;

	private Short shortObjectProperty;

	private Integer intObjectProperty;

	private Long longObjectProperty;

	private Float floatObjectProperty;

	private Double doubleObjectProperty;

	private Character charObjectProperty;

	private String stringProperty;

	private BigDecimal bigDecimalProperty;

	private ZonedDateTime zonedDateTimeProperty;

	public static SampleTargetEntity sample() {
		SampleTargetEntity retVal = new SampleTargetEntity();
		retVal.setBooleanProperty(true);
		retVal.setByteProperty((byte) 1);
		retVal.setShortProperty((short) 1000);
		retVal.setIntProperty(1000000);
		retVal.setLongProperty(10000000000L);
		retVal.setFloatProperty(9712370.4545f);
		retVal.setDoubleProperty(123370.7895);
		retVal.setCharProperty('c');

		retVal.setBooleanObjectProperty(false);
		retVal.setByteObjectProperty((byte) 10);
		retVal.setShortObjectProperty((short) 10000);
		retVal.setIntObjectProperty(10000000);
		retVal.setLongObjectProperty(100000000000L);
		retVal.setFloatObjectProperty(9612370.4545f);
		retVal.setDoubleObjectProperty(213370.7895);
		retVal.setCharObjectProperty('d');
		retVal.setStringProperty("Source String");
		retVal.setBigDecimalProperty(new BigDecimal("1235.346"));
		return retVal;
	}

}
