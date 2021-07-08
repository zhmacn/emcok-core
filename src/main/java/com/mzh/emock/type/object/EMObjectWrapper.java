package com.mzh.emock.type.object;
@FunctionalInterface
public interface EMObjectWrapper<T> {
    T wrap(T t);
}
