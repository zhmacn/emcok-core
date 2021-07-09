package com.mzh.emock.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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

}
