package com.mzh.emock.core.context;

import com.mzh.emock.core.exception.EMDefinitionException;
import com.mzh.emock.core.log.Logger;
import com.mzh.emock.core.type.EMObjectGroup;
import com.mzh.emock.core.type.object.EMObjectInfo;
import com.mzh.emock.core.type.object.EMObjectWrapper;
import com.mzh.emock.core.type.object.definition.EMDefinition;
import com.mzh.emock.core.util.EMClassUtil;
import com.mzh.emock.core.util.EMDefinitionUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * default implement of EMContext
 */
public class EMAbstractContext implements EMContext {
    private final Logger logger=Logger.get(EMAbstractContext.class);
    private final ClassLoader loader;
    private final Map<Class<?>,List<EMDefinition<?,?>>> definitionMap=new ConcurrentHashMap<>();
    private final Map<Object,EMObjectGroup<?>> objectGroupMap=new ConcurrentHashMap<>();

    public EMAbstractContext(ClassLoader loader){
        this.loader=loader;
    }
    public EMAbstractContext(){
        this.loader=EMAbstractContext.class.getClassLoader();
    }

    protected <T> boolean loadDefinition(Class<T> tClass,Class<T> aClass) throws EMDefinitionException, ClassNotFoundException {
        if(tClass==null){
            logger.info("call load definition ,clz is null");
            return false;
        }
        List<Method> methods=EMClassUtil.getAllMethods(tClass, m->EMDefinitionUtil.checkMethod(m)
                && (aClass==null || aClass.isAssignableFrom(m.getParameterTypes()[0])));
        for (Method m : methods) {
            EMDefinition<T, ?> def = new EMDefinition<>(m,this.loader);
            this.addDefinition(def.getTargetClz(),def);
        }
        return true;
    }

    protected <T,A> boolean createMock(T old, Supplier<A> args) throws Exception {
        if(old==null){
            return false;
        }
        for(Class<?> key:this.definitionMap.keySet()){
            if(key.isAssignableFrom(old.getClass())){
                updateMockObjectInfo((Class<T>)key,args,old);
            }
        }
        return true;
    }
    private <T,A> void updateMockObjectInfo(Class<T> clz,Supplier<A> args,T old) throws Exception {
        List<EMDefinition<T,?>> definitions=this.getDefinitionList(clz);
        for(EMDefinition<T,?> definition:definitions){
            EMObjectInfo<T,?> info=createMockObjectInfo((EMDefinition<T,A>)definition,args,old);
            EMObjectGroup<T> group=getObjectGroup(old);
            if(group==null){
                group=new EMObjectGroup<>(old);
                addObjectGroup(group);
            }
            group.getEmMap().computeIfAbsent(clz,c->new ArrayList<>()).add(info);
        }
    }
    private <T,A> EMObjectInfo<T,A> createMockObjectInfo(EMDefinition<T,A> definition,Supplier<A> args,T old) throws Exception {
        EMObjectWrapper<T> wrapper=definition.createObjectWrapper(args);
        return new EMObjectInfo<>(wrapper.wrap(old),definition);
    }

    @Override
    public <T> EMObjectGroup<T> getObjectGroup(T object) {
        return (EMObjectGroup<T>) this.objectGroupMap.get(object);
    }

    @Override
    public <T> void addObjectGroup(EMObjectGroup<T> group) {
        this.objectGroupMap.put(group.getOldObject(),group);
    }

    @Override
    public <T> List<EMDefinition<T, ?>> getDefinitionList(Class<T> tClass) {
        return definitionMap.get(tClass);
    }

    @Override
    public <T, A> void addDefinition(Class<T> tClass, EMDefinition<T, A> def) {
        this.definitionMap.computeIfAbsent(tClass,k->new ArrayList<>());
        this.definitionMap.get(tClass).add(def);
    }
}
