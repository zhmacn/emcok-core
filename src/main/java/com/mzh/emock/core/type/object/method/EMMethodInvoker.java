package com.mzh.emock.core.type.object.method;

import java.lang.reflect.InvocationTargetException;

public interface EMMethodInvoker<R> {
    R invoke(SimpleInvoker<R, Object[]> oldMethod, SimpleInvoker<R, Object[]> newMethod, Object[] args);
    String getCode();

    interface SimpleInvoker<R,A>{
        R invoke(A args) throws InvocationTargetException, IllegalAccessException;
    }
}
