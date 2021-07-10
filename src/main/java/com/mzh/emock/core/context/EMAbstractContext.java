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
 * @author zhma
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

    /**
     * 加载指定类中包含的emock定义
     * @param tClass 需要加载mock定义的类
     * @param aClass 加载该类参数的mock定义方法
     * @param <T> 需要mock的类型
     * @param <A> 参数的类型
     * @throws EMDefinitionException 传入的类为空
     * @throws ClassNotFoundException 未找到需要加载的类
     */
    protected <T,A> void loadDefinition(Class<T> tClass,Class<A> aClass) throws EMDefinitionException, ClassNotFoundException {
        if(tClass==null){
            logger.info("call load definition ,clz is null");
        }
        List<Method> methods=EMClassUtil.getAllMethods(tClass, m->EMDefinitionUtil.checkMethod(m)
                && (aClass==null || aClass.isAssignableFrom(m.getParameterTypes()[0])));
        for (Method m : methods) {
            EMDefinition<T, A> def = new EMDefinition<>(m,this.loader,aClass);
            this.addDefinition(def.getTargetClz(),def);
        }
    }

    /**
     * 对于特定对象，创建其mock对象wrapper，并保存在当前context中
     *
     * @param tClass 需要生成wrapper的类
     * @param args 生成wrapper所需的参数生成器
     * @param <A> 参数生成器生成的参数的类型
     * @throws Exception 无法找到对应的类等
     */
    protected <T,A> void createWrapper(Class<T> tClass, Supplier<A> args) throws Exception {
        if(tClass==null || args==null){ return ;}
        for(Class<?> key:this.definitionMap.keySet()){
            if(key.isAssignableFrom(tClass)){
                updateMockObjectInfo(key.cast(old),key,args);
            }
        }
    }


    private <T,ST,A> void updateMockObjectInfo(Class<T> clz,ST old,Supplier<A> args) throws Exception {
        List<EMDefinition<T,?>> definitions=this.getDefinitionList(clz);
        for(EMDefinition<T,?> definition:definitions){
            if(definition.getParamClz().isAssignableFrom(aClass)) {
                EMObjectInfo<T, A> info = createMockObjectInfo((EMDefinition<T, ? super A>) definition, args, old);
                EMObjectGroup<T> group = getObjectGroup(old);
                if (group == null) {
                    group = new EMObjectGroup<>(old);
                    addObjectGroup(group);
                }
                group.getEmMap().computeIfAbsent(clz, c -> new ArrayList<>()).add(info);
            }
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
