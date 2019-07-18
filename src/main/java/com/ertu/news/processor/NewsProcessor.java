package com.ertu.news.processor;

import com.ertu.news.analysis.Analysis;
import com.ertu.news.analysis.HtmlAnalysis;
import com.ertu.news.analysis.RssAnalysis;
import com.ertu.news.analysis.XmlAnalysis;
import com.ertu.news.download.FileDownLoader;
import com.ertu.news.download.utils.proxy.LocalProxy;
import com.ertu.news.io.sql.JdbcOperate;
import com.ertu.news.model.bean.ConfigBean;
import com.ertu.news.model.bean.PageBean;
import com.ertu.news.model.bean.Site;
import com.ertu.news.model.bean.SiteBean;
import com.ertu.news.utils.PdfUtils;
import com.ertu.news.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * @author hxf
 * @date 2019/4/26 16:44
 */
public class NewsProcessor implements SpiderProcessor {
    private int threadCount;
    private Logger logger = Logger.getLogger(PageProcessor.class);

    public NewsProcessor(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    public void spider() {
        for (int i = 0; i < threadCount; i++) {
            NewsProcessorThread newsProcessorThread = new NewsProcessorThread();
            newsProcessorThread.setName("news-spider-thread-"+i);
            newsProcessorThread.start();
        }
    }

    class NewsProcessorThread extends Thread {
        private Connection sqlConn = null;
        private Connection staticSourceConn = null;

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            String threadName = Thread.currentThread().getName();
            while (ScheduledTask.isNormal) {
                //记录一下出队列的网站顺序
                Site site = TaskManager.getDetailTask();
                //向监视线程汇报
                ProcessManager.checkIn(threadName, site, new Date());

                if (site != null) {
                    MDC.put("site_id", 6);
                    ConfigBean configBean = site.getConfigBean();
                    SiteBean siteBean = configBean.getSiteBean();
                    logger.info("详情页线程获得栏目：" + siteBean.getWebsiteNameEn() + "--" + siteBean.getWebsiteColumnNameEn());
                    int siteId = siteBean.getSiteId();
                    MDC.put("site_id", siteId);
                    // 获取代理
                    HttpHost host = LocalProxy.getHttpHost();
                    BlockingQueue<PageBean> infoUrls = site.getDetailUrlQueue();
                    logger.info("开始抓取网站：" + siteBean.getWebsiteNameEn() + "的详情页" + "，链接共：" + infoUrls.size());
                    // 链接数据库、建表

                    Map<String, Object> dataBaseColumnNameMap = siteBean.getDbTableConfig()
                            .get(XmlAnalysis.INFO_TABLE_TAG);
                    Map<String, Object> staticDbMap = siteBean.getDbTableConfig().get(XmlAnalysis.FILE_TABLE_TAG);
                    initDbConnectorAndTable(dataBaseColumnNameMap, staticDbMap);
                    //检查mysql的链接是否有效
                    updateSqlConnection(dataBaseColumnNameMap, staticDbMap);

                    // 获取html页面的正文内容入库信息
                    while (!site.isFinished() || !infoUrls.isEmpty()) {
                        PageBean pageBean = infoUrls.poll();
                        if (pageBean == null) {
                            continue;
                        }
                        String newsUrl = pageBean.getUrl();
                        Map<String, String> pageBeanInfoMap = pageBean.getInfoMap();
                        logger.info(siteBean.getWebsiteNameEn() + "--详情页链接：" + infoUrls.size());
                        String dataMd5 = JdbcOperate.stringToMd5Url(newsUrl);
                        String dataId = JdbcOperate.selectIdByMd5(dataBaseColumnNameMap, sqlConn, dataMd5);
                        if (dataId != null && Integer.parseInt(dataId) > 0) {
                            logger.info("该资讯已存在：" + newsUrl);
                            continue;
                        }
                        Map<String, Object> downloadMap = FileDownLoader.download(newsUrl, host);
                        if (downloadMap == null) {
                            continue;
                        }
                        byte[] download = (byte[]) downloadMap.get("content");
                        String type = downloadMap.get("type").toString();
                        if (type.contains("pdf") || PdfUtils.getPdfPage(download, newsUrl) != 0) {
                            // 内容为裸pdf
                            dealPdfDetail(pageBeanInfoMap, newsUrl, site, download);
                        } else {
                            if (download != null) {
                                //对html页面进行正确入库
                                dealHtmlDetail(pageBeanInfoMap, newsUrl, site, download);
                            }
                        }
                    }
                    logger.info("结束抓取网站：" + siteBean.getWebsiteNameEn() + "(" + siteBean.getWebsiteColumnNameEn() + ")" + "的详情页");
                } else {
                    try {
                        sleep(1000 * 60 * 3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        /**
         * 避免由于mysql链接时间过期而导致conn失效
         *
         * @param dataBaseColumnNameMap news_info的表信息
         * @param staticDbMap           news_static_file的表信息
         */
        private void updateSqlConnection(Map<String, Object> dataBaseColumnNameMap, Map<String, Object> staticDbMap) {
            try {
                if (sqlConn == null || sqlConn.isClosed()) {
                    if (sqlConn != null){
                        sqlConn.close();
                    }
                    sqlConn = JdbcOperate.getSqlConn(dataBaseColumnNameMap);
                }
                if (staticSourceConn == null || staticSourceConn.isClosed()) {
                    if (staticSourceConn != null){
                        staticSourceConn.close();
                    }
                    staticSourceConn = JdbcOperate.getSqlConn(staticDbMap);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        /**
         * 初始化数据库链接和表
         *
         * @param dataBaseColumnNameMap 数据表
         * @param staticDbMap           文件表
         */
        private void initDbConnectorAndTable(Map<String, Object> dataBaseColumnNameMap,
                                             Map<String, Object> staticDbMap) {
            sqlConn = JdbcOperate.getSqlConn(dataBaseColumnNameMap);
            staticSourceConn = JdbcOperate.getSqlConn(staticDbMap);
            JdbcOperate.createTable(dataBaseColumnNameMap, sqlConn);
            JdbcOperate.createTable(staticDbMap, staticSourceConn);
        }

        /**
         * insert fileMap to the mysql order by the fileUrl and db-table-map
         *
         * @param fileUrl     file link
         * @param site        website info
         * @param title       detail title
         * @param staticDbMap db-table-map
         */
        private void insertStaticFile2DB(String fileUrl, Site site, String title, Map<String, Object> staticDbMap) {
            try {
                Map<String, String> staticSourceMap = XmlAnalysis.getNewsFileInfoByUrl(site,
                        fileUrl, title);
                if (staticSourceMap == null || staticSourceMap.size() == 0) {
                    return;
                }
                logger.info("当前入库的文件链接为：" + fileUrl);
                checkNewsFileUrlAnd2DB(staticDbMap, staticSourceConn, fileUrl, staticSourceMap, site.getConfigBean().getSiteBean().getNewsId());
            } catch (Exception e) {
                logger.warn("链接为：" + fileUrl + "\n的文件下载失败，原因为：" + e.getMessage());
            }
        }

        /**
         * 对裸pdf的详情页进行判断和入库
         *
         * @param pageBeanInfoMap 字段信息
         * @param newsUrl         详情页链接
         * @param site            网站实体类
         * @param downloadBytes   详情页内容
         */
        private void dealPdfDetail(Map<String, String> pageBeanInfoMap, String newsUrl, Site site, byte[] downloadBytes) {
            SiteBean siteBean = site.getConfigBean().getSiteBean();
            Map<String, Object> dataBaseColumnNameMap = siteBean.getDbTableConfig().get(XmlAnalysis.INFO_TABLE_TAG);
            Map<String, Object> staticDbMap = siteBean.getDbTableConfig().get(XmlAnalysis.FILE_TABLE_TAG);
            pageBeanInfoMap.put("url_src", newsUrl);
            pageBeanInfoMap.put("site_id", siteBean.getSiteId() + "");
            pageBeanInfoMap.put("site_name", siteBean.getWebsiteNameEn());
            pageBeanInfoMap.put("site_description", siteBean.getWebsiteNameCn());
            pageBeanInfoMap.put("category", siteBean.getWebsiteColumnNameEn());

            String title = pageBeanInfoMap.get("title");
            title = StringUtils.titleFilter(title);

            //下载logo图片
            String logoUrl = StringUtils.getUrlByHtmlTag(siteBean.getLogo());
            insertStaticFile2DB(logoUrl, site, title, staticDbMap);

            int newsId = JdbcOperate.insertData(dataBaseColumnNameMap, pageBeanInfoMap, sqlConn);
            if (newsId <= 0) {
                return;
            }
            siteBean.setNewsId(newsId);
            // 把pdf保存到本地
            String pdfPath = storePdf(downloadBytes, siteBean, title);
            pdfPath = pdfPath.substring(pdfPath.indexOf("/static"));
            //编写裸pdf的正文部分
            String content = "<br/><br/><a href=\"" + pdfPath + "\">" + title + "</a>";
            //更新正文
            String formatContent = HtmlAnalysis.formatContent(content, siteBean, newsUrl);
            JdbcOperate.updateContentAndTranslateSignById(formatContent, siteBean.getNewsId(), dataBaseColumnNameMap, sqlConn);
        }

        /**
         * 对正常html的详情页进行判断和入库
         *
         * @param pageBeanInfoMap 字段信息
         * @param newsUrl         详情页链接
         * @param site            网站实体类
         * @param downloadBytes   详情页内容
         */
        private void dealHtmlDetail(Map<String, String> pageBeanInfoMap, String newsUrl, Site site, byte[] downloadBytes) {
            ConfigBean configBean = site.getConfigBean();
            SiteBean siteBean = configBean.getSiteBean();
            int siteId = siteBean.getSiteId();
            String crawlType = siteBean.getCrawlType();
            HashMap<String, String> infoMap = new HashMap<>(16);
            Analysis htmlAnalysis = new HtmlAnalysis();
            htmlAnalysis.analysis(site, downloadBytes, infoMap);
            // 对infoMap的内容和rss的内容进行整合
            if (pageBeanInfoMap != null && pageBeanInfoMap.size() != 0) {
                RssAnalysis.integrateInfo(infoMap, pageBeanInfoMap);
            }
            // 对infoMap进行再判定
            infoMap.put("url_src", newsUrl);
            String title = infoMap.get("title");
            if (title == null || "".equals(title)) {
                logger.error("栏目为：" + siteId + "\n链接为：" + newsUrl + "对应的title为空");
                return;
            }
            logger.info("文章标题为：" + title);
            if (XmlAnalysis.NEWS.equals(crawlType)) {
                //资讯入库处理
                dealNews2Db(infoMap, newsUrl, site, downloadBytes, title);
            }
            if (XmlAnalysis.LITERATURE.equals(crawlType)) {
                // 文献的html存储
                dealLiterature2Db(infoMap, newsUrl, site, downloadBytes, title);
            }
        }

        private void dealLiterature2Db(Map<String, String> infoMap, String newsUrl, Site site, byte[] downloadBytes, String title) {
            ConfigBean configBean = site.getConfigBean();
            Map<String, Object> dataBaseColumnNameMap = configBean.getSiteBean().getDbTableConfig().get(XmlAnalysis.INFO_TABLE_TAG);
            String storeHtmlPath = storeHtml(downloadBytes, configBean, title);
            infoMap.put("html_path", storeHtmlPath);
            // 获取pdf的链接并下载入库
            String pdfUrl = infoMap.get("pdf_url");
            if (pdfUrl != null && !"".equals(pdfUrl)) {
                configBean.getSiteBean().setNewsId(1);
                if (pdfUrl.contains(";;")) {
                    String[] pdfUrls = pdfUrl.split(";;");
                    for (String url : pdfUrls) {
                        pdfDownload(dataBaseColumnNameMap, sqlConn, staticSourceConn, newsUrl,
                                site, infoMap, title, url);
                    }
                } else {
                    pdfDownload(dataBaseColumnNameMap, sqlConn, staticSourceConn, newsUrl, site,
                            infoMap, title, pdfUrl);
                }
            }
        }

        /**
         * 针对资讯数据入库的方法
         *
         * @param infoMap       入库数据
         * @param newsUrl       详情页链接
         * @param site          网站实体类
         * @param downloadBytes 页面数据
         * @param title         标题
         */
        private void dealNews2Db(Map<String, String> infoMap, String newsUrl, Site site, byte[] downloadBytes, String title) {
            ConfigBean configBean = site.getConfigBean();
            SiteBean siteBean = configBean.getSiteBean();
            String storeHtmlPath = storeHtml(downloadBytes, configBean, title);
            infoMap.put("html_path", storeHtmlPath);
            //对infoMap进行去除外层标签
            String content = infoMap.get("content");
            if (content != null && !"".equals(content)) {
                infoMap.put("content", HtmlAnalysis.deleteContentTag(content));
            } else {
                logger.error("栏目id为：" + siteBean.getSiteId() + "\n链接为：" + newsUrl + "\n对应的正文为空");
                return;
            }
            try {
                int id = JdbcOperate.insertData(siteBean.getDbTableConfig().get(XmlAnalysis.INFO_TABLE_TAG), infoMap, sqlConn);
                if (id < 0) {
                    logger.error("入库失败 " + id + "：" + newsUrl + "\n入库内容为：" + infoMap.toString());
                } else {
                    logger.info("入库成功 " + id + "：" + newsUrl);
                }
                siteBean.setNewsId(id);
                // 获取content部分的文件链接并入库
                // 从正文中获取需要入库的文件链接\
                List<String> fileUrlList = HtmlAnalysis.getSideLinks(configBean, content, newsUrl);
                // 获取logo链接
                String logoUrl = StringUtils.getUrlByHtmlTag(siteBean.getLogo());
                fileUrlList.add(logoUrl);
                // 根据插入详情页数据返回的数据库id完成静态文件的信息入库
                for (String fileUrl : fileUrlList) {
                    if ("".equals(fileUrl)) {
                        continue;
                    }
                    // 获取html页面正文中的文件链接入库信息
                    insertStaticFile2DB(fileUrl, site, title, siteBean.getDbTableConfig().get(XmlAnalysis.FILE_TABLE_TAG));
                }
                content = HtmlAnalysis.formatContent(content, siteBean, newsUrl);
                JdbcOperate.updateContentAndTranslateSignById(content, siteBean.getNewsId(), siteBean.getDbTableConfig().get(XmlAnalysis.INFO_TABLE_TAG), sqlConn);
            } catch (Exception e) {
                logger.error("链接为：" + newsUrl + "的详情页数据插入失败！！" + "\n"
                        + "出错信息为：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String storePdf(byte[] download, SiteBean siteBean, String title) {
        String pdfPath = XmlAnalysis.getPdfPathByCrawlType(siteBean, title);
        try {
            FileUtils.writeByteArrayToFile(new File(pdfPath), download);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfPath;
    }

    private String storeHtml(byte[] download, ConfigBean configBean, String title) {
        String pathByCrawlType = XmlAnalysis.getHtmlPathByCrawlType(configBean, configBean.getSiteBean().getCrawlType(),
                title);
        try {
            FileUtils.writeByteArrayToFile(new File(pathByCrawlType), download);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathByCrawlType;
    }

    private void pdfDownload(Map<String, Object> dataBaseColumnNameMap, Connection sqlConn, Connection staticSourceConn,
                             String newsUrl, Site site, Map<String, String> infoMap, String title, String pdfUrl) {
        Map<String, String> staticSourceMap = XmlAnalysis.getNewsFileInfoByUrl(site, pdfUrl, title);
        if (staticSourceMap != null && staticSourceMap.size() != 0) {
            int id = JdbcOperate.insertData(dataBaseColumnNameMap, infoMap, sqlConn);
            int newsId = site.getConfigBean().getSiteBean().getNewsId();
            if (id > 0) {
                newsId = id;
            }
            checkNewsFileUrlAnd2DB(site.getConfigBean().getSiteBean().getDbTableConfig().get(XmlAnalysis.FILE_TABLE_TAG),
                    staticSourceConn, newsUrl, staticSourceMap, newsId);
        }
    }

    private void checkNewsFileUrlAnd2DB(Map<String, Object> staticDbMap, Connection staticSourceConn, String fileUrl,
                                        Map<String, String> staticSourceMap, int id) {
        String fileId = JdbcOperate.stringToMd5Url(fileUrl);
        String staticFileId = JdbcOperate.selectIdByMd5(staticDbMap, staticSourceConn, fileId);
        if (staticFileId == null || Integer.parseInt(staticFileId) <= 0) {
            insertStaticFile(staticDbMap, staticSourceConn, fileUrl, id, staticSourceMap);
        }
    }

    private void insertStaticFile(Map<String, Object> staticDbMap, Connection staticSourceConn, String newsUrl, int id,
                                  Map<String, String> staticSourceMap) {
        staticSourceMap.put("news_id", id + "");
        int insertData = JdbcOperate.insertData(staticDbMap, staticSourceMap, staticSourceConn);
        if (insertData < 0) {
            logger.info("文件链接数据已存在：" + newsUrl);
        } else {
            logger.info("文件链接数据插入成功" + newsUrl);

        }
    }
}
