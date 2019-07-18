package com.ertu.news.analysis;


import com.ertu.news.io.sql.JdbcOperate;
import com.ertu.news.model.bean.*;
import com.ertu.news.utils.StringUtils;
import com.ertu.news.utils.TimeUtils;
import com.github.binarywang.java.emoji.EmojiConverter;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.dom4j.Element;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * @author dbc hxf
 * @date 09点52分
 * <p>
 * 对xml解析到的规则在相关页面中进行获取信息
 */
public class HtmlAnalysis implements Analysis {
    private static Logger logger = Logger.getLogger(HtmlAnalysis.class);

    public static void main(String[] args) {
        String preUrl = "";
        String newsUrl = "https://www.sandia.gov/news/publications/labnews/archive/15-21-08.html";
        String fileUrl = "//abm-website-assets.s3.amazonaws.com/rdmag.com/s3fs-public/styles/thumbnail/public/author_head_shots/kenny%20pic.jpg";
        System.out.println(formatFileUrl(fileUrl, newsUrl, preUrl));
        String fileUrl1 = "./kenny%20pic.jpg";
        System.out.println(formatFileUrl(fileUrl1, newsUrl, preUrl));
        String fileUrl2 = "../author_head_shots/kenny%20pic.jpg";
        System.out.println(formatFileUrl(fileUrl2, newsUrl, preUrl));
        String fileUrl3 = "../../public/author_head_shots/kenny%20pic.jpg";
        System.out.println(formatFileUrl(fileUrl3, newsUrl, preUrl));
        String fileUrl4 = "/rdmag.com/s3fs-public/styles/thumbnail/public/author_head_shots/kenny%20pic.jpg";
        System.out.println(formatFileUrl(fileUrl4, newsUrl, preUrl));
        String fileUrl5 = "_assets/images/15-21-08/pic1.jpg";
        System.out.println(formatFileUrl(fileUrl5, newsUrl, preUrl));
    }

    /**
     * 解析html的工具类
     *
     * @param site          网站栏目对应的实体类
     * @param downloadBytes 要解析的页面
     * @param infoMap       解析返回的信息
     */
    @SuppressWarnings("unchecked")
    @Override
    public void analysis(Site site, byte[] downloadBytes, HashMap<String, String> infoMap) {
        ConfigBean configBean = site.getConfigBean();
        Html html = null;
        try {
            html = new Html(new String(downloadBytes, site.getConfigBean().getGeneralConfig().getCharset()));
        } catch (UnsupportedEncodingException e) {
            logger.error("栏目：" + configBean.getXmlPath() + "的编码方式出错！！");
            e.printStackTrace();
        }
        Map<String, Map<String, Object>> mesMap = configBean.getSiteBean().getHtmlConfig();
        String crawlType = configBean.getSiteBean().getCrawlType();
        HashMap<String, Object> pageField;
        HashMap<String, Object> remap;
        /*对map进行遍历*/
        if (XmlAnalysis.NEWS.equals(crawlType)) {
            for (Map.Entry<String, Map<String, Object>> articleContentEntry : mesMap.entrySet()
            ) {
                String keyMax = articleContentEntry.getKey();
                Map<String, Object> valuemax = articleContentEntry.getValue();
                if (!keyMax.contains("content")) {
                    pageField = (HashMap<String, Object>) valuemax.get("page-field");
                    reMessage(keyMax, pageField, html, valuemax, infoMap);
                } else {
                    for (int i = 1; i < 5; i++) {
                        remap = (HashMap<String, Object>) valuemax.get("xpath" + i);
                        if (remap != null) {
                            reContent(keyMax, remap, html, infoMap);
                        }
                    }
                }
            }
        }
        if (XmlAnalysis.LITERATURE.equals(crawlType)) {
            for (Map.Entry<String, Map<String, Object>> articleContentEntry : mesMap.entrySet()
            ) {
                String keyMax = articleContentEntry.getKey();
                System.out.println(keyMax);
                Map<String, Object> valuemax = articleContentEntry.getValue();
                pageField = (HashMap<String, Object>) valuemax.get("page-field");
                reMessage(keyMax, pageField, html, valuemax, infoMap);
            }
        }

        logger.info("获取到入库内容：" + infoMap.keySet() + "当前的");
    }


    @SuppressWarnings("unchecked")
    private static void reMessage(String keyMax, HashMap<String, Object> pageField, Html html, Map<String, Object> valuemax, HashMap<String, String> infoMap) {
        String leadingHtml = pageField.get(keyMax).toString();
        if (!"".equals(leadingHtml)) {
            infoMap.put(keyMax, leadingHtml);
            return;
        }
        Object xpathListObj = pageField.get("xpathExp");

        if (xpathListObj instanceof List) {
            List<String> xpathList = (List<String>) xpathListObj;
            for (int i = 0; i < xpathList.size(); i++) {
                Selectable selectable = html.xpath(xpathList.get(i));
                String prefix = "";
                String suffix = "";
                String beanId = "";
                String clear = "";
                Element regExp = null;
                Object prefix1 = pageField.get("prefix");
                if (prefix1 != null) {
                    List<String> prefixList = (List<String>) prefix1;
                    prefix = prefixList.get(i);
                }
                Object suffix1 = pageField.get("suffix");
                if (suffix1 != null) {
                    List<String> suffixList = (List<String>) suffix1;
                    suffix = suffixList.get(i);
                }
                Object beanId1 = pageField.get("beanId");
                if (beanId1 != null) {
                    List<String> suffixList = (List<String>) beanId1;
                    beanId = suffixList.get(i);
                }

                Object regExp1 = pageField.get("regExp");
                if (regExp1 != null) {
                    List<Element> suffixList = (List<Element>) regExp1;
                    regExp = suffixList.get(i);
                }

                Object clear1 = pageField.get("clear");
                List<String> clearList;
                if (clear1 != null) {
                    clearList = (List<String>) clear1;
                    clear = clearList.get(i);
                }

                try {
                    List<String> list;
                    list = selectable.all();
                    String xpathValue = org.apache.commons.lang3.StringUtils.join(list.toArray(), ";;").trim();
                    xpathValue = xpathValue.replaceAll("(^\\s*)|(\\s*$)", "");
                    String[] clearToList = clear.split(";;");
                    for (String clearStr : clearToList) {
                        xpathValue = xpathValue.replaceAll(clearStr, "");
                    }
                    if ("publish_date".equals(keyMax)) {
                        if (!xpathValue.trim().isEmpty()) {
                            String publishDateStr = StringUtils.matchStrByReg(xpathValue, regExp);
                            if (!publishDateStr.trim().isEmpty()) {
                                infoMap.put("publish_date_str", publishDateStr);
                            }
                        } else {
                            infoMap.put("publish_date_str", TimeUtils.dateToString(new Date(), "yyyy-MM-dd"));
                        }
                    }
                    if ("".equals(prefix) && "".equals(suffix)) {
                        xpathValue = getDateString(keyMax, beanId, regExp, xpathValue).replaceAll("(^\\s*)|(\\s*$)", "");
                        regExpString(keyMax, regExp, xpathValue, infoMap);
                    } else if (!"".equals(prefix) && "".equals(suffix)) {
                        if (xpathValue.contains(prefix)) {
                            xpathValue = xpathValue.substring(xpathValue.indexOf(prefix) + prefix.length());
                            xpathValue = getDateString(keyMax, beanId, regExp, xpathValue);
                            regExpString(keyMax, regExp, xpathValue, infoMap);
                        }
                    } else if ("".equals(prefix)) {
                        if (xpathValue.contains(suffix)) {
                            xpathValue = xpathValue.substring(0, xpathValue.lastIndexOf(suffix));
                            xpathValue = getDateString(keyMax, beanId, regExp, xpathValue);
                            regExpString(keyMax, regExp, xpathValue, infoMap);
                        }
                    } else {
                        if (xpathValue.contains(suffix) && xpathValue.contains(suffix)) {
                            xpathValue = xpathValue.substring(xpathValue.indexOf(prefix) + prefix.length(), xpathValue.lastIndexOf(suffix));
                            xpathValue = getDateString(keyMax, beanId, regExp, xpathValue);
                            regExpString(keyMax, regExp, xpathValue, infoMap);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (valuemax.get("emptyAllowed") != null && "true".equals(valuemax.get("emptyAllowed"))) {
                        if (infoMap.get(keyMax) == null || infoMap.get(keyMax).length() == 0) {
                            infoMap.put(keyMax, "");
                        }
                    }
                }
            }
        }
        if (infoMap.containsKey("pdf_url")) {
            List<String> pdfList = new ArrayList<>();
            List<String> pdfUrl = Arrays.asList(infoMap.get("pdf_url").split(";;"));
            addPreUrl(valuemax, pdfList, pdfUrl, infoMap);
        }
        if (infoMap.containsKey("pic_url")) {
            List<String> picList = new ArrayList<>();
            List<String> picUrl = Arrays.asList(infoMap.get("pic_url").split(";;"));
            addPreUrl(valuemax, picList, picUrl, infoMap);
        }

    }

    private static void addPreUrl(Map<String, Object> valuemax, List<String> picList, List<String> picUrl, HashMap<String, String> infoMap) {
        for (String pdf : picUrl) {
            String stringPicUrl = valuemax.get("preUrl") + pdf;
            picList.add(stringPicUrl);
        }
        String picValue = org.apache.commons.lang3.StringUtils.join(picList.toArray(), ";;");
        infoMap.put("pdf_url", picValue);
    }


    private static void regExpString(String keyMax, Element regExp, String xpathValue, HashMap<String, String> infoMap) {
        if (regExp == null) {
            if (xpathValue.length() > 500) {
                xpathValue = xpathValue.substring(0, 500).replaceAll("(^\\s*)|(\\s*$)", "");
            }
            if (infoMap.containsKey(keyMax)) {
                String value = infoMap.get(keyMax);
                if (value.trim().isEmpty()) {
                    infoMap.put(keyMax, xpathValue.trim());
                }
            } else {
                infoMap.put(keyMax, xpathValue.trim());
            }
        } else {
            if (!"publish_date".equals(keyMax)) {
                String xValue = StringUtils.matchStrByReg(xpathValue, regExp);
                if ("news_abstract".equals(keyMax) && xValue.length() > 500) {
                    xValue = xValue.substring(0, 400).replaceAll("(^\\s*)|(\\s*$)", "");
                }
                infoMap.put(keyMax, xValue.trim());
            } else {
                if (!xpathValue.trim().isEmpty()) {
                    infoMap.put(keyMax, xpathValue.trim());
                }
            }
        }
    }

    private static String getDateString(String keyMax, String beanId, Element regExp, String xpathValue) {
        if ("publish_date".equals(keyMax)) {
            String regExpStr = "";
            if (regExp != null) {
                regExpStr = regExp.getText();
            }
            Date publishDate = TimeUtils.formatTime(xpathValue, regExpStr, beanId);
            xpathValue = TimeUtils.dateToString(publishDate, "yyyy-MM-dd");
        }
        return xpathValue;
    }

    @SuppressWarnings("unchecked")
    private static void reContent(String keyMax, HashMap<String, Object> remap, Html html, HashMap<String, String> infoMap) {
        Object reListObj = remap.get("removedNodes");
        Object removeAttrNames = remap.get("removeAttrNames");
        Object changeAttributes = remap.get("changeAttributes");
        String contentXpath = remap.get("xpathExp").toString();
        if (!contentXpath.trim().isEmpty()) {
            Selectable selectableRe = html.xpath(contentXpath.trim());
            String revalue = "";
            try {
                List<String> list;
                list = selectableRe.all();
                revalue = org.apache.commons.lang3.StringUtils.join(list.toArray(), ";;");
            } catch (Exception e) {
                if (remap.get("emptyAllowed") != null && "true".equals(remap.get("emptyAllowed"))) {
                    infoMap.put(keyMax, "");
                }
                e.printStackTrace();
            }
            if (revalue.length() > 0) {
                Document contentDocument = Jsoup.parse(revalue);
                if (reListObj != null) {
                    if (reListObj instanceof List) {
                        List<String> reList = (List<String>) reListObj;
                        for (String s : reList) {
                            try {
                                Elements removeNode = contentDocument.select(s.trim());
                                if (removeNode != null) {
                                    removeNode.remove();
                                }
                            } catch (Exception e) {
                                logger.error(s + "\n" +
                                        "出错信息为：" + e.getMessage());
                            }
                        }
                    }
                }

                //去除配置的属性
                if (removeAttrNames != null) {
                    List<String> removeAttrs = (List<String>) removeAttrNames;
                    for (String attrName : removeAttrs) {
                        try {
                            Elements elementsByAttribute = contentDocument.getElementsByAttribute(attrName);
                            for (Object removeEleAttrObj : elementsByAttribute
                            ) {
                                if (removeEleAttrObj instanceof org.jsoup.nodes.Element) {
                                    org.jsoup.nodes.Element removeEleAttr = (org.jsoup.nodes.Element) removeEleAttrObj;
                                    removeEleAttr.removeAttr(attrName);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                //修改配置的属性
                if (changeAttributes != null) {
                    List<Map<String, String>> changeAttrs = (List<Map<String, String>>) changeAttributes;
                    for (Map<String, String> attrNameMap : changeAttrs) {
                        try {
                            String oldNameAttr = attrNameMap.get("old_name");
                            String newNameAttr = attrNameMap.get("new_name");
                            Elements elementsByAttribute = contentDocument.getElementsByAttribute(oldNameAttr);
                            for (Object removeEleAttrObj : elementsByAttribute
                            ) {
                                if (removeEleAttrObj instanceof org.jsoup.nodes.Element) {
                                    org.jsoup.nodes.Element changeEleAttr = (org.jsoup.nodes.Element) removeEleAttrObj;
                                    String value = changeEleAttr.attr(oldNameAttr);
                                    changeEleAttr.removeAttr(oldNameAttr);
                                    changeEleAttr.attr(newNameAttr, value);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                revalue = contentDocument.toString();
                //去除正文中可能出现的表情符号等
                EmojiConverter emojiConverter = EmojiConverter.getInstance();
                revalue = emojiConverter.toAlias(revalue);
                if (!revalue.trim().isEmpty() && revalue.length() > 100) {
                    infoMap.put(keyMax, revalue);
                }
            }
        }

    }

    public static List<String> getSideLinks(ConfigBean configBean, String content, String newsUrl) {
        List<String> fileLinksList = new ArrayList<>(1000);
        String filePreUrl = configBean.getSiteBean().getFilePreUrl();
        try {
            Document document = Jsoup.parse(content);
            Elements aElements = document.getElementsByTag("a");
            if (aElements != null && !aElements.isEmpty()) {
                checkUrlAndAdd2FileUrlList(aElements, filePreUrl, fileLinksList, newsUrl, "href");
            }
            Elements imgElements = document.getElementsByTag("img");
            if (imgElements != null && !imgElements.isEmpty()) {
                checkUrlAndAdd2FileUrlList(imgElements, filePreUrl, fileLinksList, newsUrl, "src", "data-src");
            }
        } catch (Exception e) {
            logger.error(configBean.getSiteBean().getWebsiteNameEn() + "出错，检查配置文件：" + configBean.getXmlPath() +
                    "出错信息：" + e.getMessage());
            e.printStackTrace();
        }
        return fileLinksList;
    }

    private static void checkUrlAndAdd2FileUrlList(Elements fileLinkPart, String preStaticUrl, List<String> fileLinksList, String newsUrl, String... attr
    ) {
        for (org.jsoup.nodes.Element ele : fileLinkPart
        ) {
            for (String attrStr : attr
            ) {
                String attrValue = ele.attr(attrStr);
                if (attrValue != null && !attrValue.trim().isEmpty()) {
                    String formatFileUrl = formatFileUrl(attrValue.trim(), newsUrl, preStaticUrl);
                    if (!formatFileUrl.trim().isEmpty()) {
                        fileLinksList.add(formatFileUrl);
                    }
                }
            }
        }
    }

    /**
     * complete the url of image order by the details page link
     *
     * @param attrValue    Image link in source code
     * @param newsUrl      details page link
     * @param preStaticUrl the prefix of the image link
     * @return the formatted image link
     */
    private static String formatFileUrl(String attrValue, String newsUrl, String preStaticUrl) {
        if (attrValue.startsWith("http")) {
            return attrValue;
        }
        if (!preStaticUrl.trim().isEmpty()) {
            return preStaticUrl + attrValue;
        } else {
            if (attrValue.startsWith("http")) {
                return attrValue;
            }
            if (attrValue.startsWith("//")) {
                int goalIndex = newsUrl.indexOf("//");
                return newsUrl.substring(0, goalIndex) + attrValue;
            } else if (attrValue.startsWith("/")) {
                String matchStrByReg = StringUtils.matchStrByReg(newsUrl, "https?://[^/]+");
                return matchStrByReg + attrValue;
            } else if (attrValue.startsWith("./")) {
                int currentDirIndex = newsUrl.lastIndexOf("/");
                return newsUrl.substring(0, currentDirIndex + 1) + attrValue.substring(2);
            } else if (attrValue.startsWith("../../")) {
                int currentDirIndex = newsUrl.lastIndexOf("/");
                int parentDirIndex = newsUrl.substring(0, currentDirIndex).lastIndexOf("/");
                int grandFaDirIndex = newsUrl.substring(0, parentDirIndex).lastIndexOf("/");
                return newsUrl.substring(0, grandFaDirIndex + 1) + attrValue.substring(6);
            } else if (attrValue.startsWith("../")) {
                int currentDirIndex = newsUrl.lastIndexOf("/");
                int parentDirIndex = newsUrl.substring(0, currentDirIndex).lastIndexOf("/");
                return newsUrl.substring(0, parentDirIndex + 1) + attrValue.substring(3);
            } else {
                int currentDirIndex = newsUrl.lastIndexOf("/");
                return newsUrl.substring(0, currentDirIndex + 1) + attrValue;
            }
        }
    }

    /**
     * 解析html获取其中需要入库的链接和需要抓取的链接
     *
     * @param bytes html的数组
     *              //     * @param urlsMap       链接规则的map
     * @param site  site
     */
    public static void classifyUrl(byte[] bytes, Site site) {
        Rule rule = site.getConfigBean().getSiteBean().getRule();
        List<String> extractLocationList = rule.getExtractLocation();
        MDC.put("site_id", site.getConfigBean().getSiteBean().getSiteId());
        if (bytes != null) {
            Html html = null;
            try {
                html = new Html(new String(bytes, site.getConfigBean().getGeneralConfig().getCharset()));
            } catch (UnsupportedEncodingException e) {
                logger.error("栏目：" + site.getConfigBean().getXmlPath() + "的编码方式出错！！");
                e.printStackTrace();
            }
            if (extractLocationList != null && !extractLocationList.isEmpty() && html != null) {
                for (String extractLocation : extractLocationList
                ) {
                    //通过正则reg匹配链接
                    getUrlsByReg(html, site, extractLocation);
                    //通过xpath匹配链接
                    getUrlsByXpath(html, site, extractLocation);
                }
            }
        }
        //检查是否有需要从列表目录获取时间的操作


    }

    private static void getUrlsByXpath(Html html, Site site, String extractLocation) {
        BlockingQueue<PageBean> detailUrlQueue = site.getDetailUrlQueue();
        SiteBean siteBean = site.getConfigBean().getSiteBean();
        Rule rule = siteBean.getRule();
        List<Element> dbUrlXpathList = rule.getDbUrlXpathList();
        Selectable partHtmlBody = html.xpath(extractLocation);
        if (partHtmlBody != null && partHtmlBody.all() != null && partHtmlBody.all().size() != 0
                && partHtmlBody.all().get(0) != null) {
            //通过详情页连接的xpath信息把在列表页的有用信息保存到pageBean中
            if (dbUrlXpathList != null && !dbUrlXpathList.isEmpty()) {
                getDetailsUrlsByXpath(detailUrlQueue, dbUrlXpathList, partHtmlBody, site);
            } else {
                logger.info("the website has not the detailXpathConfig: " + siteBean.getWebsiteNameEn());
            }
        }
    }

    private static void getDetailsUrlsByXpath(BlockingQueue<PageBean> detailUrlQueue, List<Element> dbUrlXpathList, Selectable partHtmlBody, Site site) {
        for (Object dbUrlXpathObj : dbUrlXpathList
        ) {
            Element dbUrlXpathEle = (Element) dbUrlXpathObj;
            String preUrl = dbUrlXpathEle.attribute("preUrl").getValue();
            String detailInfoXpathExp = dbUrlXpathEle.element("detailInfo-xpathExp").getText();
            Selectable partDetailSelects = partHtmlBody.xpath(detailInfoXpathExp);
            if (partDetailSelects != null && partDetailSelects.all() != null && !partDetailSelects.all().isEmpty()) {
                List<String> partDetailList = partDetailSelects.all();
                for (String partDetailStr : partDetailList
                ) {
                    PageBean pageBean = new PageBean();
                    Map<String, String> infoMap = new HashMap<>(10);
                    Html partHtml = new Html(partDetailStr);
                    List<?> fieldList = dbUrlXpathEle.elements("field");
                    String detailUrl = "";
                    String publishDate;
                    for (Object fieldObj : fieldList
                    ) {
                        Element fieldEle = (Element) fieldObj;
                        String name = fieldEle.attribute("name").getValue();
                        switch (name) {
                            case "url_src":
                                String urlXpath = fieldEle.element("xpathExp").getText();
                                Selectable urlSelects = partHtml.xpath(urlXpath);
                                if (urlSelects != null && urlSelects.all() != null && !urlSelects.all().isEmpty()) {
                                    String originalUrl = urlSelects.all().get(0);
                                    if (!originalUrl.startsWith("http")) {
                                        detailUrl = preUrl + originalUrl;
                                    } else {
                                        detailUrl = originalUrl;
                                    }
                                } else {
                                    logger.error(site.getConfigBean().getSiteBean().getWebsiteNameEn() + "--url_src is null" +
                                            "\n所属栏目为：" + site.getConfigBean().getSiteBean().getWebsiteNameEn());
                                }
                                infoMap.put("url_src", detailUrl);
                                pageBean.setUrl(detailUrl);
                                break;
                            case "publish_date":
                                Element dateXpathExp = fieldEle.element("xpathExp");
                                String publishDateStr = "";
                                String dataTransform = "";
                                if (dateXpathExp != null) {
                                    String publishDateXpath = dateXpathExp.getText();
                                    Element dataTransformEle = fieldEle.element("dataTransform");
                                    if (dataTransformEle != null) {
                                        dataTransform = dataTransformEle.attribute("beanId").getValue();
                                    }
                                    Selectable publishDateSelects = partHtml.xpath(publishDateXpath);
                                    if (publishDateSelects != null && publishDateSelects.all() != null && !publishDateSelects.all().isEmpty()) {
                                        publishDateStr = publishDateSelects.all().get(0);
                                    } else {
                                        logger.error(site.getConfigBean().getSiteBean().getWebsiteNameEn() + "--publish_date is null" +
                                                "\n所属栏目为：" + site.getConfigBean().getSiteBean().getWebsiteNameEn());
                                    }
                                }
                                Element regExpEle = fieldEle.element("regExp");
                                publishDate = getDateString(name, dataTransform, regExpEle, publishDateStr);
                                infoMap.put("publish_date", publishDate);
                                infoMap.put("publish_date_str", publishDateStr);
                                break;
                            case "title":
                                String title = partDetailStr;
                                Element xpathExp = fieldEle.element("xpathExp");
                                if (xpathExp != null) {
                                    String titleXpath = fieldEle.element("xpathExp").getText();
                                    Selectable titleSelects = partHtml.xpath(titleXpath);
                                    if (titleSelects != null && titleSelects.all() != null && !titleSelects.all().isEmpty()) {
                                        title = titleSelects.all().get(0).trim();
                                    } else {
                                        logger.error(site.getConfigBean().getSiteBean().getWebsiteNameEn() + "--title is null" +
                                                "\n所属栏目为：" + site.getConfigBean().getSiteBean().getWebsiteNameEn());
                                    }
                                }
                                Element titleRegExp = fieldEle.element("regExp");
                                if (titleRegExp != null) {
                                    title = StringUtils.matchStrByReg(title, titleRegExp).trim();
                                }
                                infoMap.put("title", title);
                                break;
                            case "author":
                                String author = "";
                                String authorXpath = fieldEle.element("xpathExp").getText();
                                Selectable authorSelects = partHtml.xpath(authorXpath);
                                if (authorSelects != null && authorSelects.all() != null && !authorSelects.all().isEmpty()) {
                                    author = authorSelects.all().get(0);
                                } else {
                                    logger.error(site.getConfigBean().getSiteBean().getWebsiteNameEn() + "--author is null" +
                                            "\n所属栏目为：" + site.getConfigBean().getSiteBean().getWebsiteNameEn());
                                }
                                infoMap.put("author", author);
                                break;
                            default:
                        }
                    }
                    pageBean.setInfoMap(infoMap);
                    detailUrlQueue.add(pageBean);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void getUrlsByReg(Html html, Site site, String extractLocation) {
        SiteBean siteBean = site.getConfigBean().getSiteBean();
        BlockingQueue<PageBean> infoUrls = site.getDetailUrlQueue();
        BlockingQueue<String> pageUrls = site.getListUrlQueue();
        Rule urlRule = siteBean.getRule();

        Selectable partHtmlBody = html.xpath(extractLocation);
        if (partHtmlBody != null && partHtmlBody.all() != null && partHtmlBody.all().size() != 0
                && partHtmlBody.all().get(0) != null) {
            String partHtml = partHtmlBody.all().get(0);

            classifyDBUrl(partHtml, urlRule.getDbUrlRegList(), infoUrls);
            classifyPageUrl(partHtml, urlRule.getListUrlRegList(), pageUrls, site);

            Map<String, Object> spliceDbUrlMap = urlRule.getSpliceDbUrlMap();
            if (spliceDbUrlMap != null && spliceDbUrlMap.size() != 0) {
                String originalUrl = spliceDbUrlMap.get("url").toString();
                String xpathExp = spliceDbUrlMap.get("xpathExp").toString();
                List<String> paramCharList = (List<String>) spliceDbUrlMap.get("paramChar");
                List<String> paramStrList = (List<String>) spliceDbUrlMap.get("paramStr");
                Html contentHtml = new Html(partHtml);
                //获取到需要匹配的链接部分
                Selectable xpathes = contentHtml.xpath(xpathExp);
                if (xpathes != null && xpathes.all() != null && xpathes.all().size() != 0 && xpathes.all().get(0) != null) {
                    for (int i = 0; i < paramCharList.size(); i++) {
                        String paramChar = paramCharList.get(i);
                        String paramStr = paramStrList.get(i);
                        Selectable replaceContents = xpathes.xpath(paramStr);
                        if (replaceContents != null && replaceContents.all() != null && replaceContents.all().size() != 0 && replaceContents.all().get(0) != null) {
                            List<String> replaceContentList = replaceContents.all();
                            for (String replace : replaceContentList
                            ) {
                                String needUrl = originalUrl.replace(paramChar, replace);
                                PageBean pageBean = new PageBean();
                                pageBean.setUrl(needUrl);
                                infoUrls.add(pageBean);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 公用类，把链接按照指定规则放入相应的队列
     *
     * @param html         需要解析的html
     * @param extractRules 规则
     * @param urlQueue     队列
     * @param site         site
     */
    private static void classifyPageUrl(String html, List<String> extractRules, BlockingQueue<String> urlQueue, Site site) {
        if (extractRules != null && extractRules.size() > 0) {
            for (String regExp : extractRules
            ) {
                //写到 splice-url
                List<String> matchesList = extractExp2Match(html, regExp);
                matchesList.removeAll(site.getDuplicatedUrls());
                urlQueue.removeAll(matchesList);
                urlQueue.addAll(matchesList);
            }
        }
    }

    private static List<String> extractExp2Match(String html, String regExp) {
        String[] split = regExp.split("&&");
        String preUrl = split[0];
        String regex = split[1];
        return StringUtils.matchStrByReg(html, regex, preUrl);
    }

    private static void classifyDBUrl(String html, List<String> extractRules, BlockingQueue<PageBean> urlQueue) {
        if (extractRules != null && extractRules.size() > 0) {
            for (String regExp : extractRules
            ) {
                //写到 splice-url
                List<String> matchesList = extractExp2Match(html, regExp);
                boolean add2Urls = false;
                for (String url : matchesList
                ) {
                    for (PageBean pageBean : urlQueue
                    ) {
                        String pageBeanUrl = pageBean.getUrl();
                        if (pageBeanUrl != null && pageBeanUrl.equals(url)) {
                            add2Urls = true;
                            break;
                        }
                    }

                    if (!add2Urls) {
                        PageBean pageBean = new PageBean();
                        pageBean.setUrl(url);
                        urlQueue.add(pageBean);
                    }
                }
            }
        }

    }

    public static String formatContent(String content, SiteBean siteBean, String newsUrl) {
        Document document = Jsoup.parse(content);
        formatContent(document, "a", "href", siteBean, newsUrl);
        formatContent(document, "img", "src", siteBean, newsUrl);
        formatContent(document, "img", "data-src", siteBean, newsUrl);

        Elements bodyEle = document.getElementsByTag("body");
        if (bodyEle != null) {
            String content5Logo = bodyEle.html();
            String logo = siteBean.getLogo();
            Document logoDocument = Jsoup.parse(logo);
            Elements imgSelect = logoDocument.getElementsByTag("img");
            Map<String, String> newsStaticFiles = selectFileDataByUrl(siteBean);
            if (newsStaticFiles != null && newsStaticFiles.size() > 0) {
                String urlRef = newsStaticFiles.get("url_ref");
                imgSelect.attr("src", urlRef);
                logo = imgSelect.outerHtml();
                content = logo + content5Logo;
                return content;
            } else {
                return document.select("body").html();
            }
        }
        return document.html();
    }

    private static Map<String, String> selectFileDataByUrl(SiteBean siteBean) {
        Map<String, Object> fileDbMap = siteBean.getDbTableConfig().get(XmlAnalysis.FILE_TABLE_TAG);
        String logo = StringUtils.getUrlByHtmlTag(siteBean.getLogo());
        return JdbcOperate.selectNewsFileByUrlMd5(fileDbMap, logo);
    }

    private static void formatContent(Document document, String tagName, String attr, SiteBean siteBean, String newsUrl) {
        String filePreUrl = siteBean.getFilePreUrl();
        Elements tagEleList = document.getElementsByTag(tagName);
        if (tagEleList != null) {
            for (Object aEleObj : tagEleList
            ) {
                org.jsoup.nodes.Element aEle = (org.jsoup.nodes.Element) aEleObj;
                String href = aEle.attr(attr);
                if (href != null && !"".equals(href)) {
                    if (!href.contains("http")) {
                        if (href.contains("../")) {
                            href = href.replace("../", "/");
                        }
                        href = formatFileUrl(href, newsUrl, filePreUrl);
                    }
                    Map<String, String> newsFileMap = JdbcOperate.selectNewsFileByUrlMd5(siteBean.getDbTableConfig().get(XmlAnalysis.FILE_TABLE_TAG), href);
                    if (newsFileMap != null && !newsFileMap.isEmpty()) {
                        String urlRef = newsFileMap.get("url_ref");
                        aEle.attr(attr, urlRef);
                    }
                    /*if ((href.contains(".jpg") || href.contains(".img") || href.contains(".svg") || href.contains(".gif") || href.contains(".png")
                            || href.contains(".JPG") || href.contains("PDF") || href.contains("pdf") || href.contains("PNG") || href.contains(".jpeg")
                            || href.contains(".doc") || href.contains(".docx") || href.contains(".txt") || href.contains(".xls"))) {
                    }*/
                }
            }
        }
    }

    public static String deleteContentTag(String content) {
        Document document = Jsoup.parse(content);
        content = document.select("body").html();
        return content;
    }

    @Override
    public void analysis(Site site, String siteUrl) {

    }
}



