package com.ertu.news.start;

import com.lwlh.processor.*;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/**
 * @author hxf
 * @date 2019/3/26 16:24
 */
public class StartAlone {
    private static Logger logger = Logger.getLogger(StartAlone.class);

    public static void main(String[] args) {
        //启动扫描调度任务的线程
        new ScheduledTask().start();
        //启动栏目入口请求解析
        new ConfigProcessor(1).spider();
        //启动深度抓取列表页和详情页的链接
        try {
            Thread.sleep(1000*3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new PageProcessor(1).spider();
        //启动抓取详情页链接的线程
        new NewsProcessor(1).spider();

        //启动监视线程
        new ProcessManager().start();

        MDC.put("site_id",0);
        logger.info("the spider has been started...");
    }
}