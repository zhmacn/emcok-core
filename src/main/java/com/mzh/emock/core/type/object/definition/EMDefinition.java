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
 *  @param <S> the target mock object type
 *  @param <A> supplier parameter return type
 */
public class EMDefinition<S, A> extends IDObject {

    private final ClassLoader loader;
    private final Method sMethod;
    private final Class<?> sClass;
    private final Class<S> tClass;
    private final Class<A> aClass;

    private String name;
    private int order;
    private boolean objEnableMock;
    private boolean methodEnableMock;
    private String[] reverseEnabledMethods;

    private EMObjectWrapper<S,? extends S> wrapper;

    private final Map<Class<?>, Object> annotations = new ConcurrentHashMap<>();


    private final EMMethodInvoker.SimpleInvoker<EMObjectWrapper<S,? extends S>, Supplier<? extends A>> methodInvoker = new EMMethodInvoker.SimpleInvoker<EMObjectWrapper<S,? extends S>, Supplier<? extends A>>() {
        @Override
        @SuppressWarnings("unchecked")
        public EMObjectWrapper<S,? extends S> invoke(Supplier<? extends A> args) throws InvocationTargetException, IllegalAccessException {
            return (EMObjectWrapper<S,? extends S>) sMethod.invoke(null, args);
        }
    };

    public EMDefinition(Method sMethod, ClassLoader loader,Class<S> rClass,Class<A> aClass) throws EMDefinitionException {
        if(sMethod==null || loader==null || rClass==null || aClass==null){
            throw new EMDefinitionException("method and loader can not be null");
        }
        if(!EMDefinitionUtil.checkMethod(sMethod)){
            throw new EMDefinitionException("the method is not a em method {0}",sMethod.getName());
        }
        Class<?> rc=EMClassUtil.getParameterizedTypeClass(sMethod.getGenericReturnType()).get(0);
        Class<?> pc=EMClassUtil.getParameterizedTypeClass(sMethod.getGenericParameterTypes()[0]).get(0);
        if(rc!=rClass || pc!=aClass){
            throw new EMDefinitionException("parameter error");
        }
        this.aClass = aClass;
        this.loader= loader;
        this.sMethod = sMethod;
        this.sClass = sMethod.getDeclaringClass();
        this.tClass= rClass;
        resolveAnnotations();
    }

    private void resolveAnnotations() {
        Annotation[] annotations = sMethod.getAnnotations();
        for (Annotation annotation : annotations) {
            this.annotations.put(annotation.annotationType(), annotation);
        }
        EMock emock = (EMock) this.annotations.get(EMock.class);
        this.objEnableMock = emock.objectEnableMock();
        this.methodEnableMock = emock.methodEnableMock();
        String name = emock.name();
        this.name = EMStringUtil.isEmpty(name) ? this.sMethod.getName() : name;
        this.reverseEnabledMethods = emock.reverseEnabledMethod();
        this.order = emock.order();
    }

    public void createObjectWrapper(Supplier<? extends A> args) throws Exception {
        this.wrapper= this.methodInvoker.invoke(args);
    }


    public EMMethodInvoker.SimpleInvoker<EMObjectWrapper<S,? extends S>, Supplier<? extends A>> getMethodInvoker() {
        return methodInvoker;
    }

    public Map<Class<?>, Object> getAnnotations() {
        return annotations;
    }

    public int getOrder() {
        return this.order;
    }

    public Class<?> getSClass() {
        return sClass;
    }

    public Class<S> getTClass() {
        return tClass;
    }


    public Method getSMethod() {
        return sMethod;
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

    public Class<A> getAClass() {
        return aClass;
    }

    public EMObjectWrapper<S,? extends S> getWrapper() {
        return wrapper;
    }

    public ClassLoader getLoader(){return loader;}
}
