package com.mzh.emock.core.compiler;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhma
 * 从指定的内存字节码中加载类
 */
public class EMMemoryClassLoader extends URLClassLoader {
    private static final EMMemoryClassLoader memoryClassLoader=new EMMemoryClassLoader();
    private static final ThreadLocal<Map<String,byte[]>> localBytes=new ThreadLocal<>();
    public static Class<?> loadFromBytes(String name, Map<String, byte[]> classBytes) throws ClassNotFoundException, IOException {
        localBytes.set(classBytes);
        return memoryClassLoader.loadClass(name);
    }
    private EMMemoryClassLoader() {
        super(new URL[0],EMMemoryClassLoader.class.getClassLoader());
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if(localBytes.get()==null || localBytes.get().get(name)==null){
            localBytes.remove();
            throw new ClassNotFoundException("load class from byte map,name:"+name);
        }
        byte[] code=localBytes.get().get(name);
        Class<?> clz= defineClass(name, code, 0, code.length);
        localBytes.remove();
        return clz;
    }

}
