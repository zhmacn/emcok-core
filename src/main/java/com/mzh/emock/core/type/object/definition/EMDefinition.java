package com.mzh.emock.core.type.object.definition;

import com.mzh.emock.core.exception.EMDefinitionException;
import com.mzh.emock.core.type.EMock;
import com.mzh.emock.core.type.IDObject;
import com.mzh.emock.core.type.object.EMObjectWrapper;
import com.mzh.emock.core.type.object.method.EMMethodInvoker;
import com.mzh.emock.core.util.EMDefinitionUtil;
import com.mzh.emock.core.util.EMStringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author mzh
 * the mock object definition sources
 * which class and which method should be used to create mocked object
 * the method return a {@link EMObjectWrapper }
 * and accept one parameter {@link java.util.function.Supplier}
 *
 * the definition source method should like this:
 * {@code
 * {@literal @}EMock(name="testMock")<br/>
 *  public EMDefinition<T> mockMethod(Supplier<A> supplier){<br/>
 *      do something;<br/>
 *  }
 * }
 *  @param <T> the target mock object type
 *  @param <A> supplier parameter return type
 */
public class EMDefinition<T, A> extends IDObject {

    private final ClassLoader mockClzLoader;
    private final Method srcMethod;
    private final Class<?> srcClz;
    private final Class<T> targetClz;

    private String name;
    private int order;
    private boolean objEnableMock;
    private boolean methodEnableMock;
    private String[] reverseEnabledMethods;

    private final Map<Class<?>, Object> annotations = new ConcurrentHashMap<>();


    private final EMMethodInvoker.SimpleInvoker<EMObjectWrapper<T>, Supplier<A>> methodInvoker = new EMMethodInvoker.SimpleInvoker<EMObjectWrapper<T>, Supplier<A>>() {
        @Override
        @SuppressWarnings("unchecked")
        public EMObjectWrapper<T> invoke(Supplier<A> args) throws InvocationTargetException, IllegalAccessException {
            return (EMObjectWrapper<T>) srcMethod.invoke(null, args);
        }
    };

    public EMDefinition(Method srcMethod, ClassLoader loader) throws EMDefinitionException,ClassNotFoundException {
        if(srcMethod==null || loader==null){
            throw new EMDefinitionException("method or classloader can not be null");
        }
        if(!EMDefinitionUtil.checkMethod(srcMethod)){
            throw new EMDefinitionException("method {0} is not a emock definition source",srcMethod.getName());
        }
        this.mockClzLoader=loader;
        this.srcMethod = srcMethod;
        this.srcClz = srcMethod.getDeclaringClass();
        this.targetClz=parseTargetClz();
        resolveAnnotations();
    }

    @SuppressWarnings("unchecked")
    private Class<T> parseTargetClz()throws ClassNotFoundException{
        String clzName = ((ParameterizedType)srcMethod .getGenericReturnType()).getActualTypeArguments()[0].getTypeName();
        return (Class<T>)this.mockClzLoader.loadClass(clzName);
    }
    private void resolveAnnotations() {
        Annotation[] annotations = srcMethod.getAnnotations();
        for (Annotation annotation : annotations) {
            this.annotations.put(annotation.annotationType(), annotation);
        }
        EMock emock = (EMock) this.annotations.get(EMock.class);
        this.objEnableMock = emock.objectEnableMock();
        this.methodEnableMock = emock.methodEnableMock();
        String name = emock.name();
        this.name = EMStringUtil.isEmpty(name) ? this.srcMethod.getName() : name;
        this.reverseEnabledMethods = emock.reverseEnabledMethod();
        this.order = emock.order();
    }

    public EMObjectWrapper<T> createObjectWrapper(Supplier<A> args) throws Exception {
        return this.methodInvoker.invoke(args);
    }


    public EMMethodInvoker.SimpleInvoker<EMObjectWrapper<T>, Supplier<A>> getMethodInvoker() {
        return methodInvoker;
    }

    public Map<Class<?>, Object> getAnnotations() {
        return annotations;
    }

    public int getOrder() {
        return this.order;
    }

    public Class<?> getSrcClz() {
        return srcClz;
    }

    public Class<T> getTargetClz() {
        return targetClz;
    }


    public Method getSrcMethod() {
        return srcMethod;
    }

    public String getName() {
        return name;
    }


    public boolean isObjectEnableMock() {
        return objEnableMock;
    }

    public boolean isMethodEnableMock() {
        return methodEnableMock;
    }

    public String[] getReverseEnabledMethods() {
        return reverseEnabledMethods;
    }

}
