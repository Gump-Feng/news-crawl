package com.ertu.news.analysis;

import com.ertu.news.model.bean.*;
import com.ertu.news.utils.StringUtils;
import com.ertu.news.utils.TimeUtils;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * @author hxf
 * @date 2019/3/18 16:23
 * <p>
 * 测试链接：https://www.belfercenter.org/rss/publication/988/feed
 * https://www.pppl.gov/news/archive/feed
 */
public class RssAnalysis implements Analysis {
    private static Logger logger = LoggerFactory.getLogger(RssAnalysis.class);


    public static void main(String[] args) {
    }

    /**
     * 解析xml中的rss信息并存到configBean中
     *
     * @param site    site
     * @param siteUrl siteUrl
     */
    @Override
    public void analysis(Site site, String siteUrl) {
        BlockingQueue<PageBean> dbUrlQueue = site.getDetailUrlQueue();
        SiteBean siteBean = site.getConfigBean().getSiteBean();
        //详情页的链接为key，配置的入库信息为value
        Document xmlDocument = XmlAnalysis.transferDocumentByFileDir(site.getConfigBean().getXmlPath());
        Element rssFields = xmlDocument.getRootElement().element("sites").element("site").element("writeConfig").element("db").element("rss-fields");
        //存储xml中需要rss解析的字段
        List<String> infoEleList = new ArrayList<>();
        if (null != rssFields) {
            logger.info("extracting the RSS of the " + siteBean.getWebsiteNameEn());
            List<?> siteColumns = rssFields.elements("field");
            for (Object siteEleObj : siteColumns) {
                Element siteEle = (Element) siteEleObj;
                String siteEleName = siteEle.attribute("name").getValue();
                infoEleList.add(siteEleName);
            }
        }
        byte[] rssBytes = (byte[]) siteBean.getSeedPage().get(siteUrl).get("content");
        SyndFeedInput rssInput = new SyndFeedInput();
        String originalCharset = "utf-8";
        GeneralConfig generalConfig = site.getConfigBean().getGeneralConfig();
        String charset = generalConfig.getCharset();
        if (charset != null) {
            originalCharset = charset;
        }
        try {
            Reader reader = new InputStreamReader(new ByteArrayInputStream(rssBytes), originalCharset);
            SyndFeed feed = rssInput.build(reader);
            feed.setEncoding(originalCharset);
            List<SyndEntry> syndFeedEntries = feed.getEntries();

            String date = "";
            Date publishedDate = null;
            for (SyndEntry syndEntry : syndFeedEntries
            ) {
                //创建放详情页信息的map
                Map<String, String> infoMap = new HashMap<>(16);
                for (String infoEle : infoEleList
                ) {
                    switch (infoEle) {
                        case "title":
                            infoMap.put("title", syndEntry.getTitle());
                            break;
                        case "author":
                            infoMap.put("author", syndEntry.getAuthor());
                            break;
                        case "publish_date":
                            Date rssPublishedDate = syndEntry.getPublishedDate();
                            if (rssPublishedDate != null) {
                                publishedDate = rssPublishedDate;
                                date = TimeUtils.dateToString(publishedDate, "yyyy-MM-dd");
                            }
                            infoMap.put("publish_date", date);
                            infoMap.put("publish_date_str", publishedDate.toString());
                            break;
                        case "news_abstract":
                            String description = "";
                            SyndContent descried = syndEntry.getDescription();
                            if (descried != null) {
                                infoMap.put("news_abstract", descried.getValue());
                            } else {
                                infoMap.put("news_abstract", description);
                            }
                            break;
                        default:
                    }
                }
                String infoLink = syndEntry.getLink().trim();
                if (!infoLink.startsWith("http")) {
                    infoLink = siteBean.getDetailPreUrl() + infoLink;
                }
                Rule rule = site.getConfigBean().getSiteBean().getRule();
                List<String> dbUrlRegList = rule.getDbUrlRegList();
                for (String dbUrlReg : dbUrlRegList
                ) {
                    dbUrlReg = dbUrlReg.split("&&")[1];
                    String matchStrByReg = StringUtils.matchStrByReg(infoLink, dbUrlReg);
                    if (!"".equals(matchStrByReg)) {
                        PageBean pageBean = new PageBean();
                        pageBean.setUrl(infoLink);
                        pageBean.setInfoMap(infoMap);
                        dbUrlQueue.put(pageBean);
                    }
                }
            }
        } catch (FeedException | InterruptedException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void analysis(Site site, byte[] body, HashMap<String, String> infoMap) {

    }

    @SuppressWarnings("rawtypes")
    public static void integrateInfo(HashMap<String, String> infoMap, Map<String, String> pageBeanInfoMap) {
        for (Map.Entry pageInfoEntry : pageBeanInfoMap.entrySet()
        ) {
            String pageKey = pageInfoEntry.getKey().toString();
            String pageValue = pageInfoEntry.getValue().toString();
            if (infoMap.containsKey(pageKey)) {
                String value = infoMap.get(pageKey);
                if (value == null || "".equals(value)) {
                    infoMap.put(pageKey, pageValue);
                }
            } else {
                infoMap.put(pageKey, pageValue);
            }
            String publishDate = infoMap.get("publish_date");
            if (publishDate != null && !publishDate.trim().isEmpty()) {
                infoMap.put("publish_date_str", publishDate);
            }
            String newsAbstract = infoMap.get("news_abstract");
            if (newsAbstract != null && !newsAbstract.trim().isEmpty() && newsAbstract.length() > 450) {
                newsAbstract = newsAbstract.substring(0, 450);
                infoMap.put("news_abstract", newsAbstract);
            }
        }
    }
}
