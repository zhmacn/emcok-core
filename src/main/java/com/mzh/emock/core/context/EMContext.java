package com.mzh.emock.core.context;

import com.mzh.emock.core.type.EMObjectGroup;
import com.mzh.emock.core.type.object.definition.EMDefinition;

import java.util.List;
import java.util.Set;

public interface EMContext {

    /**
     * find runtime mock object group by old object
     * @param object object
     * @param <T> object type
     * @return object group
     */
    <T> EMObjectGroup<T> getObjectGroup(T object);

    <T> void addObjectGroup(EMObjectGroup<T> group);

    <T> List<EMDefinition<T,?>> getDefinitionList(Class<T> tClass);

    <T,A> void addDefinition(EMDefinition<T,A> def);

    Set<Class<?>> getDefinitionKeys();

    Set<Object> getOldObjects();

}
