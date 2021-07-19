package com.mzh.emock.core.compiler;

import com.mzh.emock.core.compiler.result.EMCompilerResult;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

/**
 * compiler tool
 * 编译工具
 */
public class EMRTCompiler {

    private static final JavaCompiler compiler;
    private static final StandardJavaFileManager stdManager;
    private static final DiagnosticCollector<JavaFileObject> diagnosticCollector;
    static{
        compiler = ToolProvider.getSystemJavaCompiler();
        diagnosticCollector=new DiagnosticCollector<>();
        stdManager = compiler.getStandardFileManager(diagnosticCollector, Locale.getDefault(), StandardCharsets.UTF_8);
    }

    /**
     * 提供实时编译支持
     * @param fileName 待编译的文件名
     * @param source 字符串形式的源码
     * @return 编译结果
     */
    public static EMCompilerResult compile(String fileName, String source) {
        MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager);
        JavaFileObject javaFileObject = manager.makeStringSource(fileName, source);
        CompilationTask task = compiler.getTask(null, manager, diagnosticCollector, null, null, Collections.singletonList(javaFileObject));
        Boolean result = task.call();
        if (result == null || !result) {
            String diagnostic= diagnosticCollector.getDiagnostics().toString().replace(fileName,"EM:");
            return EMCompilerResult.buildError(new RuntimeException("Compilation failed:"+diagnostic));
        }
        return EMCompilerResult.buildSuccess(manager.getClassBytes());
    }

}
