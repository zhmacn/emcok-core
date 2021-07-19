package com.mzh.emock.core.type.object;

/**
 *
 * @param <S> 生成的mock对象类型
 * @param <T> 待mock对象的类型
 */
@FunctionalInterface
public interface EMObjectWrapper<S,T extends S> {
    S wrap(T t);
}
