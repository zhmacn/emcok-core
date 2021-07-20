package com.mzh.emock.core.type.proxy;

import com.mzh.emock.core.util.entity.EMFieldInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EMProxyHolder<S> {
    private final int proxyHash;
    private final Class<S> tClass;
    private S proxy;
    private List<EMFieldInfo> injectField;

    public EMProxyHolder(Class<S> tClass,S proxy) {
        this.tClass=tClass;
        this.proxy = proxy;
        this.proxyHash=999000000+new Random().nextInt(1000000);
    }

    public int getProxyHash() {
        return proxyHash;
    }

    public S getProxy() {
        return proxy;
    }

    public void setProxy(S proxy) {
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

    public Class<S> gettClass() {
        return tClass;
    }
}
