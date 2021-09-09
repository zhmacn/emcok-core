package com.mzh.emock.core.type.object;

/**
 *
 * @param <S> 待mock对象的类型
 */
@FunctionalInterface
public interface EMObjectWrapper<S> {
    S wrap(S s);
}
