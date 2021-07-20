package com.mzh.emock.core.context;

import com.mzh.emock.core.exception.EMDefinitionException;
import com.mzh.emock.core.log.Logger;
import com.mzh.emock.core.support.EMProxySupport;
import com.mzh.emock.core.type.EMObjectGroup;
import com.mzh.emock.core.type.IDObject;
import com.mzh.emock.core.type.object.EMObjectInfo;
import com.mzh.emock.core.type.object.EMObjectWrapper;
import com.mzh.emock.core.type.object.definition.EMDefinition;
import com.mzh.emock.core.util.EMClassUtil;
import com.mzh.emock.core.util.EMDefinitionUtil;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 默认的mock上下文
 * @author zhma
 */
public class EMAbstractContext extends IDObject implements EMContext {
    private final Logger logger=Logger.get(EMAbstractContext.class);
    private final ClassLoader loader;
    private EMProxySupport proxySupport;
    private final Map<Class<?>,List<EMDefinition<?,?>>> definitionMap=new ConcurrentHashMap<>();
    private final Map<Object,EMObjectGroup<?>> objectGroupMap=new ConcurrentHashMap<>();

    public EMAbstractContext(ClassLoader loader){
        this.loader=loader;
    }
    public EMAbstractContext(){
        this.loader=EMAbstractContext.class.getClassLoader();
    }

    /**
     * 加载指定类（sClass）中的mock定义
     * @param sClass mock方法的定义类
     * @param predicate filter 筛选器
     *                  支持传入筛选器，对指定类的方法进行筛选，在筛选后的方法中查找mock方法
     *                  该参数为空时，仅进行基本的筛选。
     * @throws EMDefinitionException sClass为空时
     */
    protected void loadDefinition(Class<?> sClass, Predicate<? super Method> predicate) throws EMDefinitionException {
        if(sClass==null){
            logger.info("call load definition ,clz is null");
            throw new EMDefinitionException("call load definition ,sClz is null");
        }
        Predicate<? super Method> fPredicate=predicate!=null?predicate:m->true;
        List<Method> methods=EMClassUtil.getAllMethods(sClass, m->EMDefinitionUtil.checkMethod(m) && fPredicate.test(m));
        for (Method m : methods) {
            EMDefinition<?, ?> def = new EMDefinition<>(m,this.loader,
                    EMClassUtil.getParameterizedTypeClass(m.getGenericReturnType()).get(0),
                    EMClassUtil.getParameterizedTypeClass(m.getGenericParameterTypes()[0]).get(0));
            this.addDefinition(def);
        }
    }

    /**
     * 对于指定的mock定义，创造mock对象wrapper
     * 可以根据传入参数来构建wrapper
     *
     * @param tFilter 期望创建wrapper的返回类型
     *                definition中的返回类型为filter的子类时，满足条件
     * @param aFilter 期望创建wrapper的参数类型
     *                definition的参数类型为filter的超类时，满足条件
     * @param args 创建wrapper时的传入参数
     * @param <A> 参数类型（实际参数的超类）
     * @param <T> 返回参数类型
     * @throws Exception 反射执行createWrapper出现错误时
     */
    @SuppressWarnings("unchecked")
    protected <T,A> void createWrapper(Class<T> tFilter,Class<A> aFilter,Supplier<? extends A> args) throws Exception {
        if(args==null || aFilter==null){ return ;}
        for(Class<?> key:this.definitionMap.keySet()){
            if(tFilter==null || EMClassUtil.isSubClass(key,tFilter)){
                List<EMDefinition<?,?>> definitions=this.definitionMap.get(key);
                for(EMDefinition<?,?> definition:definitions){
                   if(EMClassUtil.isSuperClass(definition.getAClass(),aFilter)){
                       EMDefinition<T,? super A> rem=(EMDefinition<T,? super A>)definition;
                       rem.createObjectWrapper(args);
                   }
                }
            }
        }
    }


    /**
     * 对于指定对象，根据已有definition，生成mock对象并保存至当前上下文中
     * @param old 需要生成mock对象的旧对象
     * @param <T> 旧对象的类型
     * @param <S> 旧对象接口
     * @throws Exception 反射进行构建代理对象报错
     */
    @SuppressWarnings("unchecked")
    protected <T extends S,S> void updateMockObjectInfo(T old) throws Exception {
        Set<Class<?>> keys=this.definitionMap.keySet();
        for(Class<?> key:keys){
            if(EMClassUtil.isSubClass(old.getClass(),key)){
                List<EMDefinition<?,?>> definitions=this.definitionMap.get(key);
                for(EMDefinition<?,?> definition:definitions){
                    EMObjectWrapper<S,T> wrapper=(EMObjectWrapper<S,T>) definition.getWrapper();
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
        group.updateMockInfo(new EMObjectInfo<>(mock,definition));
        group.updateProxyHolder(getProxySupport().createProxy(definition.getTClass(),old));
    }
    private EMProxySupport getProxySupport(){
        if(this.proxySupport==null){
            this.proxySupport=new EMProxySupport(this,this.loader);
        }
        return this.proxySupport;
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
        this.definitionMap.computeIfAbsent(def.getTClass(),k->new ArrayList<>());
        this.definitionMap.get(def.getTClass()).add(def);
    }

    @Override
    public Set<Class<?>> getDefinitionKeys() {
        return this.definitionMap.keySet();
    }

    @Override
    public Set<Object> getOldObjects() {
        return this.objectGroupMap.keySet();
    }

    @Override
    public void clearDefinition() {
        this.definitionMap.clear();
    }

    @Override
    public void clearGroup() {
        this.objectGroupMap.clear();
    }
}
