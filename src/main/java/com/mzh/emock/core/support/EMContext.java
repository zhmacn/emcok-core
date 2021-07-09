package com.mzh.emock.core.support;

import com.mzh.emock.core.type.EMObjectGroup;

public interface EMContext {

    <T> EMObjectGroup<T> getObjectGroup(T object);

    <T> void addObjectGroup(EMObjectGroup<T> group);

}
