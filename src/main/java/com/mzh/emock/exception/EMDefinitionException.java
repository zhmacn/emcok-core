package com.mzh.emock.exception;

import com.mzh.emock.util.EMStringUtil;

import java.util.Formatter;

public class EMDefinitionException extends Exception{
    public EMDefinitionException(String message){
        super(message);
    }
    public EMDefinitionException(String format,Object... args){
        super(EMStringUtil.format(format,args));
    }
}
