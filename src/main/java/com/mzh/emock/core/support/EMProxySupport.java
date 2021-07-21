package com.mzh.emock.core.support;

import com.mzh.emock.core.context.EMContext;
import com.mzh.emock.core.type.EMObjectGroup;
import com.mzh.emock.core.type.handle.NonRecursionSearch;
import com.mzh.emock.core.type.proxy.EMProxyHolder;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class EMProxySupport implements NonRecursionSearch {

    private final EMContext context;
    private final ClassLoader loader;
    private final EMHandlerSupport handlerSupport;

    public EMProxySupport(EMContext context,ClassLoader loader){
        this.context=context;
        this.loader=loader;
        handlerSupport=new EMHandlerSupport(context);
    }

    @SuppressWarnings("unchecked")
    public <T extends S,S> EMProxyHolder<S> createProxy(Class<S> tClass, T old) {
        EMProxyHolder<S> cached = findCreatedProxy(tClass, old);
        if (cached != null) {
            return cached;
        }
        S proxy =(tClass.isInterface() ? createInterfaceProxy(tClass, old)
                : createClassProxy(tClass,old));
        context.getObjectGroup(old).updateProxyHolder(new EMProxyHolder<>(tClass,proxy));
        return (EMProxyHolder<S>) context.getObjectGroup(old).getProxyHolder(tClass);
    }

    private <T extends S,S> S createInterfaceProxy(Class<S> inf, T old) {
        if (old instanceof Proxy) {
            InvocationHandler oldHandler = Proxy.getInvocationHandler(old);
            return inf.cast(Proxy.newProxyInstance(loader, new Class[]{inf},
                    createEnhance(oldHandler.getClass(),
                            handlerSupport.getHandlerEnhanceInterceptor(inf, old, oldHandler))));
        }
        return inf.cast(Proxy.newProxyInstance(loader, new Class[]{inf},
                handlerSupport.getInterfaceHandler(inf,old)));
    }


    private <T extends S,S> S createClassProxy(Class<S> tClass,T old) {
        return createEnhance(tClass, handlerSupport.getEnhanceInterceptor(tClass,old));
    }

    @SuppressWarnings("unchecked")
    private <T extends S,S> EMProxyHolder<S> findCreatedProxy(Class<S> tClass, T old) {
        EMObjectGroup<T> group = context.getObjectGroup(old);
        if (group == null) {
            return null;
        }
        return (EMProxyHolder<S>) group.getProxyHolder(tClass);
    }

    @SuppressWarnings("unchecked")
    private <S> S createEnhance(Class<S> tClass, MethodInterceptor methodInterceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(tClass);
        enhancer.setClassLoader(loader);
        enhancer.setUseCache(false);
        enhancer.setCallback(methodInterceptor);
        Constructor<?>[] cons = tClass.getDeclaredConstructors();
        Constructor<?> usedCon = null;
        for (Constructor<?> con : cons) {
            if (usedCon == null) {
                usedCon = con;
                continue;
            }
            if (con.getParameterCount() < usedCon.getParameterCount()) {
                usedCon = con;
            }
        }
        Object proxy;
        assert usedCon != null;
        if (usedCon.getParameterCount() == 0) {
            proxy = enhancer.create();
        } else {
            Object[] args = new Object[usedCon.getParameterCount()];
            proxy = enhancer.create(usedCon.getParameterTypes(), args);
        }
        return (S) proxy;
    }
}
