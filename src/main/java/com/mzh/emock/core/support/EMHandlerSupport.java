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

    private static class ESimpleInvoker<R> implements EMMethodInvoker.SimpleInvoker<R, Object[]> {
        private final Object object;
        private final Method method;

        ESimpleInvoker(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        @SuppressWarnings("unchecked")
        public R invoke(Object[] args) throws InvocationTargetException, IllegalAccessException {
            return (R)method.invoke(object, args);
        }
    }

    private abstract class EInvocationHandler{
        protected final Object oldObject;
        protected final Class<?> injectClz;
        public EInvocationHandler(Object oldObject,Class<?> injectClz){
            this.oldObject=oldObject;
            this.injectClz=injectClz;
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

        protected Object tryNoProxyMethod(Object proxy, Method method, Object[] args){
            String name=method.getName();
            switch (name){
                case "hashCode":
                    return context.getObjectGroup(oldObject).getProxyHolderMap().get(injectClz).getProxyHash();
                case "toString":
                    return proxy.getClass().getName()+"@EM:+"+proxy.hashCode();
                case "equals":
                    return proxy==args[0];
            }
            return null;
        }
        protected Object doMockElse(Object proxy,Method method,Object[] args, DoElse<Object> doElse)throws Throwable{
            MockResult result = doMock(proxy, method, args);
            return result.isMock() ?result.getResult(): doElse.get();
        }

        protected MockResult doMock(Object o, Method method, Object[] args) throws Exception {
            MockResult result=new MockResult();
            List<? extends EMObjectInfo<?, ?>> mockObjectInfoList=null;
            if (context.getObjectGroup(oldObject) != null
                    && context.getObjectGroup(oldObject).getEmMap()!=null
                    && context.getObjectGroup(oldObject).getEmMap().get(injectClz)!=null) {
                mockObjectInfoList= context.getObjectGroup(oldObject).getEmMap().get(injectClz);
            }
            if(mockObjectInfoList==null || mockObjectInfoList.size()==0){
                return result.setMock(false);
            }
            for(EMObjectInfo<?,?> mockObjectInfo:mockObjectInfoList){
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

    private class EInterfaceProxyInvocationHandler extends EInvocationHandler implements InvocationHandler {
        public EInterfaceProxyInvocationHandler(Object oldObject,Class<?> injectClz) {
            super(oldObject,injectClz);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object n=tryNoProxyMethod(proxy,method,args);
            return n==null?doMockElse(proxy,method,args,()->method.invoke(oldObject,args)):n;
        }
    }

    private class EObjectEnhanceInterceptor extends EInvocationHandler implements MethodInterceptor {
        public EObjectEnhanceInterceptor(Object oldObject,Class<?> injectClz) {
            super(oldObject,injectClz);
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object n=tryNoProxyMethod(proxy,method,args);
            return n==null?doMockElse(proxy,method,args,()->method.invoke(oldObject,args)):n;
        }
    }

    private class EProxyHandlerEnhanceInterceptor extends EInvocationHandler implements MethodInterceptor {
        private final InvocationHandler oldHandler;

        public EProxyHandlerEnhanceInterceptor(InvocationHandler oldHandler, Object oldObject,Class<?> injectClz ) {
            super(oldObject, injectClz);
            this.oldHandler = oldHandler;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object noProxyResult=tryNoProxyMethod(proxy,method,args);
            if(noProxyResult==null){
                if(method.getName().equals("invoke")){
                    Method rMethod=(Method)args[0];
                    Object[] rArgs= Arrays.copyOfRange(args,1,args.length-1);
                    return doMockElse(proxy,rMethod,rArgs,()->oldHandler.invoke(oldObject,rMethod,rArgs));
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
