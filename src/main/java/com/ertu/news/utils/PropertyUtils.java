package com.ertu.news.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * property文件的工具类
 *
 * @author hxf
 */
public class PropertyUtils {
    public static final String FILE_STORE_PATH_PROP = "file.rootDir";
    public static final String CONFIG_PATH_PROP = "config.dir";
    public static final String SQL_CONFIG_PATH = "sqlConfigPath";
	public static void main(String[] args) {
	}
	
    public static String getPathByName(String name) {
        InputStream resource = Object.class.getResourceAsStream("/properties/path.properties");
        Properties properties = new Properties();
        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(resource, StandardCharsets.UTF_8);
            properties.load(inputStreamReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(name);
    }

}
