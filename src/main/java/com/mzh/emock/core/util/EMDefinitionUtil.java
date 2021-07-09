package com.mzh.emock.core.util;

import com.mzh.emock.core.type.EMock;
import com.mzh.emock.core.type.object.definition.EMDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.function.Supplier;

public class EMDefinitionUtil {

    /**
     * check whether source method is a mock definition source
     *
     * @return whether the method pass the check
     *
     * @param method the src method
     */
    public static boolean checkMethod(Method method) {
        return (method.getModifiers() & Modifier.PUBLIC) != 0
                && (method.getModifiers() & Modifier.STATIC) != 0
                && method.isAnnotationPresent(EMock.class)
                && method.getParameterCount() == 1
                && Supplier.class.isAssignableFrom(method.getParameterTypes()[0])
                && method.getReturnType() == EMDefinition.class
                && ParameterizedType.class.isAssignableFrom(method.getGenericReturnType().getClass())
                && ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments() != null
                && ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments().length == 1;
    }
}
