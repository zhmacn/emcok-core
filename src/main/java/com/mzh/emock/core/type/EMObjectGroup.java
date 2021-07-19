package com.mzh.emock.core.type;

import com.mzh.emock.core.type.object.EMObjectInfo;
import com.mzh.emock.core.type.proxy.EMProxyHolder;

import java.util.*;

/**
 * mock信息集合，用于保存mock的旧对象，生成的mock对象列表，该对象的代理对象
 * 组织方式：旧对象作为唯一索引，即每一个需要mock的旧对象有且仅有一个EMObjectGroup信息
 *  对于特定对象object,其定义的mock类型签名可能有多个
 *      mock对象信息通过，如下方式保存：
 *          Map<Mock类型（T的超类），List<生成的mock对象信息>>
 *      该对象的代理对象信息，通过如下方式保存：
 *          Map<Mock类型（T的超类），EMProxyHolder<T的超类>>
 *      (T的超类)为Mock定义的返回类型的实际类型。
 * @param <T> mock信息的类型下界
 */
public class EMObjectGroup<T> extends IDObject{
    private final T oldObject;
    private static class SpecificClassGroup<S>{
        private  Class<S> mockSignClass;
        private  EMProxyHolder<S> proxyHolder;
        private  List<EMObjectInfo<S,?>> mockObjects=new ArrayList<>();

        public Class<S> getMockSignClass() {
            return mockSignClass;
        }

        public void setMockSignClass(Class<S> mockSignClass) {
            this.mockSignClass = mockSignClass;
        }

        public EMProxyHolder<S> getProxyHolder() {
            return proxyHolder;
        }

        public void setProxyHolder(EMProxyHolder<S> proxyHolder) {
            this.proxyHolder = proxyHolder;
        }

        public List<EMObjectInfo<S, ?>> getMockObjects() {
            return mockObjects;
        }

        public void setMockObjects(List<EMObjectInfo<S, ?>> mockObjects) {
            this.mockObjects = mockObjects;
        }
    }
    private final List<SpecificClassGroup<? super T>> groupHolder=new ArrayList<>();

    /**
     * 构造器
     * @param oldObject 需要mock的对象
     */
    public  EMObjectGroup(T oldObject){
        this.oldObject=oldObject;
    }

    /**
     * 获取需要mock的对象
     * @return 需要mock的对象
     */
    public T getOldObject() {
        return oldObject;
    }

    /**
     * 更新代理对象信息
     * @param proxyHolder 待更新的代理对象
     */
    public void updateProxyHolder(EMProxyHolder<? super T> proxyHolder){
        this.proxyHolderUpdate(proxyHolder);
    }

    /**
     * 获取指定代理class的代理对象
     * @param targetClass 代理的class
     * @return 代理对象
     */
    public EMProxyHolder<? super T> getProxyHolder(Class<? super T> targetClass){
        return getGroupByClass(targetClass).getProxyHolder();
    }

    /**
     * 更新或添加已生成的mock对象
     * 并进行排序
     * @param emObjectInfo 待更新的mock对象
     */
    public void updateMockInfo(EMObjectInfo<? super T,?> emObjectInfo){
        mockInfoUpdate(emObjectInfo);
    }

    /**
     * 根据指定的代理类，获取代理对象列表
     * @param targetClass 指定的代理类
     * @return 代理对象列表
     */
    public List<? extends EMObjectInfo<? super T,?>> getMockInfo(Class<? super T> targetClass){
        return getGroupByClass(targetClass).getMockObjects();
    }



    private <S> void proxyHolderUpdate(EMProxyHolder<S> holder){
        getGroupByClass(holder.getTargetClass()).setProxyHolder(holder);
    }

    private <S> void mockInfoUpdate(EMObjectInfo<S,?> emObjectInfo){
        List<EMObjectInfo<S, ?>> mockObjects = getGroupByClass(emObjectInfo.getDefinition().getTargetClz()).getMockObjects();
        mockObjects.add(emObjectInfo);
        mockObjects.sort(Comparator.comparingInt(a -> a.getDefinition().getOrder()));
    }

    @SuppressWarnings("unchecked")
    private <S> SpecificClassGroup<S> getGroupByClass(Class<S> clz){
        for(SpecificClassGroup<? super T> si:groupHolder){
            if(si.getMockSignClass()==clz){
                return (SpecificClassGroup<S>) si;
            }
        }
        SpecificClassGroup<S> instance=new SpecificClassGroup<>();
        instance.setMockSignClass(clz);
        groupHolder.add((SpecificClassGroup<? super T>) instance);
        return instance;
    }
}
