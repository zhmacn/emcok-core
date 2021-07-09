package com.mzh.emock.core.type;

import com.mzh.emock.core.util.EMObjectUtil;

public abstract class IDObject {
    private final long id;
    public IDObject(){
        this.id= EMObjectUtil.getNextId();
    }

    public long getId() {
        return id;
    }
}
