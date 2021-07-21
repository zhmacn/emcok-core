package com.mzh.emock.core.util;

import com.mzh.emock.core.type.EMock;
import com.mzh.emock.core.type.object.definition.EMDefinition;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class EMDefinitionUtil {
    public static final List<Class<?>> MOCK_EXCLUDE= Arrays.asList(
            Class.class, Constructor.class,Method.class, Field.class,
            Type.class, BigDecimal.class, BigInteger.class, AtomicLong.class, AtomicInteger.class,
            Enum.class,String.class,Character.class ,Boolean.class,Byte.class ,
            Short.class  ,Integer.class  ,Long.class,Float.class  ,Double.class
    );

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
                && EMClassUtil.isSubClass(method.getParameterTypes()[0],Supplier.class)
                && method.getReturnType() == EMDefinition.class
                && EMClassUtil.isSubClass(method.getGenericReturnType().getClass(),ParameterizedType.class)
                && ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments() != null
                && ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments().length == 1;
    }

}
