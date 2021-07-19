package com.mzh.emock.core.context;

import com.mzh.emock.core.type.EMObjectGroup;
import com.mzh.emock.core.type.object.definition.EMDefinition;

import java.util.List;
import java.util.Set;

/**
 * EMContext
 * 现有mock对象上下文
 * 提供如下基本功能：
 *  1. mock定义管理：
 *  1.1 添加mock定义
 *  1.2 删除mock定义
 *  1.3 根据mock目标类类型查询已存在的mock定义（根据mock的类或接口）
 *  2. mock对象管理：
 *  2.1 添加mock对象
 *  2.2 获取mock对象
 *  2.3 清除指定对象的mock对象
 * @see EMObjectGroup
 * @see EMDefinition
 */
public interface EMContext {

    /**
     * 根据特定对象获取该对象已有的mock信息
     * @param object 查询mock对象的key，查询当前容器中是否有该对象的mock对象
     * @param <T> 该对象的下边界，即：容器中的mock定义、mock实现类均基于T的超类。
     * @return 该object对象对应的mock定义信息、已存在的mock对象以及已生成的代理等信息。
     */
    <T> EMObjectGroup<T> getObjectGroup(T object);

    /**
     * 保存特定对象的mock关联信息
     * @param group 关联信息集合
     * @param <T> mock信息下界，mock信息、mock定义等均基于T的超类。
     */
    <T> void addObjectGroup(EMObjectGroup<T> group);

    /**
     * 获取已生成的mock信息的对象列表
     * @return 已生成mock信息的对象列表
     */
    Set<Object> getOldObjects();

    /**
     * 清空已存在的mock信息
     */
    void clearGroup();

    /**
     * 根据mock定义的返回值信息，获取emock的定义信息
     * 此查询应为精确匹配方式
     * @param tClass emock定义的返回泛型类型的class
     * @param <T> emock定义的返回类型的实际类型
     * @return 满足条件的mock定义列表
     */
    <T> List<EMDefinition<T,?>> getDefinitionList(Class<T> tClass);

    /**
     * 添加mock定义信息
     * @param def mock定义信息
     * @param <T> mock定义信息的返回类型的实际类型
     * @param <A> 生成mock定义信息
     */
    <T,A> void addDefinition(EMDefinition<T,A> def);

    /**
     * 获取定义信息的键值，即：定义的mock类信息
     * @return 定义的mock类信息列表
     */
    Set<Class<?>> getDefinitionKeys();

    /**
     * 清除已存在的mock定义信息
     */
    void clearDefinition();


}
