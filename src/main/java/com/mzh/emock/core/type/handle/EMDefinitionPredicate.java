package com.mzh.emock.core.type.handle;

import com.mzh.emock.core.type.object.definition.EMDefinition;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class EMDefinitionPredicate <A>{
    private Predicate<EMDefinition<?,? super A>> predicate;
    private Supplier<? extends A> argSupplier;

    public EMDefinitionPredicate(Predicate<EMDefinition<?, ? super A>> predicate, Supplier<? extends A> argSupplier) {
        this.predicate = predicate;
        this.argSupplier = argSupplier;
    }

    public Predicate<EMDefinition<?, ? super A>> getPredicate() {
        return predicate;
    }

    public Supplier<? extends A> getArgSupplier() {
        return argSupplier;
    }
}
