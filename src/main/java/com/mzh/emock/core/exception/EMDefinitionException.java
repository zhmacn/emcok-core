package com.mzh.emock.core.exception;

import com.mzh.emock.core.util.EMStringUtil;

public class EMDefinitionException extends Exception{
    public EMDefinitionException(String message){
        super(message);
    }
    public EMDefinitionException(String format,Object... args){
        super(EMStringUtil.format(format,args));
    }
}
