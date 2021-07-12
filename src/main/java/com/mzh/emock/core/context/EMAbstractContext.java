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
     * load mock method form source class (sClass)
     * @param sClass the mock method definition class
     *               which class should be search mock method
     * @param aClass the parameter type of method
     *               filter the mock method to be loaded
     *               example:
     *                  when
     *                     aClass is A
     *                  then
     *                     the method which parameter type is a sign of A will be loaded
     * @param <T> 需要mock的类型
     * @param <A> 参数的类型
     * @throws EMDefinitionException 传入的类为空
     * @throws ClassNotFoundException 未找到需要加载的类
     */
    protected <T,A> void loadDefinition(Class<?> sClass,Class<T> tClass,Class<A> aClass) throws EMDefinitionException, ClassNotFoundException {
        if(sClass==null){
            logger.info("call load definition ,clz is null");
        }
        List<Method> methods=EMClassUtil.getAllMethods(fClass, m->EMDefinitionUtil.checkMethod(m)
                && (aClass==null || m.getParameterTypes()[0].isAssignableFrom(aClass)));
        for (Method m : methods) {
            EMDefinition<?, A> def = new EMDefinition<>(m,this.loader,aClass);
            this.addDefinition(def.getTargetClz(),def);
        }
    }

    /**
     * create wrapper in definition
     *
     *
     * @param tClass 需要生成wrapper的类
     * @param args 生成wrapper所需的参数生成器
     * @param <A> 参数生成器生成的参数的类型
     * @throws Exception 无法找到对应的类等
     */
    protected <T,A> void createWrapper(Class<T> tClass,Class<A> aClass,Supplier<A> args) throws Exception {
        if(tClass==null || args==null){ return ;}
        for(Class<?> key:this.definitionMap.keySet()){
            if(key.isAssignableFrom(tClass)){
                List<EMDefinition<?,?>> definitions=this.definitionMap.get(key);
                for(EMDefinition<?,?> definition:definitions){
                   Class<?> pClz=definition.getParamClz();
                   if(pClz.isAssignableFrom(aClass)){
                       definition.createObjectWrapper(args);
                   }
                }
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
