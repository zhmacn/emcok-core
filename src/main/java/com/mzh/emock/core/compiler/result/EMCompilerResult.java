package com.mzh.emock.core.compiler.result;

import java.util.Map;

/**
 * @author zhma
 * 编译结果类
 * 用于存放编译结果
 */
public class EMCompilerResult {
    private final boolean success;
    private final Map<String,byte[]> result;
    private final Exception exception;
    private EMCompilerResult(boolean s,Map<String,byte[]> res,Exception ex){
        this.success=s;
        this.result=res;
        this.exception=ex;
    }

    public static EMCompilerResult buildSuccess(Map<String,byte[]> result){
        return new EMCompilerResult(true,result,null);
    }
    public static EMCompilerResult buildError(Exception ex){
        return new EMCompilerResult(false,null,ex);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public Map<String, byte[]> getResult() {
        return this.result;
    }

    public Exception getException() {
        return this.exception;
    }
}
