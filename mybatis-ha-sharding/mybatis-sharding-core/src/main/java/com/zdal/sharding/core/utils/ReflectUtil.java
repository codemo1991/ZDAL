package com.zdal.sharding.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

/**
 * 
 * Name: ReflectUtil.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.utils.ReflectUtil.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年6月12日 下午2:19:05
 *
 * Update-User: @author
 * Update-Time:
 * Update-Remark:
 * 
 * Check-User:
 * Check-Time:
 * Check-Remark:
 * 
 * Company: 
 * Copyright:
 */
public final class ReflectUtil {
	
	private static final String IS_SETTER_PATTERN = "set(\\w+)";
	private static final String IS_GETTER_PATTERN = "get(\\w+)";
	
	private ReflectUtil() {
		throw new RuntimeException();
	}
	
	/**
	 * 根据class创建一个该class的实例
	 * @param clazz
	 * @return
	 */
	public static Object newInstance(Class<?> clazz) {
		return newInstance(clazz.getName());
	}
	
	/**
	 * 根据className创建一个该class的实例
	 * @param className
	 * @return
	 */
	public static Object newInstance(String className) {
		try {
			return Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 直接设置对象属性值,无视private/protected修饰符,不经过setter函数.
	 * @param object
	 * @param fieldName
	 * @param value
	 */
	public static void setFieldValue(final Object object, final String fieldName, final Object value) {
		Field field = getDeclaredField(object, fieldName);

		if (field == null) throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");

		makeAccessible(field);

		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 直接读取对象属性值,无视private/protected修饰符,不经过getter函数.
	 * @param object
	 * @param fieldName
	 * @return
	 */
	public static Object getFieldValue(final Object object, final String fieldName) {
		Field field = getDeclaredField(object, fieldName);

		if (field == null) throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");

		makeAccessible(field);

		Object result = null;
		try {
			result = field.get(object);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 直接调用对象方法,无视private/protected修饰符.
	 * @param object
	 * @param methodName
	 * @param parameterTypes
	 * @param parameters
	 * @return
	 * @throws InvocationTargetException
	 */
	public static Object invokeMethod(final Object object, final String methodName, final Class<?>[] parameterTypes,
			final Object[] parameters) throws InvocationTargetException {
		
		Method method = getDeclaredMethod(object, methodName, parameterTypes);
		
		if (method == null) throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + object + "]");

		method.setAccessible(true);

		try {
			return method.invoke(object, parameters);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 循环向上转型,获取对象的DeclaredField.
	 * @param object
	 * @param fieldName
	 * @return
	 */
	protected static Field getDeclaredField(final Object object, final String fieldName) {		
		for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				return superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 循环向上转型,获取对象的DeclaredField.
	 * @param field
	 */
	protected static void makeAccessible(final Field field) {
		if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
	}

	/**
	 * 循环向上转型,获取对象的DeclaredMethod.
	 * @param object
	 * @param methodName
	 * @param parameterTypes
	 * @return
	 */
	protected static Method getDeclaredMethod(Object object, String methodName, Class<?>[] parameterTypes) {
		for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				return superClass.getDeclaredMethod(methodName, parameterTypes);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 通过反射,获得Class定义中声明的父类的泛型参数的类型.
	 * eg.
	 * @param clazz The class to introspect
	 * @return the first generic declaration, or Object.class if cannot be determined
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getSuperClassGenricType(final Class<?> clazz) {
		return getSuperClassGenricType(clazz, 0);
	}

	/**
	 * 通过反射,获得Class定义中声明的父类的泛型参数的类型.
	 * eg.
	 * @param clazz The class to introspect
	 * @return the first generic declaration, or Object.class if cannot be determined
	 */
	@SuppressWarnings("rawtypes")
	public static Class getSuperClassGenricType(final Class<?> clazz, final int index) {
		Type genType = clazz.getGenericSuperclass();

		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

		if (index >= params.length || index < 0) {
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}
		return (Class<?>) params[index];
	}

	/**
	 * 将反射时的checked exception转换为unchecked exception.
	 * @param e
	 * @return
	 */
	public static IllegalArgumentException convertToUncheckedException(Exception e) {
		if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
				|| e instanceof NoSuchMethodException) {
			return new IllegalArgumentException("Refelction Exception.", e);
		} else {
			return new IllegalArgumentException(e);
		}
	}
	
	/**
	 * 判断一个方法是否为setter方法
	 * @param method
	 * @return
	 */
	public static boolean isSetter(Method method) {
		if (method == null) return false;
		String methodName = method.getName();
		return Pattern.matches(IS_SETTER_PATTERN, methodName);
	}
	
	/**
	 * 判断一个方法是否为getter
	 * @param method
	 * @return
	 */
	public static boolean isGetter(Method method) {
		if (method == null) return false;
		String methodName = method.getName();
		return Pattern.matches(IS_GETTER_PATTERN, methodName);
	}
	
	/**
	 * 判断一个方法是不是 setter getter 方法
	 * @param method
	 * @return
	 */
	public static boolean isSetterGetter(Method method) {
		return isSetter(method) || isGetter(method);
	}
	
	/**
	 * 根据setter getter方法名称获取对应的字段名称
	 * @param method
	 * @return
	 */
	public static String getFieldNameBySetterGetter(Method method) {
		if (!isSetterGetter(method)) return null;
		
		String methodName = method.getName();
		methodName = StringUtil.subPre(methodName, 3);
		
		return methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1, methodName.length());
	}

}
