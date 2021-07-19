package com.mzh.emock.core.type.proxy;

import com.mzh.emock.core.util.entity.EMFieldInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EMProxyHolder<T> {
    private final int proxyHash;
    private final Class<T> targetClass;
    private T proxy;
    private List<EMFieldInfo> injectField;

    public EMProxyHolder(Class<T> targetClass,T proxy) {
        this.targetClass=targetClass;
        this.proxy = proxy;
        this.proxyHash=999000000+new Random().nextInt(1000000);
    }

    public int getProxyHash() {
        return proxyHash;
    }

    public T getProxy() {
        return proxy;
    }

    public void setProxy(T proxy) {
        this.proxy = proxy;
    }


    public List<EMFieldInfo> getInjectField() {
        return injectField;
    }

    public void setInjectField(List<EMFieldInfo> injectField) {
        this.injectField = injectField;
    }

    public void addInjectField(EMFieldInfo fieldInfo){
        if(this.injectField==null){
            injectField=new ArrayList<>();
        }
        injectField.add(fieldInfo);
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }
}
