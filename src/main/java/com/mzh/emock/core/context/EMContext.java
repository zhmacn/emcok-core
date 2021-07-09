package com.mzh.emock.core.context;

import com.mzh.emock.core.type.EMObjectGroup;
import com.mzh.emock.core.type.object.definition.EMDefinition;

import java.util.List;

public interface EMContext {

    <T> EMObjectGroup<T> getObjectGroup(T object);

    <T> void addObjectGroup(EMObjectGroup<T> group);

    <T> List<EMDefinition<T,?>> getDefinitionList(Class<T> tClass);

    <T,A> void addDefinition(Class<T> tClass, EMDefinition<T,A> def);

}
