package com.mzh.emock.core.support;

import com.mzh.emock.core.context.EMContext;
import com.mzh.emock.core.type.handle.NonRecursionSearch;
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

public class EMHandlerSupport implements NonRecursionSearch {
    private final EMContext context;

    @FunctionalInterface
    private interface DoElse<T>{
        T get() throws Throwable;
    }

    private static class ESimpleInvoker<T,R> implements EMMethodInvoker.SimpleInvoker<R, Object[]> ,NonRecursionSearch{
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

    private abstract class EInvocationHandler<T extends S,S> implements NonRecursionSearch{
        protected final T old;
        protected final Class<S> tClass;
        public EInvocationHandler(Class<S> tClass,T old){
            this.old=old;
            this.tClass=tClass;
        }
        protected class ExecResult{
            private boolean exec;
            private Object result;

            public boolean isExec() {
                return exec;
            }

            public ExecResult setExec(boolean exec) {
                this.exec = exec;
                return this;
            }

            public Object getResult() {
                return result;
            }

            public ExecResult setResult(Object result) {
                this.result = result;
                return this;
            }
        }
        //对于object代理的非代理方法处理，proxy对象为old（S）类型的代理对象
        protected ExecResult tryObjectNoProxyMethod(S proxy, Method method, Object[] args){
            ExecResult result=new ExecResult();
            String name=method.getName();
            switch (name){
                case "hashCode":
                    return result.setResult(context.getObjectGroup(old).getProxyHolder(tClass).getProxyHash())
                            .setExec(true);
                case "toString":
                    return result.setResult(old.getClass().getName()+"@"+old.hashCode()+":mock by-->"+proxy.getClass()+"@"+proxy.hashCode())
                            .setExec(true);
                case "equals":
                    return result.setResult(proxy==args[0]).setExec(true);
            }
            return result.setExec(false);
        }
        protected ExecResult tryHandlerNoProxyMethod(InvocationHandler proxy,Method method,Object[] args){
            ExecResult result=new ExecResult();
            String name=method.getName();
            switch (name){
                case "hashCode":
                    return result.setResult(-1*context.getObjectGroup(old).getProxyHolder(tClass).getProxyHash())
                            .setExec(true);
                case "toString":
                    return result.setResult(old.getClass().getName()+"@"+old.hashCode()+"'s handler :mock by-->"+proxy.getClass()+"@"+proxy.hashCode())
                            .setExec(true);
                case "equals":
                    return result.setResult(proxy==args[0])
                            .setExec(true);
            }
            return result.setExec(false);
        }

        protected Object doMockElse(S proxy,Method method,Object[] args, DoElse<Object> doElse)throws Throwable{
            ExecResult result = doMock(proxy, method, args);
            return result.isExec() ?result.getResult(): doElse.get();
        }

        protected ExecResult doMock(S o, Method method, Object[] args) throws Exception {
            ExecResult result=new ExecResult();

            if (context.getObjectGroup(old) == null || context.getObjectGroup(old).getMockInfo(tClass).size()==0) {
                return result.setExec(false);
            }
            List<? extends EMObjectInfo<? super T, ?>> mockObjectInfoList=context.getObjectGroup(old).getMockInfo(tClass);
            for(EMObjectInfo<? super T,?> mockObjectInfo:mockObjectInfoList){
                if(mockObjectInfo.isMocked()){
                    Map<String, ? extends EMMethodInfo<?>> invokeMethods = mockObjectInfo.getInvokeMethods();
                    EMMethodInfo<?> methodInfo = invokeMethods.get(method.getName());
                    if(methodInfo.isMock()) {
                        if (methodInfo.getEnabledDynamicInvoker() != null) {
                            EMMethodInvoker<?> dynamicInvoker = methodInfo.getDynamicInvokers().get(methodInfo.getEnabledDynamicInvoker());
                            Object mocked=dynamicInvoker.invoke(new ESimpleInvoker<>(old, method), new ESimpleInvoker<>(mockObjectInfo.getMockedObject(), method), args);
                            return result.setResult(mocked).setExec(true);
                        }
                        Object mocked=method.invoke(mockObjectInfo.getMockedObject(), args);
                        return result.setResult(mocked).setExec(true);
                    }
                }
            }
            return result.setExec(false);
        }

    }

    private class EInterfaceProxyInvocationHandler<T extends S,S>  extends EInvocationHandler<T,S> implements InvocationHandler {
        public EInterfaceProxyInvocationHandler(Class<S> tClass,T old) {
            super(tClass,old);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            ExecResult execResult=tryObjectNoProxyMethod((S)proxy,method,args);
            return execResult.isExec()?execResult.getResult():doMockElse((S)proxy,method,args,()->method.invoke(old,args));
        }
    }

    private class EObjectEnhanceInterceptor<T extends S,S> extends EInvocationHandler<T,S> implements MethodInterceptor {
        public EObjectEnhanceInterceptor(Class<S> tClass,T old) {
            super(tClass,old);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            ExecResult execResult=tryObjectNoProxyMethod((S)proxy,method,args);
            return execResult.isExec()?execResult.getResult():doMockElse((S)proxy,method,args,()->method.invoke(old,args));
        }
    }

    private class EProxyHandlerEnhanceInterceptor<T extends S,S> extends EInvocationHandler<T,S> implements MethodInterceptor {
        private final InvocationHandler oldHandler;

        public EProxyHandlerEnhanceInterceptor(Class<S> tClass,T old,InvocationHandler oldHandler) {
            super(tClass,old);
            this.oldHandler = oldHandler;
        }

        @Override
        @SuppressWarnings("unchecked")
        //此处的代理对象Proxy为oldHandler的代理对象，非old的代理对象
        //其中的args[0]
        //其中的args[1]
        //其中的args[2]--args[args.length-1]为真实参数
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            //判断是否需要执行代理对象的默认方法，执行：toString、hashCode、equals，认为是需要执行代理对象的该方法。
            ExecResult execResult=tryHandlerNoProxyMethod(oldHandler.getClass().cast(proxy),method,args);
            if(!execResult.isExec()){
                if(method.getName().equals("invoke")){
                    S rProxy=(S)args[0];
                    Method rMethod=(Method)args[1];
                    Object[] rArgs= (Object[]) args[2];
                    ExecResult objectExecResult=tryObjectNoProxyMethod(this.old,rMethod,null);
                    return objectExecResult.isExec()?objectExecResult.getResult():doMockElse(rProxy,rMethod,rArgs,()->oldHandler.invoke(old,rMethod,rArgs));
                }
                return method.invoke(oldHandler,args);
            }
            return execResult.getResult();
        }
    }

    public EMHandlerSupport(EMContext context){
        this.context=context;
    }

    public <T extends S,S> InvocationHandler getInterfaceHandler(Class<S> tClass,T old){
        return new EInterfaceProxyInvocationHandler<>(tClass,old);
    }

    public <T extends S,S> MethodInterceptor getEnhanceInterceptor(Class<S> tClass,T old){
        return new EObjectEnhanceInterceptor<>(tClass,old);
    }

    public <T extends S,S> MethodInterceptor getHandlerEnhanceInterceptor(Class<S> tClass, T old,InvocationHandler oldHandler){
        return new EProxyHandlerEnhanceInterceptor<>(tClass,old,oldHandler);
    }
}
