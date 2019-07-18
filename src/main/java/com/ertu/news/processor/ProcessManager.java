package com.ertu.news.processor;

import com.ertu.news.model.bean.Site;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hxf
 * @date 2019/7/8 9:33
 * <p>
 * 监视、维护NewsProcessor的运行：
 * 每隔30分钟newsProcessor向ProcessManager汇报一次运行情况，
 * 若超过两次未汇报，则停掉该线程并把正在消费的资讯保存，重新创建线程进行工作
 */
public class ProcessManager extends Thread {

    private static Map<String, Map<String, Object>> monitorMap = new HashMap<>();
    private Logger logger = Logger.getLogger(ProcessManager.class);

    @Override
    public void run() {
        Map<String, Map<String, Object>> oldMonitorMap = new HashMap<>(32);
        while (true) {
            MDC.put("site_id", 10);
            ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
            int activeCount = threadGroup.activeCount();
            Thread[] threads = new Thread[activeCount];
            threadGroup.enumerate(threads);
            if (!oldMonitorMap.isEmpty()) {
                for (Map.Entry<String, Map<String, Object>> threadMap : monitorMap.entrySet()) {
                    String threadName = threadMap.getKey();
                    Map<String, Object> newDateMap = threadMap.getValue();
                    Site site = (Site) threadMap.getValue().get("site");
                    Map<String, Object> oldDateMap = oldMonitorMap.get(threadName);
                    if (newDateMap == oldDateMap) {
                        //表示NewsProcessor的线程发生异常
                        for (int i = 0; i < activeCount; i++) {
                            String name = threads[i].getName();
                            if (threadName.equals(name)) {
                                //停掉卡死的线程，开启新线程
                                threads[i].interrupt();
                                boolean interrupted = threads[i].isInterrupted();
                                if (interrupted) {
                                    logger.info("线程：" + threadName + "出现异常，已被停止");
                                }else {
                                    logger.info("线程：" + threadName + "停止异常！！");
                                }
                                TaskManager.addJobTask(site);
                                NewsProcessor newsProcessorThread = new NewsProcessor(1);
                                logger.info("详情页抓取线程添加完成");
                                newsProcessorThread.spider();
                            }
                        }
                    }
                }
            }

            try {
                oldMonitorMap = monitorMap;
                Thread.sleep(1000 * 60 * 30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 修改资讯详情页线程的状态修改
     *
     * @param threadName 线程名
     * @param site       正在抓取的网站
     * @param date       时间
     */
    static void checkIn(String threadName, Site site, Date date) {
        Map<String, Object> siteMap = new HashMap<>();
        siteMap.put("date", date);
        siteMap.put("site", site);
        monitorMap.put(threadName, siteMap);
    }

}