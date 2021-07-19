package com.mzh.emock.core.support;

import com.mzh.emock.core.context.EMContext;
import com.mzh.emock.core.type.object.EMObjectInfo;
import com.mzh.emock.core.type.object.method.EMMethodInfo;
import com.mzh.emock.core.type.object.method.EMMethodInvoker;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EMHandlerSupport {
    private final EMContext context;

    @FunctionalInterface
    private interface DoElse<T>{
        T get() throws Throwable;
    }

    private static class ESimpleInvoker<T,R> implements EMMethodInvoker.SimpleInvoker<R, Object[]> {
        private final T object;
        private final Method method;

        ESimpleInvoker(T object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        @SuppressWarnings("unchecked")
        public R invoke(Object[] args) throws InvocationTargetException, IllegalAccessException {
            return (R)method.invoke(object, args);
        }
    }

    private abstract class EInvocationHandler<T extends S,S>{
        protected final T oldObject;
        protected final Class<S> targetClass;
        public EInvocationHandler(T oldObject,Class<S> targetClass){
            this.oldObject=oldObject;
            this.targetClass=targetClass;
        }
        private class MockResult{
            private boolean mock;
            private Object result;

            public boolean isMock() {
                return mock;
            }

            public MockResult setMock(boolean mock) {
                this.mock = mock;
                return this;
            }

            public Object getResult() {
                return result;
            }

            public MockResult setResult(Object result) {
                this.result = result;
                return this;
            }
        }

        protected Object tryNoProxyMethod(S proxy, Method method, Object[] args){
            String name=method.getName();
            switch (name){
                case "hashCode":
                    return context.getObjectGroup(oldObject).getProxyHolder(targetClass).getProxyHash();
                case "toString":
                    return oldObject.getClass().getName()+"@"+oldObject.hashCode()+":mock by-->"+proxy.getClass()+"@"+proxy.hashCode();
                case "equals":
                    return proxy==args[0];
            }
            return null;
        }
        protected Object doMockElse(S proxy,Method method,Object[] args, DoElse<Object> doElse)throws Throwable{
            MockResult result = doMock(proxy, method, args);
            return result.isMock() ?result.getResult(): doElse.get();
        }

        protected MockResult doMock(S o, Method method, Object[] args) throws Exception {
            MockResult result=new MockResult();

            if (context.getObjectGroup(oldObject) == null || context.getObjectGroup(oldObject).getMockInfo(targetClass).size()==0) {
                return result.setMock(false);
            }
            List<? extends EMObjectInfo<? super T, ?>> mockObjectInfoList=context.getObjectGroup(oldObject).getMockInfo(targetClass);
            for(EMObjectInfo<? super T,?> mockObjectInfo:mockObjectInfoList){
                if(mockObjectInfo.isMocked()){
                    Map<String, ? extends EMMethodInfo<?>> invokeMethods = mockObjectInfo.getInvokeMethods();
                    EMMethodInfo<?> methodInfo = invokeMethods.get(method.getName());
                    if(methodInfo.isMock()) {
                        if (methodInfo.getEnabledDynamicInvoker() != null) {
                            EMMethodInvoker<?> dynamicInvoker = methodInfo.getDynamicInvokers().get(methodInfo.getEnabledDynamicInvoker());
                            Object mocked=dynamicInvoker.invoke(new ESimpleInvoker<>(oldObject, method), new ESimpleInvoker<>(mockObjectInfo.getMockedObject(), method), args);
                            return result.setResult(mocked).setMock(true);
                        }
                        Object mocked=method.invoke(mockObjectInfo.getMockedObject(), args);
                        return result.setResult(mocked).setMock(true);
                    }
                }
            }
            return result.setMock(false);
        }

    }

    private class EInterfaceProxyInvocationHandler<T extends S,S>  extends EInvocationHandler<T,S> implements InvocationHandler {
        public EInterfaceProxyInvocationHandler(T oldObject,Class<S> targetClass) {
            super(oldObject,targetClass);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object n=tryNoProxyMethod((S)proxy,method,args);
            return n==null?doMockElse((S)proxy,method,args,()->method.invoke(oldObject,args)):n;
        }
    }

    private class EObjectEnhanceInterceptor<T extends S,S> extends EInvocationHandler<T,S> implements MethodInterceptor {
        public EObjectEnhanceInterceptor(T oldObject,Class<S> targetClass) {
            super(oldObject,targetClass);
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object n=tryNoProxyMethod((S)proxy,method,args);
            return n==null?doMockElse((S)proxy,method,args,()->method.invoke(oldObject,args)):n;
        }
    }

    private class EProxyHandlerEnhanceInterceptor<T extends S,S> extends EInvocationHandler<T,S> implements MethodInterceptor {
        private final InvocationHandler oldHandler;

        public EProxyHandlerEnhanceInterceptor(InvocationHandler oldHandler, T oldObject,Class<S> targetClass ) {
            super(oldObject, targetClass);
            this.oldHandler = oldHandler;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object noProxyResult=tryNoProxyMethod(targetClass.cast(proxy),method,args);
            if(noProxyResult==null){
                if(method.getName().equals("invoke")){
                    Method rMethod=(Method)args[0];
                    Object[] rArgs= Arrays.copyOfRange(args,1,args.length-1);
                    return doMockElse((S)proxy,rMethod,rArgs,()->oldHandler.invoke(oldObject,rMethod,rArgs));
                }
                return method.invoke(oldHandler,args);
            }
            return noProxyResult;
        }
    }

    public EMHandlerSupport(EMContext context){
        this.context=context;
    }

    public InvocationHandler getInterfaceHandler(Object old,Class<?> injectClz){
        return new EInterfaceProxyInvocationHandler(old,injectClz);
    }

    public MethodInterceptor getEnhanceInterceptor(Object old,Class<?> injectClz){
        return new EObjectEnhanceInterceptor(old,injectClz);
    }

    public MethodInterceptor getHandlerEnhanceInterceptor(InvocationHandler oldHandler, Object oldObject,Class<?> injectClz){
        return new EProxyHandlerEnhanceInterceptor(oldHandler,oldObject,injectClz);
    }
}
