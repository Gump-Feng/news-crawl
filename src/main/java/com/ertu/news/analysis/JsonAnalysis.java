package com.ertu.news.analysis;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ertu.news.model.bean.PageBean;
import com.ertu.news.model.bean.Site;
import com.ertu.news.model.bean.SiteBean;
import com.ertu.news.utils.StringUtils;
import com.ertu.news.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * @author hxf
 * @date 2019/6/24 18:35
 */
public class JsonAnalysis implements Analysis {
    private static Logger logger = LoggerFactory.getLogger(JsonAnalysis.class);

    @Override
    public void analysis(Site site, String siteUrl) {
        BlockingQueue<PageBean> dbUrlQueue = site.getDetailUrlQueue();
        SiteBean siteBean = site.getConfigBean().getSiteBean();
        //详情页的链接为key，配置的入库信息为value
        Document xmlDocument = XmlAnalysis.transferDocumentByFileDir(site.getConfigBean().getXmlPath());
        Element crawlJsonExpEle = xmlDocument.getRootElement().element("sites").element("site").element("crawlJsonExps");
        byte[] rssBytes = (byte[]) siteBean.getSeedPage().get(siteUrl).get("content");
        MDC.put("site_id", siteBean.getSiteId()+"");
        logger.info("开始解析json数据：" + siteBean.getWebsiteColumnNameEn() + "/" + site.getConfigBean().getXmlPath());
        JSONObject jsonObject = (JSONObject) JSONObject.parse(rssBytes);
        if (crawlJsonExpEle != null) {
            List<?> jsonExpElems = crawlJsonExpEle.elements("jsonExp");
            if (jsonExpElems != null) {
                for (Object jsonEleObj : jsonExpElems) {
                    if (jsonEleObj instanceof Element) {
                        Element jsonEle = (Element) jsonEleObj;
                        String write = jsonEle.attribute("write").getValue();
                        String extract = jsonEle.attribute("extract").getValue();
                        String preUrl = jsonEle.attribute("preUrl").getValue();
                        if ("true".equals(write) && "false".equals(extract)) {
                            try {
                                Element jsonArray = jsonEle.element("jsonArray");
                                if (jsonArray != null) {
                                    String jsonArrayText = jsonArray.getText();
                                    //  |的作用：存在多层jsonArray的情况进行分割
                                    if (!jsonArrayText.contains("|")) {
                                        JSONArray dbJsonArray;
                                        dbJsonArray = jsonObject.getJSONArray(jsonArrayText);
                                        if (dbJsonArray != null) {
                                            for (Object jsonObj : dbJsonArray
                                            ) {
                                                if (jsonObj instanceof JSONObject) {
                                                    JSONObject json = (JSONObject) jsonObj;
                                                    getDbJsonByXmlEle(json, jsonEle, dbUrlQueue, preUrl);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.error("json页面解析出错，出错信息为：" + Arrays.toString(e.getStackTrace()));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void analysis(Site site, byte[] body, HashMap<String, String> infoMap) {

    }

    private static void getDbJsonByXmlEle(JSONObject json, Element jsonEle, BlockingQueue<PageBean> dbUrlQueue, String preUrl) {
        List<?> fieldList = jsonEle.elements("field");
        PageBean pageBean = new PageBean();
        Map<String, String> infoMap = new HashMap<>(16);
        for (Object fieldObj : fieldList) {
            if (fieldObj instanceof Element) {
                Element field = (Element) fieldObj;
                String fieldName = field.attribute("name").getValue();
                String jsonKey = field.element("jsonKey").getText();
                switch (fieldName) {
                    case "url_src":
                        String url = json.getString(jsonKey);
                        if (!url.startsWith("http")) {
                            url = preUrl + url;
                        }
                        pageBean.setUrl(url);
                        break;
                    case "title":
                        String title = json.getString(jsonKey);
                        infoMap.put("title", title);
                        break;
                    case "publish_date":
                        String publishDateStr = json.getString(jsonKey);
                        Element regExp = jsonEle.element("regExp");
                        publishDateStr = StringUtils.matchStrByReg(publishDateStr, regExp);
                        Element dataTransform = jsonEle.element("dataTransform");
                        Date publishDate = null;
                        if (dataTransform != null) {
                            String beanId = dataTransform.attribute("beanId").getValue();
                            publishDate = TimeUtils.formatTime(publishDateStr, "", beanId);
                        }
                        infoMap.put("publish_date_str", publishDateStr);
                        infoMap.put("publish_date", TimeUtils.dateToString(publishDate, "yyyy-MM-dd"));
                        break;
                    case "news_abstract":
                        String newsAbstract = json.getString(jsonKey);
                        if (newsAbstract.length() > 400) {
                            newsAbstract = newsAbstract.substring(0, 400);
                        }
                        infoMap.put("news_abstract", newsAbstract);
                        break;
                    case "author":
                        String author = json.getString(jsonKey);
                        infoMap.put("author", author);
                        break;
                    default:
                }
            }
        }
        pageBean.setInfoMap(infoMap);
        dbUrlQueue.add(pageBean);
    }
}
