package com.mzh.emock.core.support;

import com.mzh.emock.core.context.EMContext;
import com.mzh.emock.core.type.EMObjectGroup;
import com.mzh.emock.core.type.proxy.EMProxyHolder;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class EMProxySupport {

    private final EMContext context;
    private final ClassLoader loader;
    private final EMHandlerSupport handlerSupport;

    public EMProxySupport(EMContext context,ClassLoader loader){
        this.context=context;
        this.loader=loader;
        handlerSupport=new EMHandlerSupport(context);
    }

    @SuppressWarnings("unchecked")
    public <T extends S,S> EMProxyHolder<S> createProxy(Class<S> targetClz, T oldBean) {
        EMProxyHolder<S> cached = findCreatedProxy(targetClz, oldBean);
        if (cached != null) {
            return cached;
        }
        S proxy =(targetClz.isInterface() ? createInterfaceProxy(targetClz, oldBean)
                : createClassProxy(oldBean, targetClz));
        context.getObjectGroup(oldBean).updateProxyHolder(new EMProxyHolder<>(targetClz,proxy));
        return (EMProxyHolder<S>) context.getObjectGroup(oldBean).getProxyHolder(targetClz);
    }

    private <T extends S,S> S createInterfaceProxy(Class<S> inf, T oldBean) {
        if (oldBean instanceof Proxy) {
            InvocationHandler oldHandler = Proxy.getInvocationHandler(oldBean);
            return inf.cast(Proxy.newProxyInstance(loader, new Class[]{inf},
                    createEnhance(oldHandler, handlerSupport.getHandlerEnhanceInterceptor(
                            oldHandler, oldBean, inf))));
        }
        return inf.cast(Proxy.newProxyInstance(loader, new Class[]{inf},
                handlerSupport.getInterfaceHandler(oldBean, inf)));
    }


    private <T> T createClassProxy(T oldBean,Class<?> injectClz) {
        return createEnhance(oldBean, handlerSupport.getEnhanceInterceptor(oldBean, injectClz));
    }

    @SuppressWarnings("unchecked")
    private <T extends S,S> EMProxyHolder<S> findCreatedProxy(Class<S> targetClz, T oldBean) {
        EMObjectGroup<T> group = context.getObjectGroup(oldBean);
        if (group == null) {
            return null;
        }
        return (EMProxyHolder<S>) group.getProxyHolder(targetClz);
    }

    @SuppressWarnings("unchecked")
    private <T> T createEnhance(T old, MethodInterceptor methodInterceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(old.getClass());
        enhancer.setClassLoader(loader);
        enhancer.setUseCache(false);
        enhancer.setCallback(methodInterceptor);
        Constructor<?>[] cons = old.getClass().getDeclaredConstructors();
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
        return (T) proxy;
    }
}
