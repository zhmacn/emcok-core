package com.mzh.emock.core.type.object;
@FunctionalInterface
public interface EMObjectWrapper<T> {
    T wrap(T t);
}
