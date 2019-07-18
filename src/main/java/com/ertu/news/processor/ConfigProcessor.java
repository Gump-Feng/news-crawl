package com.ertu.news.processor;

import com.ertu.news.analysis.*;
import com.ertu.news.download.FileDownLoader;
import com.ertu.news.download.utils.proxy.LocalProxy;
import com.ertu.news.io.sql.JdbcOperate;
import com.ertu.news.model.bean.ConfigBean;
import com.ertu.news.model.bean.Site;
import com.ertu.news.model.bean.SiteBean;
import com.ertu.news.utils.MailUtils;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author hxf
 * @date 2019/4/26 15:15
 */
public class ConfigProcessor implements SpiderProcessor {
    private Logger logger = Logger.getLogger(ConfigProcessor.class);
    private int threadCount;

    private boolean isNormal = true;

    public ConfigProcessor(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    public void spider() {
        for (int i = 0; i < threadCount; i++) {
            new ConfigProcessorThread().start();
        }
    }

    class ConfigProcessorThread extends Thread {
        private Connection connection = null;

        @Override
        public void run() {
            while (isNormal) {
                Site site = TaskManager.getJobTask();
                if (site != null
                ) {
                    ConfigBean configBean = site.getConfigBean();
                    SiteBean siteBean = configBean.getSiteBean();
                    int siteId = siteBean.getSiteId();
                    MDC.put("site_id", siteId);
                    Map<String, Object> websiteDbMap = siteBean.getDbTableConfig().get(XmlAnalysis.WEBSITE_TABLE_TAG);
                    if (connection == null) {
                        connection = JdbcOperate.getSqlConn(websiteDbMap);
                    }else {
                        try {
                            if (connection.isClosed()){
                                connection.close();
                                connection = JdbcOperate.getSqlConn(websiteDbMap);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    Map<String, Object> websiteMap = JdbcOperate.selectWebsiteBySiteId(websiteDbMap, connection, siteId + "");
                    if (websiteMap != null && websiteMap.size() > 0) {
                        //解析xml文档获得网站的入口和内部的连接规则
                        List<String> columnUrls = siteBean.getSiteUrls();
                        String websiteNameEn = siteBean.getWebsiteNameEn();
                        //获取可用代理
                        HttpHost host = LocalProxy.getHttpHost();
                        //获取图片的链接前缀
                        System.out.println(columnUrls + "=======================");

                        logger.info("start spider the website : " + "(" + websiteNameEn + siteBean.getWebsiteColumnNameEn() + ")"
                                + "\n" + "the seed size of the " + websiteNameEn + " ：" + columnUrls.size());

                        for (String siteUrl : columnUrls
                        ) {
                            logger.info("request the seed of " + siteBean.getWebsiteNameEn() + "：" + siteUrl);
                            try {
                                Map<String, Object> resultMap = FileDownLoader.download(siteUrl, host);
                                if (resultMap == null) {
                                    logger.error(websiteNameEn + "的入口出错：" + siteUrl + "\n 本次抓取将不被执行");
//                                    String errorInfo = TimeUtils.dateToString(new Date(), "yyyy-MM-dd") + "\n请求出错栏目信息" + "\n出错栏目id：" + siteId + "\n出错网站名称：" + websiteNameEn
//                                            + "\n出错栏目名称：" + siteBean.getWebsiteColumnNameEn() + "\n出错栏目配置文件路径：" + configBean.getXmlPath() + "\n出错栏目入口连接：" + siteBean.getSiteUrls() + "\n\n";
                                    try {
                                        MailUtils.sendErrorMail2Monitor(configBean);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    continue;
                                }
                                byte[] resultArray = (byte[]) resultMap.get("content");
                                String type = resultMap.get("type").toString();
                                siteBean.getSeedPage().put(siteUrl, resultMap);
                                //rss解析
                                if (type.contains("rss") || type.contains("xml") || type.contains("x-php")) {
                                    Analysis analysis = new RssAnalysis();
                                    analysis.analysis(site, siteUrl);
                                }
                                //json解析
                                if (type.contains("json")) {
                                    Analysis analysis = new JsonAnalysis();
                                    analysis.analysis(site, siteUrl);
                                }
                                if (resultArray == null) {
                                    logger.error(websiteNameEn + "在" + LocalDate.now() + "下载信息失败");
                                } else {
                                    logger.info("start analysis the seedUrl of the website");
                                    HtmlAnalysis.classifyUrl(resultArray, site);
                                    int detailQueueSize = site.getDetailUrlQueue().size();
                                    int listPageQueueSize = site.getListUrlQueue().size();
                                    if (detailQueueSize == 0) {
                                        logger.error("\n" + siteBean.getWebsiteNameEn() + "未匹配到详情页链接，请检查！！，列表页链接为：" + listPageQueueSize);
                                    } else {
                                        logger.info("\n" + siteBean.getWebsiteNameEn() + "已经添加到解析队列中，详情页共：" + detailQueueSize + "；列表页共：" + listPageQueueSize);
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("当前访问链接为：" + siteUrl + "\n" + Arrays.toString(e.getStackTrace()));
                                e.printStackTrace();
                            }
                        }
                        MDC.put("site_id", 4);
                        TaskManager.addDetailPageTask(site);
                        logger.info("详情页队列已添加栏目：" + siteBean.getWebsiteNameEn() + "(" + siteBean.getWebsiteColumnNameEn() + ")");
                        TaskManager.addListPageTask(site);
                        logger.info("列表页队列已添加栏目：" + siteBean.getWebsiteNameEn() + "(" + siteBean.getWebsiteColumnNameEn() + ")");
                    }
                }
                try {
                    sleep(1000 * 5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
    }
}
