package com.ertu.news.model.bean;

import org.dom4j.Document;

/**
 * @author hxf
 * @date 11点12分
 * 栏目的配置文件对应的实体类
 */
public class ConfigBean {
    private GeneralConfig generalConfig;
    private SiteBean siteBean;
    private Document xmlDocument;
    private String xmlPath;

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public Document getXmlDocument() {
        return xmlDocument;
    }

    public void setXmlDocument(Document xmlDocument) {
        this.xmlDocument = xmlDocument;
    }

    public GeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    public void setGeneralConfig(GeneralConfig generalConfig) {
        this.generalConfig = generalConfig;
    }

    public SiteBean getSiteBean() {
        return siteBean;
    }

    public void setSiteBean(SiteBean siteBean) {
        this.siteBean = siteBean;
    }
}
