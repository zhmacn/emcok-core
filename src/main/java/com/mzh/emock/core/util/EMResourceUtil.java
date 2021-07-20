package com.mzh.emock.core.util;

import java.io.File;

public class EMResourceUtil {
    public static String formatResourcePath(String path) {
        path=path.replace(".", File.separator);
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        if (!path.startsWith("classpath")) {
            path = "classpath*:" + path;
        }
        return path + "**"+File.separator+"*.class";
    }
}
