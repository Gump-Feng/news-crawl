package com.ertu.news.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * ClazzUtils
 *
 * @author ZENG.XIAO.YAN
 * @version 1.0
 */
public class ClazzUtils {
    private static final String CLASS_SUFFIX = ".class";
    private static final String CLASS_FILE_PREFIX = File.separator + "classes" + File.separator;
    private static final String PACKAGE_SEPARATOR = ".";
    private static final String GOAL_PACKAGE = "com.lwlh.statefulsite";

    /**
     * 查找包下的所有类的名字
     *
     * @return List集合，内容为类的全名
     */
    public static ConcurrentLinkedQueue<String> getClazzName() {
        ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<>();
        String suffixPath = GOAL_PACKAGE.replaceAll("\\.", "/");
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> urls = loader.getResources(suffixPath);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url != null) {
                    String protocol = url.getProtocol();
                    if ("file".equals(protocol)) {
                        String path = url.getPath();
                        result.addAll(getAllClassNameByFile(new File(path), false));
                    } else if ("jar".equals(protocol)) {
                        JarFile jarFile = null;
                        try {
                            jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (jarFile != null) {
                            result.addAll(getAllClassNameByJar(jarFile));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        result.remove("com.lwlh.statefulsite.StatefulSite");
        result.remove("com.lwlh.statefulsite.StatefulStart");
        return result;
    }


    /**
     * 递归获取所有class文件的名字
     *
     * @param file 文件
     * @param flag 是否需要迭代遍历
     * @return List
     */
    private static List<String> getAllClassNameByFile(File file, boolean flag) {
        List<String> result = new ArrayList<>();
        if (!file.exists()) {
            return result;
        }
        if (file.isFile()) {
            getClassName(result, file);
            return result;

        } else {
            File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File f : listFiles) {
                    if (flag) {
                        result.addAll(getAllClassNameByFile(f, true));
                    } else {
                        if (f.isFile()) {
                            getClassName(result, f);
                        }
                    }
                }
            }
            return result;
        }
    }

    private static void getClassName(List<String> result, File f) {
        String path = f.getPath();
        if (path.endsWith(CLASS_SUFFIX)) {
            path = path.replace(CLASS_SUFFIX, "");
            // 从"/classes/"后面开始截取
            String clazzName = path.substring(path.indexOf(CLASS_FILE_PREFIX) + CLASS_FILE_PREFIX.length())
                    .replace(File.separator, PACKAGE_SEPARATOR);
            if (!clazzName.contains("$")) {
                result.add(clazzName);
            }
        }
    }


    /**
     * 递归获取jar所有class文件的名字
     *
     * @param jarFile jar
     * @return List
     */
    private static List<String> getAllClassNameByJar(JarFile jarFile) {
        List<String> result = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String name = jarEntry.getName();
            // 判断是不是class文件
            if (name.endsWith(CLASS_SUFFIX)) {
                name = name.replace(CLASS_SUFFIX, "").replace("/", ".");
                // 如果不要子包的文件,那么就必须保证最后一个"."之前的字符串和包名一样且不是内部类
                if (GOAL_PACKAGE.equals(name.substring(0, name.lastIndexOf("."))) && !name.contains("$")) {
                    result.add(name);
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        ConcurrentLinkedQueue<String> list = ClazzUtils.getClazzName();
        for (String string : list) {
            System.out.println(string);
        }
    }
}