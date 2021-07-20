package com.mzh.emock.core.type.object.method;


import com.mzh.emock.core.type.IDObject;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EMMethodInfo<R> extends IDObject {
    private String name;
    private boolean isMock;
    private Method sMethod;
    private Map<String, EMMethodInvoker<R>> dynamicInvokers=new ConcurrentHashMap<>();
    private String enabledDynamicInvoker;

    public EMMethodInfo(Method method,boolean isMock){
        this.name=method.getName();
        this.sMethod=method;
        this.isMock=isMock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setMock(boolean mock) {
        isMock = mock;
    }

    public Method getSMethod() {
        return sMethod;
    }

    public void setSMethod(Method sMethod) {
        this.sMethod = sMethod;
    }

    public Map<String, EMMethodInvoker<R>> getDynamicInvokers() {
        return dynamicInvokers;
    }

    public void setDynamicInvokers(Map<String, EMMethodInvoker<R>> dynamicInvokers) {
        this.dynamicInvokers = dynamicInvokers;
    }

    public String getEnabledDynamicInvoker() {
        return enabledDynamicInvoker;
    }

    public void setEnabledDynamicInvoker(String enabledDynamicInvoker) {
        this.enabledDynamicInvoker = enabledDynamicInvoker;
    }
}
