package com.mzh.emock.type.object;

import com.mzh.emock.type.IDObject;
import com.mzh.emock.type.object.definition.EMDefinition;
import com.mzh.emock.type.object.method.EMMethodInfo;
import com.mzh.emock.util.EMClassUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EMObjectInfo<T,A> extends IDObject {

    private boolean isMocked;
    private T mockedObject;
    private EMDefinition<T,A> definition;
    private Map<String, EMMethodInfo<T>> invokeMethods=new ConcurrentHashMap<>();

    public EMObjectInfo(T mo, EMDefinition<T,A> df){
        this.isMocked= df.isObjectEnableMock();
        this.mockedObject=mo;
        this.definition=df;
        EMClassUtil.getAllMethods(df.getTargetClz(),m->m.getDeclaringClass()!=Object.class)
        .forEach(method->{
            EMMethodInfo<T> methodInfo=new EMMethodInfo<>(method,df.isMethodEnableMock());
            if(Arrays.stream(df.getReverseEnabledMethods()).anyMatch(s->s.equals(method.getName()))){
                methodInfo.setMock(!methodInfo.isMock());
            }
            this.invokeMethods.put(method.getName(),methodInfo);
        });
    }

    public boolean isMocked() {
        return isMocked;
    }

    public void setMocked(boolean mocked) {
        isMocked = mocked;
    }

    public T getMockedObject() {
        return mockedObject;
    }

    public void setMockedObject(T mockedObject) {
        this.mockedObject = mockedObject;
    }

    public EMDefinition<T, A> getDefinition() {
        return definition;
    }

    public  void setDefinition(EMDefinition<T, A> definition) {
        this.definition = definition;
    }

    public Map<String, EMMethodInfo<T>> getInvokeMethods() {
        return invokeMethods;
    }

    public void setInvokeMethods(Map<String, EMMethodInfo<T>> invokeMethods) {
        this.invokeMethods = invokeMethods;
    }
}
