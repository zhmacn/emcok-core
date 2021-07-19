package com.mzh.emock.core.type.object.definition;

import com.mzh.emock.core.exception.EMDefinitionException;
import com.mzh.emock.core.type.EMock;
import com.mzh.emock.core.type.IDObject;
import com.mzh.emock.core.type.object.EMObjectWrapper;
import com.mzh.emock.core.type.object.method.EMMethodInvoker;
import com.mzh.emock.core.util.EMClassUtil;
import com.mzh.emock.core.util.EMDefinitionUtil;
import com.mzh.emock.core.util.EMStringUtil;

import javax.naming.ldap.SortResponseControl;
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
    private final Class<A> paramClz;

    private String name;
    private int order;
    private boolean objEnableMock;
    private boolean methodEnableMock;
    private String[] reverseEnabledMethods;

    private EMObjectWrapper<T,? extends T> wrapper;

    private final Map<Class<?>, Object> annotations = new ConcurrentHashMap<>();


    private final EMMethodInvoker.SimpleInvoker<EMObjectWrapper<T,? extends T>, Supplier<? extends A>> methodInvoker = new EMMethodInvoker.SimpleInvoker<EMObjectWrapper<T,? extends T>, Supplier<? extends A>>() {
        @Override
        @SuppressWarnings("unchecked")
        public EMObjectWrapper<T,? extends T> invoke(Supplier<? extends A> args) throws InvocationTargetException, IllegalAccessException {
            return (EMObjectWrapper<T,? extends T>) srcMethod.invoke(null, args);
        }
    };

    public EMDefinition(Method srcMethod, ClassLoader loader,Class<T> methodReturn,Class<A> methodParameter) throws EMDefinitionException {
        if(srcMethod==null || loader==null || methodReturn==null || methodParameter==null){
            throw new EMDefinitionException("method and loader can not be null");
        }
        if(!EMDefinitionUtil.checkMethod(srcMethod)){
            throw new EMDefinitionException("the method is not a em method {0}",srcMethod.getName());
        }
        Class<?> rc=EMClassUtil.getParameterizedTypeClass(srcMethod.getGenericReturnType()).get(0);
        Class<?> pc=EMClassUtil.getParameterizedTypeClass(srcMethod.getGenericParameterTypes()[0]).get(0);
        if(rc!=methodReturn || pc!=methodParameter){
            throw new EMDefinitionException("parameter error");
        }
        this.paramClz =methodParameter;
        this.mockClzLoader=loader;
        this.srcMethod = srcMethod;
        this.srcClz = srcMethod.getDeclaringClass();
        this.targetClz=methodReturn;
        resolveAnnotations();
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

    public void createObjectWrapper(Supplier<? extends A> args) throws Exception {
        this.wrapper= this.methodInvoker.invoke(args);
    }


    public EMMethodInvoker.SimpleInvoker<EMObjectWrapper<T,? extends T>, Supplier<? extends A>> getMethodInvoker() {
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

    public Class<A> getParamClz() {
        return paramClz;
    }

    public EMObjectWrapper<T,? extends T> getWrapper() {
        return wrapper;
    }
}
