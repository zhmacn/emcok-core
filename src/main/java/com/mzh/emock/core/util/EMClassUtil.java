package com.mzh.emock.core.util;

import net.sf.cglib.beans.BeanCopier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EMClassUtil {

    public static void recursionClazz(Class<?> curr, Consumer<Class<?>> consumer) {
        while (curr != null) {
            consumer.accept(curr);
            curr = curr.getSuperclass();
        }
    }

    public static Class<?> getRawType(Class<?> srcType){
        if(srcType.isArray()){
            srcType=srcType.getComponentType();
        }
        return srcType;
    }

    public static boolean isReferenceField(Class<?> type) {
        return  !type.isPrimitive();
    }


    public static List<Field> getAllDeclaredFields(Class<?> clz, Function<Field,Boolean> filter) {
        List<Field> res = new ArrayList<>();
        recursionClazz(clz, c -> {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                if (filter.apply(field)) { res.add(field); }
            }
        });
        return res;
    }

    public static List<Method> getAllMethods(Class<?> clz,Function<Method,Boolean> filter){
        List<Method> res=new ArrayList<>();
        recursionClazz(clz,c->{
            Method[] methods=c.getMethods();
            for(Method method:methods){
                if(filter.apply(method)){ res.add(method);}
            }
        });
        return res;
    }

    /**
     * get method parameter Type
     * @param type the generic type
     * @return typeclass
     */
    public static List<Class<?>> getParameterizedTypeClass(Type type){
        if(type instanceof ParameterizedType){
            Type[] types=((ParameterizedType)type).getActualTypeArguments();
            return Arrays.stream(types).map(t->(Class<?>)t).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * if target is a subclass of src
     * @param target target class
     * @param src src class
     * @return result
     */
    public static boolean isSubClass(Class<?> target,Class<?> src){
        return src.isAssignableFrom(target);
    }

    /**
     * if target is a superclass of src
     * @param target target class
     * @param src src class
     * @return result
     */
    public static boolean isSuperClass(Class<?> target,Class<?> src){
        return target.isAssignableFrom(src);
    }
}
