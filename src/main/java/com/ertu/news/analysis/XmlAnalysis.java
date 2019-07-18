package com.ertu.news.analysis;

import com.ertu.news.download.FileDownLoader;
import com.ertu.news.download.utils.proxy.LocalProxy;
import com.ertu.news.model.bean.*;
import com.ertu.news.utils.*;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author hxf
 * @date 09点50分
 * <p>
 * 对xml进行解析并封装
 */
public class XmlAnalysis implements Analysis {

    private static String ROOT_DIR = PropertyUtils.getPathByName(PropertyUtils.FILE_STORE_PATH_PROP);
    private final static String FILE_PATH = ROOT_DIR + "/static_image/";
    private final static String HTML_PATH = ROOT_DIR + "/static_html/";
    private final static String PDF_PATH = ROOT_DIR + "/static_pdf/";
    public final static String NEWS = "news";
    public final static String LITERATURE = "literature";
    public final static String WEBSITE_TABLE_TAG = "website-fields";
    public final static String INFO_TABLE_TAG = "context-fields";
    public final static String FILE_TABLE_TAG = "static-fields";
    private static Logger logger = Logger.getLogger(XmlAnalysis.class);

    public static void main(String[] args) {
    }

    private static void getDataBaseColumnNameByXml(ConfigBean configBean) {
        Map<String, Map<String, Object>> dbTableMap = new HashMap<>(16);
        //此处标签固定，不能修改
        final String[] dbArray = {WEBSITE_TABLE_TAG, INFO_TABLE_TAG, FILE_TABLE_TAG};
        Document document = XmlAnalysis.transferDocumentByFileDir(configBean.getXmlPath());
        Element dbColumn = document.getRootElement().element("sites").element("site").element("writeConfig").element("db");
        for (String field : dbArray
        ) {
            Map<String, Object> dataBaseMap = new HashMap<>(20);
            Element tableEle = dbColumn.element(field);
            //获取服务器信息
            String serverName = tableEle.element("server-name").getText();
            //获取数据库名称
            String dbName = tableEle.element("serverDb-name").getText();
            //获取表名
            String tableName = tableEle.element("table-name").getText();

            dataBaseMap.put("serverName", serverName);
            dataBaseMap.put("dbName", dbName);
            dataBaseMap.put("tableName", tableName);
            //获取各个字段名并存储导map中
            List<?> columnElements = tableEle.elements("field");
            for (Object columnElementObj : columnElements) {
                if (columnElementObj instanceof Element) {
                    Element columnElement = (Element) columnElementObj;
                    Map<String, String> columnMap = new HashMap<>(30);
                    String columnName = columnElement.attribute("name").getValue();
                    String typeName = columnElement.attribute("type").getValue();
                    String length = columnElement.attribute("length").getValue();
                    columnMap.put("name", columnName);
                    columnMap.put("type", typeName);
                    columnMap.put("length", length);
                    dataBaseMap.put(columnName, columnMap);
                }
            }
            dbTableMap.put(field, dataBaseMap);
        }
        configBean.getSiteBean().setDbTableConfig(dbTableMap);
    }

    /**
     * @param configBean 需要解析的配置文件的路径
     *                   //     * @return 解析xml返回的总的配置信息
     *                   包括数据库的字段内容、html的匹配信息
     */
    private static void getHtmlConfigByXml(ConfigBean configBean) {
        SiteBean siteBean = configBean.getSiteBean();
        Map<String, Map<String, Object>> htmlInfoMap = new HashMap<>(100);
        Document document = XmlAnalysis.transferDocumentByFileDir(configBean.getXmlPath());
        Element pageFields = document.getRootElement().element("sites").element("site").element("writeConfig").element("db").element("page-fields");
        //判断是资讯还是文献
        String crawlType = pageFields.element("crawl-type").getText();
        if (NEWS.equals(crawlType)) {
            List<?> siteColumns = pageFields.elements("field");
            for (Object siteColumnObj : siteColumns) {
                Map<String, Object> siteColumnMap = new HashMap<>(50);
                Element siteColumn = (Element) siteColumnObj;
                String siteColumnName = siteColumn.attribute("name").getValue();
                if (!"content".equals(siteColumnName)) {
                    //获取xml中的非content部分的配置信息
                    try {
                        getElementInfo(siteColumn, siteColumnMap, htmlInfoMap, siteColumnName);
                    } catch (Exception e) {
                        logger.error("出错信息为：" + e.getMessage() + "\n" + configBean.getXmlPath());
                        e.printStackTrace();
                    }
                } else {
                    //获取xml中的content部分的信息
                    Map<String, Object> contentSiteColumnMap = new HashMap<>(20);
                    //存放content对应的所有信息的map
                    Map<String, Object> xpathMap = new HashMap<>(20);
                    getContentElementInfo(xpathMap, siteColumn, contentSiteColumnMap);
                    htmlInfoMap.put("content", xpathMap);
                }
            }

        }
        if (LITERATURE.equals(crawlType)) {
            List<?> siteColumns = pageFields.elements("field");
            for (Object siteColumnObj : siteColumns) {
                Map<String, Object> siteColumnMap = new HashMap<>(50);
                Element siteColumn = (Element) siteColumnObj;
                String siteColumnName = siteColumn.attribute("name").getValue();
                try {
                    getElementInfo(siteColumn, siteColumnMap, htmlInfoMap, siteColumnName);
                } catch (Exception e) {
                    logger.error("出错信息为：" + e.getMessage() + "\n" + configBean.getXmlPath());
                    e.printStackTrace();
                }
            }
        }
        siteBean.setCrawlType(crawlType);
        siteBean.setHtmlConfig(htmlInfoMap);
//        logger.debug("获取到" + configBean + "对应的页面字段信息：" + htmlInfoMap);
    }

    /**
     * 获取正文部分的xpath集合
     *
     * @param xpathMap      存放content对应的所有信息的map
     * @param siteColumn    content所有的字段信息
     * @param siteColumnMap content下每个字段对应的map
     */
    private static void getContentElementInfo(Map<String, Object> xpathMap, Element siteColumn, Map<String, Object> siteColumnMap) {
        List<?> xpathList = siteColumn.elements("xpath");
        int xpathCount = 1;
        for (Object xpathElementObj : xpathList
        ) {
            Element xpathElement = (Element) xpathElementObj;
            String onlyGetText = "false";
            Attribute emptyAllowed = xpathElement.attribute("emptyAllowed");
            String emptyAllowedStr = "";
            if (emptyAllowed != null) {
                emptyAllowedStr = emptyAllowed.getValue();
            }
            String xpathExp = xpathElement.element("xpathExp").getText();
            Element removedNodes = xpathElement.element("removedNodes");
            List<String> removedNodeList = new ArrayList<>(20);
            if (null != removedNodes) {
                List<?> removeList = removedNodes.elements("xpath");
                for (Object removeEleObj : removeList
                ) {
                    //目前采用字符串拼接
                    if (removeEleObj instanceof Element) {
                        Element removeEle = (Element) removeEleObj;
                        removedNodeList.add(removeEle.getText());
                    }
                }
            }
            //初始化去除属性
            Element removedAttributes = xpathElement.element("removedAttributes");
            List<String> removedAttributeList = new ArrayList<>(20);
            if (null != removedAttributes) {
                List<?> attributeList = removedAttributes.elements("attribute");
                if (attributeList != null) {
                    for (Object attrElement : attributeList
                    ) {
                        if (attrElement instanceof Element) {
                            Element attrEle = (Element) attrElement;
                            Element attrNameEle = attrEle.element("name");
                            removedAttributeList.add(attrNameEle.getText());
                        }
                    }
                }
            }
            //初始化需要修改的属性
            Element changeAttributes = xpathElement.element("changeAttributes");
            List<Map<String, String>> changeAttributeList = new ArrayList<>(20);
            if (null != changeAttributes) {
                List<?> attributeList = changeAttributes.elements("attribute");
                if (attributeList != null) {
                    for (Object attrElement : attributeList
                    ) {
                        if (attrElement instanceof Element) {
                            Map<String, String> changeAttrMap = new HashMap<>(16);
                            Element attrEle = (Element) attrElement;
                            Element oldAttrNameEle = attrEle.element("old_name");
                            Element newAttrNameEle = attrEle.element("new_name");
                            changeAttrMap.put("old_name", oldAttrNameEle.getText());
                            changeAttrMap.put("new_name", newAttrNameEle.getText());
                            changeAttributeList.add(changeAttrMap);
                        }
                    }
                }
            }
            siteColumnMap.put("onlyGetText", onlyGetText);
            siteColumnMap.put("emptyAllowed", emptyAllowedStr);
            siteColumnMap.put("xpathExp", xpathExp);
            siteColumnMap.put("removedNodes", removedNodeList);
            siteColumnMap.put("removeAttrNames", removedAttributeList);
            siteColumnMap.put("changeAttributes", changeAttributeList);
            xpathMap.put("xpath" + xpathCount, siteColumnMap);
            xpathCount++;
        }

    }

    /**
     * @param siteColumn     html的非content节点
     * @param siteColumnMap  每个字段对应的map
     * @param htmlInfoMap    html总的数据map
     * @param siteColumnName html对应的非content字段名
     *                       <p>
     *                       修改：给pub_date、title、abstract等匹配多个xpath
     */
    private static void getElementInfo(Element siteColumn, Map<String, Object> siteColumnMap, Map<String, Map<String, Object>> htmlInfoMap, String siteColumnName) throws Exception {
        //设置onlyGetText默认值为false
        String onlyGetText = "false";
        String emptyAllowed = "true";
        String preUrl = "";
        List<String> xpathExpList = new ArrayList<>();
        List<String> prefixList = new ArrayList<>();
        List<String> suffixList = new ArrayList<>();
        List<Element> regExpList = new ArrayList<>();
        List<String> beanIdList = new ArrayList<>();
        List<String> clearList = new ArrayList<>();
        String value = "";
        Element xpathElement = siteColumn.element("xpath");
        Attribute onlyGetTextAttr = xpathElement.attribute("onlyGetText");
        if (null != onlyGetTextAttr) {
            onlyGetText = onlyGetTextAttr.getValue();
        }
        Attribute emptyAllowedAttr = xpathElement.attribute("emptyAllowed");
        if (null != emptyAllowedAttr) {
            emptyAllowed = emptyAllowedAttr.getValue();
        }
        Attribute preUrlAttr = siteColumn.attribute("preUrl");
        if (preUrlAttr != null) {
            preUrl = preUrlAttr.getValue();
        }
        //添加多个对应字段
        List<?> pageFieldEleList = xpathElement.elements("page-field");
        if (pageFieldEleList != null && pageFieldEleList.size() != 0) {
            for (Object pageFieldEleObj : pageFieldEleList
            ) {
                if (pageFieldEleObj instanceof Element) {
                    Element pageFieldEle = (Element) pageFieldEleObj;
                    //获取页面以外的固定字段
                    Element leadingHtml = pageFieldEle.element("leadingHtml");
                    if (leadingHtml != null) {
                        value = leadingHtml.getText();
                    } else {
                        //对出版时间标签的特殊处理
                        String beanId = "";
                        if ("publish_date".equals(siteColumnName)) {
                            Element dataTransform = pageFieldEle.element("dataTransform");
                            if (dataTransform != null) {
                                beanId = dataTransform.attribute("beanId").getValue();
                            }
                        }
                        beanIdList.add(beanId);
                        //获取字段的xpath
                        Element xpathExp = pageFieldEle.element("xpathExp");
                        if (xpathExp != null) {
                            xpathExpList.add(xpathExp.getText());
                        }
                        //获取字段的其他附加约束   prefix、suffix、regExp等
                        Element prefixElement = pageFieldEle.element("prefix");
                        String prefix = "";
                        if (null != prefixElement) {
                            prefix = prefixElement.getText();
                        }
                        prefixList.add(prefix);
                        Element suffixElement = pageFieldEle.element("suffix");
                        String suffix = "";
                        if (null != suffixElement) {
                            suffix = suffixElement.getText();
                        }
                        suffixList.add(suffix);
                        Element regExpElement = pageFieldEle.element("regExp");
                        regExpList.add(regExpElement);

                        //添加对特殊字段内容的删除替换操作
                        Element clearElement = pageFieldEle.element("clear");
                        String clear = "";
                        if (null != clearElement) {
                            clear = clearElement.getText();
                        }
                        clearList.add(clear);
                    }
                }
            }
        }
        Map<String, Object> pageFieldMap = new HashMap<>(10);
        pageFieldMap.put("xpathExp", xpathExpList);
        pageFieldMap.put("regExp", regExpList);
        pageFieldMap.put("prefix", prefixList);
        pageFieldMap.put("suffix", suffixList);
        pageFieldMap.put("beanId", beanIdList);
        pageFieldMap.put("clear", clearList);
        pageFieldMap.put(siteColumnName, value);
        siteColumnMap.put("onlyGetText", onlyGetText);
        siteColumnMap.put("emptyAllowed", emptyAllowed);
        siteColumnMap.put("preUrl", preUrl);
        siteColumnMap.put("page-field", pageFieldMap);
        htmlInfoMap.put(siteColumnName, siteColumnMap);
    }

    /**
     * 获得网站的入口、写库链接正则、抓取链接正则
     *
     * @param configBean 配置文件的路径
     *                   //     * @return Map<String, Set < String>>    <链接种类，链接列表>
     */
    private static void getSiteUrlRuleByXml(ConfigBean configBean) {
        SiteBean siteBean = configBean.getSiteBean();
        Document document = transferDocumentByFileDir(configBean.getXmlPath());
        Element siteElement = document.getRootElement().element("sites").element("site");
        //获取入口链接
        getSiteSeedUrls(siteBean, siteElement);

        //获取入口链接内匹配链接的区域配置
        getAllUrlRule(siteBean, siteElement);
    }

    private static void getAllUrlRule(SiteBean siteBean, Element siteElement) {
        Rule urlRule = new Rule();
        Element extractLocation = siteElement.element("extractLocation");
        List<String> extractLocationList = new ArrayList<>(16);
        String extractLocationText = "//body";
        if (extractLocation != null) {
            extractLocationText = extractLocation.getText();
        }
        extractLocationList.add(extractLocationText);
        //获取入口页面需要抓取和入库的链接配置
        Element crawlRegExps = siteElement.element("crawlRegExps");
        if (crawlRegExps != null) {
            List<?> htmlUrlList = crawlRegExps.elements("regExp");
            List<String> dbUrlRegList = new ArrayList<>(16);
            List<String> crawlUrlRegList = new ArrayList<>(16);
            List<String> dbAndListUrlRegList = new ArrayList<>(16);
            if (htmlUrlList != null && htmlUrlList.size() != 0) {
                for (Object htmlUrlEleObj : htmlUrlList
                ) {
                    if (htmlUrlEleObj instanceof Element) {
                        Element htmlUrlEle = (Element) htmlUrlEleObj;
                        String htmlUrlReg = htmlUrlEle.getText();
                        String write = htmlUrlEle.attribute("write").getValue();
                        String extract = htmlUrlEle.attribute("extract").getValue();
                        String detailPreUrl = htmlUrlEle.attribute("preUrl").getValue();
                        siteBean.setDetailPreUrl(detailPreUrl);
                        //考虑到对于正则的匹配需求暂时不会很复杂，所以此处直接把preUrl拼接到正则中
                        String preUrl = htmlUrlEle.attribute("preUrl").getValue();
                        if ("true".equals(write) && "false".equals(extract)) {
                            dbUrlRegList.add(preUrl + "&&" + htmlUrlReg);
                        } else if ("false".equals(write) && "true".equals(extract)) {
                            crawlUrlRegList.add(preUrl + "&&" + htmlUrlReg);
                        } else {
                            dbAndListUrlRegList.add(preUrl + "&&" + htmlUrlReg);
                        }
                    }
                }
            }
            //针对需要拼接链接的栏目，特殊处理
            List<?> spliceElements = crawlRegExps.elements("splice-url");
            if (spliceElements != null && !spliceElements.isEmpty()) {
                getSpliceUrlRule(spliceElements, urlRule);
            }
            urlRule.setListUrlRegList(crawlUrlRegList);
            urlRule.setDbUrlRegList(dbUrlRegList);
            urlRule.setDbAndListUrlRegList(dbAndListUrlRegList);
        }

        //判断是否需要从列表页中获取入库信息
        Element crawlXpathExps = siteElement.element("crawlXpathExps");
        if (crawlXpathExps != null) {
            getDetailInfoFromListPage(crawlXpathExps, urlRule);
        }

        urlRule.setExtractLocation(extractLocationList);
        //获取文件链接前缀
        Element filePreUrlEle = siteElement.element("static-preUrl");
        if (filePreUrlEle != null) {
            siteBean.setFilePreUrl(filePreUrlEle.getText());
        }
        siteBean.setRule(urlRule);
    }

    private static void getDetailInfoFromListPage(Element crawlXpathExps, Rule urlRule) {
        List<?> xpathExpList = crawlXpathExps.elements("xpathExp");
        List<Element> dbUrlXpathList = new ArrayList<>();
        List<Element> listUrlXpathList = new ArrayList<>();
        List<Element> dbAndListUrlXpathList = new ArrayList<>();
        if (xpathExpList != null && !xpathExpList.isEmpty()) {
            for (Object xpathExpObj : xpathExpList
            ) {
                Element xpathExpEle = (Element) xpathExpObj;
                String write = xpathExpEle.attribute("write").getValue();
                String extract = xpathExpEle.attribute("extract").getValue();
                if ("true".equals(write) && "false".equals(extract)) {
                    dbUrlXpathList.add(xpathExpEle);
                } else if ("true".equals(extract) && "false".equals(write)) {
                    listUrlXpathList.add(xpathExpEle);
                } else {
                    dbAndListUrlXpathList.add(xpathExpEle);
                }
            }
        }
        urlRule.setDbUrlXpathList(dbUrlXpathList);
        urlRule.setListUrlXpathList(listUrlXpathList);
        urlRule.setDbAndListUrlXpathList(dbAndListUrlXpathList);
    }

    private static void getSpliceUrlRule(List<?> spliceElements, Rule urlRule) {
        Map<String, Object> spliceDbUrlMap = new HashMap<>(16);
        Map<String, Object> spliceCrawlUrlMap = new HashMap<>(16);
        if (spliceElements != null && spliceElements.size() != 0) {
            for (Object spliceElementObj : spliceElements
            ) {
                if (spliceElementObj instanceof Element) {
                    Element spliceElement = (Element) spliceElementObj;
                    String write = spliceElement.attribute("write").getValue();
                    String extract = spliceElement.attribute("extract").getValue();
                    if ("true".equals(write) && "false".equals(extract)) {
                        String url = spliceElement.element("url").getText();
                        String spliceXpath = spliceElement.element("xpathExp").getText();
                        List<String> urlParamCharList = new ArrayList<>();
                        List<String> urlParamStrList = new ArrayList<>();
                        List<?> paramList = spliceElement.element("url-params").elements("param");
                        if (paramList != null && paramList.size() != 0) {
                            for (Object paramEleObj : paramList
                            ) {
                                if (paramEleObj instanceof Element) {
                                    Element paramEle = (Element) paramEleObj;
                                    String replaceChar = paramEle.attribute("name").getValue();
                                    String replaceStr = paramEle.getText();
                                    urlParamCharList.add(replaceChar);
                                    urlParamStrList.add(replaceStr);

                                    spliceDbUrlMap.put("url", url);
                                    spliceDbUrlMap.put("xpathExp", spliceXpath);
                                    spliceDbUrlMap.put("paramChar", urlParamCharList);
                                    spliceDbUrlMap.put("paramStr", urlParamStrList);
                                }
                            }
                        }
                    }
                }
            }
        }
        urlRule.setSpliceDeepUrlMap(spliceCrawlUrlMap);
        urlRule.setSpliceDbUrlMap(spliceDbUrlMap);
    }

    private static void getSiteSeedUrls(SiteBean siteBean, Element siteElement) {
        List<?> mainUrlList = siteElement.element("seeds").elements("seed");
        //每个线程都对应相关的链接Map，故不考虑线程安全
        List<String> doorUrlList = new ArrayList<>(100);
        if (mainUrlList != null && mainUrlList.size() != 0) {
            for (Object seedEleObj : mainUrlList
            ) {
                Element seedEle = (Element) seedEleObj;
                String doorUrl = seedEle.getText();
                if (doorUrl != null && !"".equals(doorUrl)) {
                    doorUrlList.add(doorUrl);
                }
            }
        }
        siteBean.setSiteUrls(doorUrlList);
    }

    public static Document transferDocumentByFileDir(String xmlConfigDir) {
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(new BufferedReader(new InputStreamReader(new FileInputStream(new File(xmlConfigDir)), StandardCharsets.UTF_8)));
            document.setXMLEncoding("utf-8");
        } catch (DocumentException | FileNotFoundException e) {
            logger.error("文件路径为：" + xmlConfigDir);
            e.printStackTrace();
        }
        return document;
    }

    /**
     * 对栏目的xml文件进行初始化封装
     *
     * @param configFileDir configFileDir
     */
    @Override
    public void analysis(Site site, String configFileDir) {
        //初始化configBean
        ConfigBean configBean = new ConfigBean();
        site.setConfigBean(configBean);
        configBean.setXmlPath(configFileDir);
        //初始化ConfigBean
        initSiteConfig(site, configFileDir);
        //初始化网站请求信息
        initSiteRequest(site);

        //添加seedPage的map
        Map<String, Map<String, Object>> seedPage = new HashMap<>(16);
        site.getConfigBean().getSiteBean().setSeedPage(seedPage);
        //实例化rssInfo的map
        Map<String, Map<String, String>> rssInfo = new HashMap<>(1000);
        site.getConfigBean().getSiteBean().setRssInfo(rssInfo);

        //实例化详情页的链接集合
        BlockingQueue<PageBean> infoUrls = new LinkedBlockingDeque<>();
        site.setDetailUrlQueue(infoUrls);

        //实例化列表页的链接集合
        BlockingQueue<String> pageUrls = new LinkedBlockingDeque<>();
        site.setListUrlQueue(pageUrls);

        //实例化去重链接的翻页集合
        Set<String> duplicatedSet = new HashSet<>();
        site.setDuplicatedUrls(duplicatedSet);

        //默认为false
        site.setFinished(false);
    }

    @Override
    public void analysis(Site site, byte[] body, HashMap<String, String> infoMap) {

    }

    private static void initSiteRequest(Site site) {
        Document configDocument = transferDocumentByFileDir(site.getConfigBean().getXmlPath());
        Request request = new Request();
        Element requestEle = configDocument.getRootElement().element("request");
        if (requestEle != null) {
            String domainUrl = requestEle.element("domainUrl").getText();
            request.setDomain(domainUrl);
        }
        site.setRequest(request);
    }

    private static void initSiteConfig(Site site, String configFileDir) {
        Document configDocument = transferDocumentByFileDir(configFileDir);
        Element rootElement = configDocument.getRootElement();

        //初始化GeneratorConfig
        initGeneral(site, rootElement, configFileDir);
        //初始化SiteBean
        initSiteBean(site, rootElement);
    }

    private static void initSiteBean(Site site, Element rootElement) {
        ConfigBean configBean = site.getConfigBean();
        SiteBean siteBean = new SiteBean();
        //获取当前xml的siteID
        Element siteElement = rootElement.element("sites").element("site");
        if (siteElement != null) {
            //获取当前xml的入口连接
            List<?> columnUrl = siteElement.element("seeds").elements("seed");
            if (columnUrl != null && columnUrl.size() != 0) {
                List<String> columnUrlList = new ArrayList<>();
                for (Object columnUrlObj : columnUrl
                ) {
                    Element columnUrlEle = (Element) columnUrlObj;
                    String seedUrl = columnUrlEle.getText();
                    if (!"".equals(seedUrl)) {
                        columnUrlList.add(seedUrl);
                    }
                }
                siteBean.setSiteUrls(columnUrlList);
            } else {
                logger.error(siteBean.getWebsiteNameEn() + "的入口连接为空");
            }
            siteBean.setWebsiteNameEn(siteElement.attribute("name").getValue());
            siteBean.setWebsiteColumnNameEn(siteElement.element("category").getText());
            siteBean.setWebsiteNameCn(siteElement.element("description").getText());
            siteBean.setWebsiteColumnNameCn(siteElement.element("category_desc").getText());
            String siteId = siteElement.attribute("id").getValue();
            siteBean.setSiteId(Integer.parseInt(siteId));
            configBean.setSiteBean(siteBean);

            //获取配置文件的链接规则
            getSiteUrlRuleByXml(configBean);

            //获取相关数据库的链接规则
            getDataBaseColumnNameByXml(configBean);

            //获取入库信息的xpath、正则等配置
            getHtmlConfigByXml(configBean);

            //获取栏目的基本信息
            getInitialWebsiteInfo(configBean);

            //获取logo图片信息
            getSiteLogoByXml(configBean);
        } else {
            logger.error(configBean.getXmlPath() + "配置文件无siteId");
        }
    }

    private static void getSiteLogoByXml(ConfigBean configBean) {
        Document xmlDocument = XmlAnalysis.transferDocumentByFileDir(configBean.getXmlPath());
        Element pageField = xmlDocument.getRootElement().element("sites").element("site").element("writeConfig").element("db").element("page-fields");
        if (pageField != null) {
            List<?> fieldEleList = pageField.elements("field");
            for (Object fieldObj : fieldEleList
            ) {
                Element fieldEle = (Element) fieldObj;
                String name = fieldEle.attribute("name").getValue();
                if ("content".equals(name)) {
                    Element logoELe = fieldEle.element("leadingHtml");
                    if (logoELe != null) {
                        String logoELeText = logoELe.getText();
                        configBean.getSiteBean().setLogo(logoELeText);
                    }
                }
            }
        }
    }

    private static void initGeneral(Site site, Element rootElement, String configXmlDir) {
        ConfigBean configBean = new ConfigBean();
        GeneralConfig generalConfig = new GeneralConfig();
        //获取栏目配置中的链接规则配置
        Element generalElement = rootElement.element("general");
        //获取抓取时间列表
        getSpiderTimes(generalElement, generalConfig);
        //获取访问类型、字节大小等信息
        getSiteContentType(generalElement, generalConfig);
        configBean.setGeneralConfig(generalConfig);
        configBean.setXmlPath(configXmlDir);
        site.setConfigBean(configBean);
    }

    private static void getSiteContentType(Element generalElement, GeneralConfig generalConfig) {
        boolean caseSensitiveFilesystem = false;
        String caseSensitiveFilesystemStr = generalElement.element("caseSensitiveFilesystem").getText();
        if (!"true".equals(caseSensitiveFilesystemStr)) {
            caseSensitiveFilesystem = true;
        }
        List<?> contentTypeList = generalElement.element("contentTypeMap").elements("contentType");
        Map<String, String> contentTypeMap = new HashMap<>(10);
        for (Object contentTypeObj : contentTypeList
        ) {
            Element contentTypeEle = (Element) contentTypeObj;
            String contentType = contentTypeEle.attribute("name").getValue();
            String typeEleText = contentTypeEle.getText();
            contentTypeMap.put(contentType, typeEleText);
        }
        boolean useProxy = false;
        Element useProxyEle = generalElement.element("useProxy");
        if (useProxyEle != null) {
            String useProxyStr = useProxyEle.getText();
            if ("true".equals(useProxyStr)) {
                useProxy = true;
            }
        }
        //获取编码方式
        Element downloadEle = generalElement.element("download");
        if (downloadEle != null) {
            Element charsetEle = downloadEle.element("charset");
            if (charsetEle != null) {
                String charset = charsetEle.getText();
                generalConfig.setCharset(charset);
            } else {
                generalConfig.setCharset("utf-8");
            }
        }

        generalConfig.setContentTypes(contentTypeMap);
        generalConfig.setUseForeignProxy(useProxy);
        generalConfig.setCaseSensitiveFilesystem(caseSensitiveFilesystem);
    }

    private static void getSpiderTimes(Element generalElement, GeneralConfig generalConfig) {
        Element scheduleElement = generalElement.element("schedule");
        if (scheduleElement != null) {
            //获取是否需要抓取
            String enabled = scheduleElement.attribute("enabled").getValue();
            String isSchedule = "true";
            if (isSchedule.equals(enabled)) {
                generalConfig.setEnabled(true);
            } else {
                generalConfig.setEnabled(false);
            }
            //获取每周的抓取频率
            Element frequencyEle = scheduleElement.element("day-of-week");
            if (frequencyEle != null) {
                String frequency = frequencyEle.getText();
                generalConfig.setFrequency(frequency);
            } else {
                generalConfig.setFrequency("-1");
            }
            //获取每天的抓取时间
            List<?> timeElements = scheduleElement.element("times").elements("time");
            List<String> scheduleTimeList = new ArrayList<>();
            extractEleList(timeElements, scheduleTimeList);
            generalConfig.setScheduleTimes(scheduleTimeList);
        }
    }

    /**
     * 获取网站内容的静态文件的路径
     *
     * @param site    栏目的对应实体类
     * @param fileUrl 文件对应的url
     * @param title   正文标题
     * @return 文件相关的map
     */
    public static Map<String, String> getNewsFileInfoByUrl(Site site, String fileUrl, String title) {
        ConfigBean configBean = site.getConfigBean();
        //获取小飞机代理
        HttpHost host = LocalProxy.getHttpHost();
        String filePath;
        SiteBean siteBean = configBean.getSiteBean();
        int siteId = siteBean.getSiteId();
        int newsId = siteBean.getNewsId();
        if (title.length() > 50) {
            title = title.substring(0, 50).trim();
        }
        title = StringUtils.titleFilter(title);
        //根据crawlType获取路径
        filePath = getFilePathByCrawlType(configBean, siteBean.getCrawlType(), title);
        Map<String, String> staticSourceMap = new HashMap<>(16);
        Map<String, Object> download;
        //对连接用域名进行判断，是否是网站内部文件
        if (XmlAnalysis.NEWS.equals(siteBean.getCrawlType())) {
            if (!fileUrl.contains("jpg") && !fileUrl.contains("svg") && !fileUrl.contains("image") && !fileUrl.contains("gif")
                    && !fileUrl.contains("png") && !fileUrl.contains("doc") && !fileUrl.contains("xls") && !fileUrl.contains("txt")
                    && !fileUrl.contains("JPG") && !fileUrl.contains("PNG") && !fileUrl.contains("pdf")
                    && !fileUrl.contains("jpeg") && !fileUrl.contains("docx")) {
                return null;
            }
        }
        int tryTimes = 0;
        download = FileDownLoader.download(fileUrl, host);
        //对图片下载失败进行处理
        while (download == null) {
            if (tryTimes < 3) {
                download = FileDownLoader.download(fileUrl, host);
                tryTimes++;
            } else {
                return null;
            }
        }
        File file = null;
        String fileDir = "";
        String suffix = "";
        int pdfPage = 0;
        if (download.size() != 0) {
            Object typeObj = download.get("type");
            Object content = download.get("content");
            if (typeObj instanceof String) {
                String type = ((String) typeObj).toLowerCase();
                if (type.contains(".svg") || fileUrl.contains(".svg")) {
                    suffix = "svg";
                } else if (fileUrl.contains(".gif") || type.contains(".gif")) {
                    suffix = "gif";
                } else if (fileUrl.contains(".jpg") || fileUrl.contains(".image") || type.contains("jpg") || type.contains("image")) {
                    suffix = "jpg";
                } else if (type.contains("png") || fileUrl.contains(".png")) {
                    suffix = "png";
                } else if (type.contains("doc") || fileUrl.contains(".doc")) {
                    suffix = "doc";
                } else if (type.contains("docx") || fileUrl.contains(".docx")) {
                    suffix = "doc";
                } else if (type.contains("xls") || fileUrl.contains(".xls")) {
                    suffix = "xls";
                } else if (type.contains("txt") || fileUrl.contains(".txt")) {
                    suffix = "txt";
                } else if (type.contains("jpeg") || fileUrl.contains(".jpeg")) {
                    suffix = "jpeg";
                }
            }
            //把文件以特定的形式写进磁盘中
            if (content instanceof byte[]) {
                byte[] contentByte = (byte[]) content;
                if ("".equals(suffix)) {
                    pdfPage = PdfUtils.getPdfPage(contentByte, fileUrl);
                    if (pdfPage > 0) {
                        suffix = "pdf";
                    } else {
                        logger.info("该文件链接类型暂未识别：" + fileUrl);
                        return null;
                    }
                }
                fileDir = filePath + new Random().nextInt(20) + "." + suffix;
                file = new File(fileDir);
                if (!file.exists()) {
                    try {
                        FileUtils.writeByteArrayToFile(file, contentByte);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (file != null) {
            staticSourceMap.put("news_id", String.valueOf(newsId));
            staticSourceMap.put("type", suffix);
            staticSourceMap.put("url_src", fileUrl);
            staticSourceMap.put("url_ref", fileDir.substring(ROOT_DIR.length()));
            staticSourceMap.put("site_id", String.valueOf(siteId));
            staticSourceMap.put("title", title);
            staticSourceMap.put("pdf_page", pdfPage + "");
        }
        return staticSourceMap;
    }

    public static String getHtmlPathByCrawlType(ConfigBean configBean, String crawlType, String title) {
        SiteBean siteBean = configBean.getSiteBean();
        String path = "";
        title = StringUtils.titleFilter(title);
        Date date = new Date();
        int newsId = siteBean.getNewsId();
        if (newsId <= 0) {
            newsId = 1;
        }
        if (XmlAnalysis.NEWS.equals(crawlType)) {
            String dateToString = TimeUtils.dateToString(date, "yyyy-MM-dd");
            if (title.length() > 50) {
                path = HTML_PATH + siteBean.getWebsiteNameEn() + "_" + siteBean.getSiteId() + "/" + dateToString + "/" + newsId + "_" + title.substring(50) + ".html";
            } else {
                path = HTML_PATH + siteBean.getWebsiteNameEn() + "_" + siteBean.getSiteId() + "/" + dateToString + "/" + newsId + "_" + title + ".html";
            }
        }
        if (XmlAnalysis.LITERATURE.equals(crawlType)) {
            String dateToString = TimeUtils.dateToString(date, "yyyy-MM-dd HH");
            //+ configBean.getNewsId() / 100 + "/" + configBean.getNewsId() / 10000 + "/" + configBean.getNewsId()
            path = HTML_PATH + siteBean.getWebsiteNameEn() + "_" + siteBean.getSiteId() + "/" + dateToString + "/" + title + ".html";
        }
        return path.substring(ROOT_DIR.length());
    }

    private static String getFilePathByCrawlType(ConfigBean configBean, String crawlType, String title) {
        String path = "";
        SiteBean siteBean = configBean.getSiteBean();
        Date date = new Date();
        if (XmlAnalysis.NEWS.equals(crawlType)) {
            String dateToString = TimeUtils.dateToString(date, "yyyy-MM-dd");
            path = FILE_PATH + siteBean.getWebsiteNameEn() + "_" + siteBean.getSiteId() + "/" + dateToString + "/" + siteBean.getNewsId() + "_" + title + "/";
        }
        if (XmlAnalysis.LITERATURE.equals(crawlType)) {
            String dateToString = TimeUtils.dateToString(date, "yyyy-MM-dd HH");
            //+ configBean.getNewsId() / 100 + "/" + configBean.getNewsId() / 10000 + "/" + configBean.getNewsId()
            path = FILE_PATH + siteBean.getWebsiteNameEn() + "_" + siteBean.getSiteId() + "/" + dateToString + "/" + "_" + title + "/";
        }
        return path;
    }

    private static void getInitialWebsiteInfo(ConfigBean configBean) {
        Map<String, String> webInfoMap = new HashMap<>(16);
        Document configDocument = XmlAnalysis.transferDocumentByFileDir(configBean.getXmlPath());
        Element site = configDocument.getRootElement().element("sites").element("site");
        if (site != null) {
            String websiteNameEn = site.attribute("name").getValue();
            String siteId = site.attribute("id").getValue();
            String websiteNameCn = site.element("description").getText();
            String columnNameEn = site.element("category").getText();
            String columnNameCn = site.element("category_desc").getText();
            List<?> elements = site.element("seeds").elements("seed");
            List<String> seedList = new ArrayList<>();
            if (elements != null) {
                extractEleList(elements, seedList);
            }
            webInfoMap.put("Web_Name_EN", websiteNameEn);
            webInfoMap.put("Site_Id", siteId);
            webInfoMap.put("Web_Name_CH", websiteNameCn);
            webInfoMap.put("Column_Name_EN", columnNameEn);
            webInfoMap.put("Column_Name_CH", columnNameCn);
            webInfoMap.put("Url", seedList.toString());
        }
        configBean.getSiteBean().setWebsiteInfo(webInfoMap);
    }

    private static void extractEleList(List<?> elements, List<String> seedList) {
        for (Object seedElementObj : elements
        ) {
            if (seedElementObj instanceof Element) {
                Element seedElement = (Element) seedElementObj;
                String text = seedElement.getText();
                seedList.add(text);
            }

        }
    }

    public static void traverseConfigByDir(String totalConfigDir, List<Site> sourceConfigQueue) {
        File totalFile = new File(totalConfigDir);
        List<File> configList = new ArrayList<>(1000);
        TraverseFile.traverseFileByRecurse(totalFile, configList);
        for (File configFile : configList
        ) {
            //初始化栏目的配置文件并封装为site对象
            try {
                Site site = new Site();
                Analysis xmlAnalysis = new XmlAnalysis();
                xmlAnalysis.analysis(site, configFile.getAbsolutePath());
                MDC.put("site_id", site.getConfigBean().getSiteBean().getSiteId());
                sourceConfigQueue.add(site);
            } catch (Exception e) {
                logger.error("配置栏目：" + configFile.getAbsolutePath() + "--时出错\n" + "出错信息为：" + e.getMessage());
                e.printStackTrace();
            }
        }
        logger.info("website configs have been added, total counts: " + sourceConfigQueue.size());
    }

    public static String getPdfPathByCrawlType(SiteBean siteBean, String title) {
        String crawlType = siteBean.getCrawlType();
        Date date = new Date();
        String pdfPath = "";
        if (XmlAnalysis.NEWS.equals(crawlType)) {
            String dateStr = TimeUtils.dateToString(date, "yyyy-MM-dd");
            if (title.length() > 50) {
                pdfPath = PDF_PATH + siteBean.getWebsiteNameEn() + "_" + siteBean.getSiteId() + "/" + dateStr + "/" + siteBean.getNewsId() + "_" + title.substring(50) + ".pdf";
            } else {
                pdfPath = PDF_PATH + siteBean.getWebsiteNameEn() + "_" + siteBean.getSiteId() + "/" + dateStr + "/" + siteBean.getNewsId() + "_" + title + ".pdf";
            }
        }
        return pdfPath;
    }
}
