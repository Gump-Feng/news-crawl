package com.ertu.news.processor;

import com.ertu.news.analysis.XmlAnalysis;
import com.ertu.news.io.sql.JdbcOperate;
import com.ertu.news.model.bean.ConfigBean;
import com.ertu.news.model.bean.Site;
import com.ertu.news.model.bean.SiteBean;
import com.ertu.news.utils.PropertyUtils;
import com.ertu.news.utils.TimeUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hxf
 * @date 2019/3/21 15:31
 * 用于获取栏目采集的配置文件和调度时间
 */
public class ScheduledTask extends Thread {

    static boolean isNormal = true;
    private static Logger logger = Logger.getLogger(ScheduledTask.class);
    private Connection connection = null;


    @Override
    public void run() {
        List<Site> sourceConfigList = new ArrayList<>();
        //获取指定路径下的所有配置文件
        String xmlRootDir = PropertyUtils.getPathByName(PropertyUtils.CONFIG_PATH_PROP);
        MDC.put("site_id", 1);
        logger.info("the spider gets the config path :" + xmlRootDir);
        while (isNormal) {
            XmlAnalysis.traverseConfigByDir(xmlRootDir, sourceConfigList);

            sourceConfigList.forEach(site -> {
                SiteBean siteBean = site.getConfigBean().getSiteBean();
                Map<String, Object> websiteDbMap = siteBean.getDbTableConfig().get(XmlAnalysis.WEBSITE_TABLE_TAG);
                //解决和mysql的数据库连接中断的问题
                try {
                    if (connection == null || connection.isClosed()) {
                        if (connection != null){
                            connection.close();
                        }
                        connection = JdbcOperate.getSqlConn(websiteDbMap);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                //设置日志中的特殊字段
                MDC.put("site_id", siteBean.getSiteId());
                if (site.getConfigBean().getGeneralConfig().isEnabled()) {
                    //把栏目信息插入到website表
                    ConfigBean configBean = site.getConfigBean();
                    MDC.put("site_id", siteBean.getSiteId());
                    try {
                        JdbcOperate.createTable(websiteDbMap, connection);
                    } catch (Exception e) {
                        logger.error(siteBean.getWebsiteNameEn() + "===" + configBean.getXmlPath() + "数据库连接失败！！！\n" + "错误信息为：" + e.getMessage());
                    }
                    //向数据库插入栏目信息
                    try {
                        JdbcOperate.insertData(websiteDbMap, siteBean.getWebsiteInfo(), connection);
                    } catch (Exception e) {
                        logger.error("栏目为：" + configBean.getXmlPath() + "的配置文件出错！！");
                    }
                    List<String> scheduleTimes = site.getConfigBean().getGeneralConfig().getScheduleTimes();
                    for (String scheduleTime : scheduleTimes
                    ) {
                        boolean isAdd2JobQueue = TimeUtils.isAdd2JobQueue(scheduleTime, site);
                        if (isAdd2JobQueue) {
                            TaskManager.addJobTask(site);
                            logger.info("job queue add task: " + siteBean.getWebsiteNameEn() + "(" + siteBean.getWebsiteColumnNameEn() + ")");
                        }
                    }
                } else {
                    logger.warn("the website :" + siteBean.getWebsiteNameEn() + "未设置采集");
                }
            });
            sourceConfigList.clear();
            try {
                sleep(1000 * 60 * 20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
