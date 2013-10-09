package org.springframework.data.support;

import java.lang.reflect.Field;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.util.ReflectionUtils;

/**
 * Custom extension of {@link BeanWrapperImpl} that falls back to direct field access in case the object or type being
 * wrapped does not use accessor methods.
 * 
 * @author Oliver Gierke
 */
public class DirectFieldAccessFallbackBeanWrapper extends BeanWrapperImpl {

	public DirectFieldAccessFallbackBeanWrapper(Object entity) {
		super(entity);
	}

	public DirectFieldAccessFallbackBeanWrapper(Class<?> type) {
		super(type);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.beans.BeanWrapperImpl#getPropertyValue(java.lang.String)
	 */
	@Override
	public Object getPropertyValue(String propertyName) {

		try {
			return super.getPropertyValue(propertyName);
		} catch (NotReadablePropertyException e) {
			return getPropertyUsingFieldAccess(propertyName);
		}
	}

	/**
	 * Looks up the property with the given name using direct field access.
	 * 
	 * @param propertyName
	 * @return
	 */
	protected Object getPropertyUsingFieldAccess(String propertyName) {

		Field field = ReflectionUtils.findField(getWrappedClass(), propertyName);
		ReflectionUtils.makeAccessible(field);
		return ReflectionUtils.getField(field, getWrappedInstance());
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.beans.BeanWrapperImpl#setPropertyValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setPropertyValue(String propertyName, Object value) {

		try {
			super.setPropertyValue(propertyName, value);
		} catch (NotWritablePropertyException e) {
			setPropertyUsingFieldAccess(propertyName, value);
		}
	}

	/**
	 * Sets the property with the given name to the given value using field access.
	 * 
	 * @param propertyName
	 * @param value
	 */
	protected void setPropertyUsingFieldAccess(String propertyName, Object value) {

		Field field = ReflectionUtils.findField(getWrappedClass(), propertyName);
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, getWrappedInstance(), value);
	}
}
