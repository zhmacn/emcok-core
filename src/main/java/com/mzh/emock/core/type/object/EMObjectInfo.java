package com.mzh.emock.core.type.object;

import com.mzh.emock.core.type.IDObject;
import com.mzh.emock.core.type.object.definition.EMDefinition;
import com.mzh.emock.core.type.object.method.EMMethodInfo;
import com.mzh.emock.core.util.EMClassUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EMObjectInfo<S,A> extends IDObject {

    private boolean isMocked;
    private S mockedObject;
    private EMDefinition<S,A> definition;
    private Map<String, EMMethodInfo<?>> invokeMethods=new ConcurrentHashMap<>();

    public EMObjectInfo(S mo, EMDefinition<S,A> df){
        this.isMocked= df.isObjectEnableMock();
        this.mockedObject=mo;
        this.definition=df;
        EMClassUtil.getAllMethods(df.getTClass(),m->m.getDeclaringClass()!=Object.class)
        .forEach(method->{
            EMMethodInfo<?> methodInfo=new EMMethodInfo<>(method,df.isMethodEnableMock());
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

    public S getMockedObject() {
        return mockedObject;
    }

    public void setMockedObject(S mockedObject) {
        this.mockedObject = mockedObject;
    }

    public EMDefinition<S, A> getDefinition() {
        return definition;
    }

    public  void setDefinition(EMDefinition<S, A> definition) {
        this.definition = definition;
    }

    public Map<String, EMMethodInfo<?>> getInvokeMethods() {
        return invokeMethods;
    }

    public void setInvokeMethods(Map<String, EMMethodInfo<?>> invokeMethods) {
        this.invokeMethods = invokeMethods;
    }
}
