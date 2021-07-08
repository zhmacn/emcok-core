package com.mzh.emock.support;

import com.mzh.emock.type.EMObjectGroup;

public interface EMContext {

    <T> EMObjectGroup<T> getObjectGroup(T object);

    <T> void addObjectGroup(EMObjectGroup<T> group);

}
