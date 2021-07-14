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
import java.util.*;
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
     * @param tFilter the return type of method
     *               filter the mock method by the generic type of return type EMDefinition
     * @param aFilter the parameter type of method
     *               filter the mock method by the generic type of param type Supplier
     * @param <T> the generic type of EMDefinition
     * @param <A> the generic type of Supplier
     * @throws EMDefinitionException sClass is null
     */
    protected <T,A> void loadDefinition(Class<?> sClass,Class<T> tFilter,Class<A> aFilter) throws EMDefinitionException {
        if(sClass==null){
            logger.info("call load definition ,clz is null");
            throw new EMDefinitionException("call load definition ,sClz is null");
        }
        List<Method> methods=EMClassUtil.getAllMethods(sClass, m->EMDefinitionUtil.checkMethod(m)
                && (aFilter==null || EMClassUtil.isSuperClass(EMClassUtil.getParameterizedTypeClass(m.getGenericParameterTypes()[0]).get(0),aFilter))
                && (tFilter==null || EMClassUtil.isSubClass(EMClassUtil.getParameterizedTypeClass(m.getGenericReturnType()).get(0),tFilter))
        );
        for (Method m : methods) {
            EMDefinition<?, ?> def = new EMDefinition<>(m,this.loader,
                    EMClassUtil.getParameterizedTypeClass(m.getGenericReturnType()).get(0),
                    EMClassUtil.getParameterizedTypeClass(m.getGenericParameterTypes()[0]).get(0));
            this.addDefinition(def);
        }
    }

    /**
     * create wrapper in definition
     *
     *
     * @param tFilter the return type of the method
     *                filter the definition by return type to create wrapper
     * @param aFilter the parameter type of method
     *                filter the definition by parameter type to create wrapper
     * @param args the parameters to create wrapper
     * @param <A> the parameter type
     * @param <T> the return type
     * @throws Exception 无法找到对应的类等
     */
    @SuppressWarnings("unchecked")
    protected <T,A> void createWrapper(Class<T> tFilter,Class<A> aFilter,Supplier<? extends A> args) throws Exception {
        if(args==null || aFilter==null){ return ;}
        for(Class<?> key:this.definitionMap.keySet()){
            if(tFilter==null || EMClassUtil.isSubClass(key,tFilter)){
                List<EMDefinition<?,?>> definitions=this.definitionMap.get(key);
                for(EMDefinition<?,?> definition:definitions){
                   if(EMClassUtil.isSuperClass(definition.getParamClz(),aFilter)){
                       EMDefinition<T,? super A> rem=(EMDefinition<T,? super A>)definition;
                       rem.createObjectWrapper(args);
                   }
                }
            }
        }
    }


    /**
     * create or update mock info by old object
     * @param old the oldObject
     * @param <T> the old object type
     * @throws Exception throw exception when call invoke method
     */
    @SuppressWarnings("unchecked")
    private <T extends S,S> void updateMockObjectInfo(T old) throws Exception {
        Set<Class<?>> keys=this.definitionMap.keySet();
        for(Class<?> key:keys){
            if(EMClassUtil.isSubClass(old.getClass(),key)){
                List<EMDefinition<?,?>> definitions=this.definitionMap.get(key);
                for(EMDefinition<?,?> definition:definitions){
                    EMObjectWrapper<S> wrapper=(EMObjectWrapper<S>) definition.getWrapper();
                    if(wrapper==null){
                        logger.info("update mock object warning,no wrapper,object class : "
                                +old.hashCode()+",definition id :"+definition.getId());
                        continue;
                    }
                    update(old,wrapper.wrap(old),(EMDefinition<S, ?>) definition);
                }
            }
        }
    }

    private <T extends S,S> void update(T old,S mock,EMDefinition<S,?> definition){
        this.objectGroupMap.computeIfAbsent(old,o->new EMObjectGroup<>(old));
        EMObjectGroup<T> group=getObjectGroup(old);
        group.getEmMap().computeIfAbsent(definition.getTargetClz(),c->new ArrayList<>());
        group.getEmMap().get(definition.getTargetClz()).add(new EMObjectInfo<>(mock,definition));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> EMObjectGroup<T> getObjectGroup(T object) {
        return (EMObjectGroup<T>) this.objectGroupMap.get(object);
    }

    @Override
    public <T> void addObjectGroup(EMObjectGroup<T> group) {
        this.objectGroupMap.put(group.getOldObject(),group);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<EMDefinition<T, ?>> getDefinitionList(Class<T> tClass) {
        List<EMDefinition<?,?>> definitions=this.definitionMap.get(tClass);
        List<EMDefinition<T,?>> rl=new ArrayList<>();
        for(EMDefinition<?,?> definition:definitions) {
            EMDefinition<T, ?> rdm = (EMDefinition<T, ?>) definition;
            rl.add(rdm);
        }
        return Collections.unmodifiableList(rl);

    }

    @Override
    public <T, A> void addDefinition(EMDefinition<T, A> def) {
        this.definitionMap.computeIfAbsent(def.getTargetClz(),k->new ArrayList<>());
        this.definitionMap.get(def.getTargetClz()).add(def);
    }

    @Override
    public Set<Class<?>> getDefinitionKeys() {
        return this.definitionMap.keySet();
    }

    @Override
    public Set<Object> getOldObjects() {
        return this.objectGroupMap.keySet();
    }
}
