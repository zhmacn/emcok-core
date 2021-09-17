package com.mzh.emock.core.support;

import com.mzh.emock.core.context.EMContext;
import com.mzh.emock.core.log.Logger;
import com.mzh.emock.core.type.EMObjectGroup;
import com.mzh.emock.core.type.handle.NonRecursionSearch;
import com.mzh.emock.core.type.proxy.EMProxyHolder;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class EMProxySupport implements NonRecursionSearch {

    private final EMContext context;
    private final ClassLoader loader;
    private final EMHandlerSupport handlerSupport;
    private final Logger logger=Logger.get(EMProxySupport.class);
    private Unsafe unsafe;

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
            if((oldHandler.getClass().getModifiers() & Modifier.FINAL)==0){
                return inf.cast(Proxy.newProxyInstance(loader, new Class[]{inf},
                        createEnhance(oldHandler.getClass(),
                                handlerSupport.getHandlerEnhanceInterceptor(inf, old, oldHandler))));
            }
            logger.warn("target class"+inf.getName()+
                    " is a jdk proxy,but it's handler a instance of final class,"+
                    " will not create proxy for handler!!!");
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
        Optional<Constructor<?>> minCon=Arrays.stream(tClass.getDeclaredConstructors())
                .min(Comparator.comparingInt(Constructor::getParameterCount));
        Object proxy;
        if (minCon.isPresent() && minCon.get().getParameterCount() == 0) {
            enhancer.setCallback(methodInterceptor);
            proxy = enhancer.create();
        } else {
            enhancer.setCallbackType(MethodInterceptor.class);
            Class<?> clz=enhancer.createClass();
            Enhancer.registerStaticCallbacks(clz,new MethodInterceptor[]{methodInterceptor});
            proxy=this.unsafeCreateObject(clz);
        }
        return (S) proxy;
    }

    private Object unsafeCreateObject(Class<?> clazz){
        try{
            if(this.unsafe==null){
                Field field=Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                this.unsafe=(Unsafe) field.get(null);
            }
            return this.unsafe.allocateInstance(clazz);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

}
