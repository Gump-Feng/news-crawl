package com.ertu.news.processor;

import com.ertu.news.analysis.HtmlAnalysis;
import com.ertu.news.download.DownLoader;
import com.ertu.news.model.bean.ConfigBean;
import com.ertu.news.model.bean.Site;
import com.ertu.news.model.bean.SiteBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author hxf
 * @date 2019/4/26 16:30
 */
public class PageProcessor implements SpiderProcessor {
    private int threadCount;
    private Logger logger = LoggerFactory.getLogger(PageProcessor.class);

    public PageProcessor(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    public void spider() {
        for (int i = 0; i < threadCount; i++) {
            new PageProcessorThread().start();
        }
    }

    class PageProcessorThread extends Thread {
        @Override
        public void run() {
            while (ScheduledTask.isNormal) {
                Site site = TaskManager.getListPageTask();
                if (site != null) {
                    MDC.put("site_id", 5 + "");
                    ConfigBean configBean = site.getConfigBean();
                    SiteBean siteBean = configBean.getSiteBean();
                    logger.info("列表页线程获得栏目：" + siteBean.getWebsiteNameEn() + "--" + siteBean.getWebsiteColumnNameEn());
                    int siteId = siteBean.getSiteId();
                    MDC.put("site_id", siteId + "");
                    logger.info("开始解析网站：" + siteBean.getWebsiteNameEn() + "--" + configBean.getXmlPath());
                    Set<String> duplicatedUrls = site.getDuplicatedUrls();
                    BlockingQueue<String> pageUrls = site.getListUrlQueue();
                    if (pageUrls.size() > 0) {
                        for (int i = 0; i < pageUrls.size(); i++) {
                            String crawlUrl = pageUrls.poll();
                            if (duplicatedUrls.contains(crawlUrl)) {
                                continue;
                            }
                            logger.info("深度抓取链接-" + siteBean.getWebsiteNameEn() + "：" + crawlUrl);
                            byte[] download = DownLoader.download(crawlUrl);
                            if (download != null) {
                                duplicatedUrls.add(crawlUrl);
                                HtmlAnalysis.classifyUrl(download, site);
                            }
                        }
                        logger.info("栏目：" + siteBean.getWebsiteNameEn() + "列表页请求完毕");
                    } else {
                        logger.info("栏目：" + siteBean.getWebsiteNameEn() + "列表页为空");
                    }
                    //列表页请求完毕，将isFinish修改为true
                    site.setFinished(true);
                } else {
                    try {
                        sleep(1000 * 10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
