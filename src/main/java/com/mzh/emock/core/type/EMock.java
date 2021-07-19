package com.mzh.emock.core.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法注解，用于标记某个方法是否为生成mock定义的方法
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EMock {
    /**
     * 标记该方法生成的mock定义在mock时的优先级
     * 在mock定义中返回的mock对象类型签名一致时，该优先级生效。
     * 其他情况：优先使用子类的mock对象。
     * 例：
     * 1. 已有类型；
     *  1.1 interface A
     *  1.2 interface B extends A
     *  1.3 class C extends D implements B
     * 2. mock定义
     *  2.1 EMDefinition<A> definitionA(Supplier<X> x)
     *  2.2 EMDefinition<A> definitionA(Supplier<X> x)
     *  2.3 EMDefinition<B> definitionB(Supplier<X> x)
     * 3. 对C实例进行mock时，会同时生成3个mock对象。
     * 4. 使用C对象时：
     *  4.1 若C对象的声明类型为B，则默认使用2.3的mock对象进行处理
     *  4.2 若C对象的声明类型为A，则比较2.1和2.2中的EMock的order值，优先使用order值小的对象
     *
     * @return 优先级，数值越小，优先级越高
     */
    int order() default Integer.MAX_VALUE;

    /**
     * mock定义的名称
     * @return mock定义的名称，若此处定义为空，默认使用定义的方法名作为mock定义名称
     */
    String name() default "";

    /**
     * 根据该定义生成mock对象时，该对象是否默认启用mock
     * @return 是否启用mock，默认为true
     */
    boolean objectEnableMock() default true;

    /**
     * 根据该定义生成mock对象时，该对象中的所有方法是否启用mock
     * @return 是否启用mock，默认为true
     */
    boolean methodEnableMock() default true;

    /**
     * 该对象的哪些方法需要更改默认mock状态
     * @return 需要更改默认mock状态的方法列表
     */
    String[] reverseEnabledMethod() default {};
}
