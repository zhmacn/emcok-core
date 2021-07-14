package com.mzh.emock.core.type;

import com.mzh.emock.core.type.object.EMObjectInfo;
import com.mzh.emock.core.type.proxy.EMProxyHolder;
import com.mzh.emock.core.util.entity.EMObjectMap;

import java.util.List;
import java.util.Map;

public class EMObjectGroup<T> extends IDObject{
    private final T oldObject;
    private final Map<Class<? super T>,List<EMObjectInfo<? super T,?>>> emMap=new EMObjectMap<>();
    private final Map<Class<? super T>,EMProxyHolder<? super T>> proxyHolderMap=new EMObjectMap<>();

    public  EMObjectGroup(T oldObject){
        this.oldObject=oldObject;
    }

    public T getOldObject() {
        return oldObject;
    }

    public Map<Class<? super T>, List<EMObjectInfo<? super T, ?>>> getEmMap() {
        return emMap;
    }

    public Map<Class<? super T>, EMProxyHolder<? super T>> getProxyHolderMap() {
        return proxyHolderMap;
    }
}
