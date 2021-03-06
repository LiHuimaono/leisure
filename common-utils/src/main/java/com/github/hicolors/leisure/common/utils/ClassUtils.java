package com.github.hicolors.leisure.common.utils;

import com.github.hicolors.leisure.common.utils.reflect.ClassFactory;
import com.github.hicolors.leisure.common.utils.reflect.ColorsClassFactory;
import com.github.hicolors.leisure.common.utils.reflect.MethodProxy;
import com.github.hicolors.leisure.common.utils.reflect.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Class Utils
 *
 * @author 李伟超
 * @email liweichao0102@gmail.com
 * @date 2018/1/10
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ClassUtils {

    /**
     * The CGLIB class separator: "$$"
     */
    public static final String CGLIB_CLASS_SEPARATOR = "$$";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtils.class);
    private static final ColorsClassFactory CLASS_FACTORY = ClassFactory.getFastClassFactory();
    private static final ConcurrentHashMap<Class<?>, BeanInfo> BEAN_INFO_CACHE = new ConcurrentHashMap<>();

    public static BeanInfo getBeanInfo(Class<?> clazz) {
        if (!BEAN_INFO_CACHE.containsKey(clazz)) {
            try {
                BEAN_INFO_CACHE.putIfAbsent(clazz, Introspector.getBeanInfo(clazz, Object.class));
            } catch (IntrospectionException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return BEAN_INFO_CACHE.get(clazz);
    }

    /**
     * 创建@{clazz}对象
     * <p/>
     * 通过class创建对象
     *
     * @param <T>   泛型类型
     * @param clazz 类型
     * @return 创建的对象
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return CLASS_FACTORY.getClass(clazz).newInstance();
        } catch (Exception e) {
            LOGGER.error("创建类:" + clazz + "\t时出现异常!", e);
        }
        return null;
    }

    /**
     * 获取 @{target} 的真实class
     *
     * @param <T>    泛型类型
     * @param target 对象
     * @return class
     */
    public static <T> Class<T> getRealClass(T target) {
        if (target instanceof Class<?>) {
            return (Class<T>) target;
        }
        return (Class<T>) getRealClass(target.getClass());
    }

    /**
     * 获取 @{clazz} 的真实class
     *
     * @param <T>   泛型类型
     * @param clazz class
     * @return real class
     */
    public static <T> Class<T> getRealClass(Class<T> clazz) {
        return (Class<T>) getUserClass(clazz);
    }

    /**
     * 创建@{clazz}对象
     *
     * @param clazz     class
     * @param parameter parameter
     * @return Object
     * 调用带参数的构造方法
     */
    public static <T> T newInstance(Class<T> clazz, Object parameter) {
        return CLASS_FACTORY.getClass(clazz).newInstance(parameter);
    }

    /**
     * 创建@{clazz}对象
     *
     * @param <T>            泛型
     * @param clazz          class
     * @param parameterTypes 构造方法参数表
     * @param parameters     参数
     * @return Object
     * 调用带参数的构造方法
     */
    public static <T> T newInstance(Class<T> clazz, Class<?>[] parameterTypes, Object[] parameters) {
        return CLASS_FACTORY.getClass(clazz).newInstance(parameterTypes, parameters);
    }

    /**
     * 创建@{clazz}对象
     * 根据@{className}
     *
     * @param className class Name
     * @return 对象
     */
    public static <T> T newInstance(String className) {
        try {
            return (T) newInstance(org.apache.commons.lang3.ClassUtils.getClass(className));
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    public static ObjectProperty[] getPropertys(Object target) {
        return getPropertys(target.getClass());
    }

    public static ObjectProperty[] getPropertys(Class<?> clazz) {
        return CLASS_FACTORY.getClass(clazz).getPropertys();
    }

    public static ObjectProperty getProperty(Object target, String name) {
        return getProperty(target.getClass(), name);
    }

    public static ObjectProperty getProperty(Class<?> clazz, String name) {
        return CLASS_FACTORY.getClass(clazz).getProperty(name);
    }

    public static <T> T getFieldValue(Class<?> clazz, String name) {
        return CLASS_FACTORY.getClass(clazz).getValue(name);
    }

    public static <T> T getFieldValue(Object target, Class<?> clazz, String name) {
        return CLASS_FACTORY.getClass(clazz).getValue(target, name);
    }

    public static void setFieldValue(Object target, String name, Object value) {
        CLASS_FACTORY.getClass(getRealClass(target)).setValue(target, name, value);
    }

    public static void setFieldValue(Object target, Class<?> clazz, String name, Object value) {
        CLASS_FACTORY.getClass(clazz).setValue(target, name, value);
    }

    public static <T> T getValue(Object target, String name) {
        ObjectProperty property = getProperty(target, name);
        if ((property != null) && (property.isRead())) {
            return (T) property.getValue(target);
        }
        return CLASS_FACTORY.getClass(target.getClass()).getValue(target, name);
    }

    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        return CLASS_FACTORY.getClass(clazz).getDeclaredField(fieldName);
    }

    public static Field[] getDeclaredFields(Class<?> clazz, Class<? extends Annotation> annotClass) {
        return CLASS_FACTORY.getClass(clazz).getDeclaredFields(annotClass);
    }

    public static Field[] getDeclaredFields(Class<?> clazz) {
        return CLASS_FACTORY.getClass(clazz).getDeclaredFields();
    }

    public static void setValue(Object target, String name, Object value) {
        ObjectProperty property = getProperty(target, name);
        if ((property != null) && (property.isWrite())) {
            property.setValue(target, value);
        } else {
            CLASS_FACTORY.getClass(target.getClass()).setValue(target, name, value);
        }
    }

    public static MethodProxy getMethodProxy(Class<?> clazz, String method) {
        try {
            return CLASS_FACTORY.getClass(clazz).getMethod(method);
        } catch (Exception e) {
            LOGGER.error(clazz + "." + method + "-" + e.getMessage(), e);
        }
        return null;
    }

    public static MethodProxy getMethodProxy(Class<?> clazz, String method, Class<?>... paramTypes) {
        try {
            return CLASS_FACTORY.getClass(clazz).getMethod(method, paramTypes);
        } catch (Exception e) {
            LOGGER.error(clazz + "." + method + "-" + e.getMessage(), e);
        }
        return null;
    }

    public static boolean isBasicType(Class type) {
        return org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper(type) || isOther(type);
    }

    private static boolean isOther(Class type) {
        return String.class.isAssignableFrom(type) || Date.class.isAssignableFrom(type) || BigDecimal.class.isAssignableFrom(type) || Enum.class.isAssignableFrom(type);
    }

    public static boolean isBeanType(Class<?> clazz) {
        return !isBasicType(clazz);
    }

    public static Object newInstance(Class<?> componentType, int length) {
        return Array.newInstance(componentType, length);
    }

    public static boolean isArray(Field field) {
        return isArray(field.getType());
    }

    public static boolean isArray(Object object) {
        return (object != null) && (isArray(object.getClass()));
    }

    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    public static boolean isInterface(Field field) {
        if ((isMap(field)) || (isList(field))) {
            return false;
        }
        if (isArray(field)) {
            return field.getType().getComponentType().isInterface();
        }
        return isInterface(field.getType());
    }

    public static boolean isList(Field field) {
        return field.getType() == List.class;
    }

    public static boolean isMap(Field field) {
        return field.getType() == Map.class;
    }

    public static boolean isList(Object obj) {
        return obj instanceof List;
    }

    public static boolean isList(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    public static boolean isMap(Object obj) {
        return obj instanceof Map;
    }

    public static boolean isInterface(Class<?> clazz) {
        return clazz.isInterface();
    }

    public static Class getSuperClassGenricType(Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    public static <T> Class<T> getMethodGenericReturnType(Method method, int index) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) returnType;
            Type[] typeArguments = type.getActualTypeArguments();
            if ((index >= typeArguments.length) || (index < 0)) {
                throw new RuntimeException(String.format("你输入的索引 %s", index < 0 ? "不能小于0" : "超出了参数的总数"));
            }
            return (Class<T>) typeArguments[index];
        }
        return (Class<T>) Object.class;
    }

    public static <T> Class<T> getMethodGenericReturnType(Method method) {
        return getMethodGenericReturnType(method, 0);
    }

    /**
     * 获取方法参数的泛型类型
     *
     * @param method 反射方法
     * @param index  参数下标
     * @return 泛型类型集合
     */
    public static List<Class> getMethodGenericParameterTypes(Method method, int index) {
        List<Class> results = new ArrayList<>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        if ((index >= genericParameterTypes.length) || (index < 0)) {
//            throw new UtilsException("你输入的索引" + (index < 0 ? "不能小于0" : "超出了参数的总数"));
        }
        Type genericParameterType = genericParameterTypes[index];
        if (genericParameterType instanceof ParameterizedType) {
            ParameterizedType aType = (ParameterizedType) genericParameterType;
            Type[] parameterArgTypes = aType.getActualTypeArguments();
            for (Type parameterArgType : parameterArgTypes) {
                Class<?> parameterArgClass = WildcardType.class.isAssignableFrom(parameterArgType.getClass()) ? Object.class : (Class<?>) parameterArgType;
                results.add(parameterArgClass);
            }
            return results;
        }
        return results;
    }

    public static int getParamIndex(Method method, Class<?> genericType) {
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            Type genericParameterType = genericParameterTypes[i];
            if (genericParameterType instanceof ParameterizedType) {
                ParameterizedType aType = (ParameterizedType) genericParameterType;
                Type[] parameterArgTypes = aType.getActualTypeArguments();
                for (Type parameterArgType : parameterArgTypes) {
                    Class<?> parameterArgClass = WildcardType.class.isAssignableFrom(parameterArgType.getClass()) ? Object.class : (Class<?>) parameterArgType;
                    if (genericType == parameterArgClass) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public static List<Class> getMethodGenericParameterTypes(Method method) {
        return getMethodGenericParameterTypes(method, 0);
    }

    public static <T> Class<T> getFieldGenericType(Field field) {
        return getFieldGenericType(field, 0);
    }

    public static <T extends Annotation> T getClassGenricType(Class<?> clazz, Class<T> annotClass) {
        return clazz.getAnnotation(annotClass);
    }

    public static <T extends Annotation> T getFieldGenericType(Field field, Class<T> annotClass) {
        return field.getAnnotation(annotClass);
    }

    public static <T> Class<T> getFieldGenericType(Field field, int index) {
        Type genericFieldType = field.getGenericType();
        if (genericFieldType instanceof ParameterizedType) {
            ParameterizedType aType = (ParameterizedType) genericFieldType;
            Type[] fieldArgTypes = aType.getActualTypeArguments();
            if ((index >= fieldArgTypes.length) || (index < 0)) {
                throw new RuntimeException("你输入的索引" + (index < 0 ? "不能小于0" : "超出了参数的总数"));
            }
            return (Class<T>) fieldArgTypes[index];
        }
        return (Class<T>) Object.class;
    }

    public static String[] getParamNames(Class<?> clazz, String methodname, Class<?>[] parameterTypes) {
        return getParamNames(clazz.getName(), methodname, parameterTypes);
    }

    public static String[] getParamNames(String classname, String methodname, Class<?>[] parameterTypes) {
        try {
            return JavassistUtil.getParamNames(classname, methodname, parameterTypes);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new String[0];
    }

    public static Annotation getParamAnno(Method method) {
        return getParamAnnos(method, 0, 0);
    }

    public static Annotation[] getParamAnnos(Method method, Class<? extends Annotation> annotClass) {
        Annotation[][] annotations = method.getParameterAnnotations();
        for (Annotation[] paramAnnots : annotations) {
            for (Annotation annot : paramAnnots) {
                if (annotClass.equals(annot.annotationType())) {
                    return paramAnnots;
                }
            }
        }
        return new Annotation[0];
    }

    public static Annotation getParamAnnos(Method method, int i, int j) {
        return getParamAnnos(method, i)[j];
    }

    public static Annotation[] getParamAnnos(Method method, int i) {
        Annotation[][] annotations = method.getParameterAnnotations();
        return annotations[i];
    }

    public static Annotation[] getMethodAnnos(Method method) {
        return method.getAnnotations();
    }

    public static <T extends Annotation> T getMethodAnno(Method method, Class<T> classes) {
        if (method.isAnnotationPresent(classes)) {
            return method.getAnnotation(classes);
        }
        return null;
    }

    public static Method[] getDeclaredMethods(Class<?> clazz) {
        return ClassUtils.getRealClass(clazz).getDeclaredMethods();
    }

    public static Method getDeclaredMethod(Class<?> clazz, String methodName) {
        MethodProxy proxy = getMethodProxy(clazz, methodName);
        return proxy != null ? proxy.getMethod() : null;
    }

    public static <T extends Annotation> T getAnnotation(Class clazz, Class<T> annotClass) {
        return annotClass.cast(clazz.getAnnotation(annotClass));
    }

    public static <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> annotClass) {
        for (Annotation annot : annotations) {
            if (annotClass.equals(annot.annotationType())) {
                return (T) annot;
            }
        }
        return null;
    }

    public static <T> Class<T> getSuperClassGenricType(Class<T> clazz, int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            LOGGER.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return (Class<T>) Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if ((index >= params.length) || (index < 0)) {
            LOGGER.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: " + params.length);
            return (Class<T>) Object.class;
        }
        if (params[index] instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) params[index]).getRawType();
        }
        if (!(params[index] instanceof Class<?>)) {
            LOGGER.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return (Class<T>) Object.class;
        }
        return (Class<T>) params[index];
    }

    public static <T> Class<T> getInterfaceGenricType(Class<?> clazz, Class<?> interfaceClazz) {
        return getInterfaceGenricType(clazz, interfaceClazz, 0);
    }

    public static <T> Class<T> getInterfaceGenricType(Class<?> clazz, Class<?> interfaceClazz, int index) {
        Type[] genTypes = clazz.getGenericInterfaces();
        for (Type genType : genTypes) {
            if (!(genType instanceof ParameterizedType)) {
                return (Class<T>) Object.class;
            }
            if (interfaceClazz.equals(((ParameterizedType) genType).getRawType())) {
                Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
                if ((index >= params.length) || (index < 0)) {
                    LOGGER.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: " + params.length);
                    return (Class<T>) Object.class;
                }
                if (params[index] instanceof ParameterizedType) {
                    return (Class<T>) ((ParameterizedType) params[index]).getRawType();
                }
                if (!(params[index] instanceof Class<?>)) {
                    LOGGER.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
                    return (Class<T>) Object.class;
                }
                return (Class<T>) params[index];
            }
        }
        return (Class<T>) Object.class;
    }

    public static Class getRealType(ObjectProperty property) {
        return getRealType(property.getPropertyType());
    }

    public static Class getRealType(Class clazz) {
        if (clazz.isInterface()) {
            LOGGER.error("The implementation of interface " + clazz.toString() + " is not specified.");
        }
        return clazz;
    }

    public static Class<?> getUserClass(Class<?> clazz) {
        if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && Object.class != superclass) {
                return superclass;
            }
        }
        return clazz;
    }

}