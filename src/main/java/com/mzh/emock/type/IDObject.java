package com.mzh.emock.type;

import com.mzh.emock.util.EMObjectUtil;

public abstract class IDObject {
    private final long id;
    public IDObject(){
        this.id= EMObjectUtil.getNextId();
    }

    public long getId() {
        return id;
    }
}
