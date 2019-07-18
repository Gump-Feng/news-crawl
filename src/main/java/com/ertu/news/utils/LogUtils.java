package com.ertu.news.utils;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hxf
 * @date 2019/5/6 16:50
 *
 * 系统日志写到数据库的工具类
 */
public class LogUtils {
    private static Logger logger =Logger.getLogger("SYSTEM");

    /**
     * 消息级别
     */
    public static void info(int siteId, String message){
        MDC.put("site_id", siteId);
        logger.info(message);
    }

    public static void debug(int siteId, String message){
        MDC.put("site_id", siteId);
        logger.debug(message);
    }

    public static void error(int siteId, String message){
        MDC.put("site_id", siteId);
        logger.error(message);
    }

    public static void warn(int siteId, String message){
        MDC.put("site_id", siteId);
        logger.warn(message);
    }






    public static Map<String, String> createLogMap(String logInfo, int logLevel, int siteId, Date crawlTime){
        Map<String, String> logInfoMap = new HashMap<>(10);
        logInfoMap.put("site_id",siteId+"");
        logInfoMap.put("log_info",logInfo);
        logInfoMap.put("log_level",logLevel+"");

        if (crawlTime == null){
            crawlTime = new Date();
        }
        String crawlTimeStr = TimeUtils.dateToString(crawlTime, "yyyy-MM-dd HH:mm:dd");
        logInfoMap.put("crawl_time",crawlTimeStr);
        return logInfoMap;
    }


    public static Map<String, Object> createLogTable() {
        Map<String, Object> dataBaseMap = new HashMap<>(16);
        dataBaseMap.put("serverName", "0.120");
        dataBaseMap.put("dbName", "news_test");
        dataBaseMap.put("tableName", "news_crawl_log");
        //获取各个字段名并存储导map中
        Map<String, String> siteIdMap = new HashMap<>(5);
        siteIdMap.put("name", "site_id");
        siteIdMap.put("type", "int");
        siteIdMap.put("length", "10");
        //logInfo
        Map<String, String> logInfoMap = new HashMap<>(5);
        logInfoMap.put("name", "log_info");
        logInfoMap.put("type", "text");
        logInfoMap.put("length", "0");
        //log_level
        Map<String, String> logLevelMap = new HashMap<>(5);
        logLevelMap.put("name", "log_level");
        logLevelMap.put("type", "int");
        logLevelMap.put("length", "1");
        //crawl_time
        Map<String, String> crawlTimeMap = new HashMap<>(5);
        crawlTimeMap.put("name", "crawl_time");
        crawlTimeMap.put("type", "datetime");
        crawlTimeMap.put("length", "0");

        dataBaseMap.put("site_id", siteIdMap);
        dataBaseMap.put("log_info", logInfoMap);
        dataBaseMap.put("log_level", logLevelMap);
        dataBaseMap.put("crawl_time", crawlTimeMap);
        return dataBaseMap;
    }
}
