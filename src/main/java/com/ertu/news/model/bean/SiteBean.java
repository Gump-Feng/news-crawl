package com.ertu.news.model.bean;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author hxf
 * @date 2019/5/5 10:45
 */
public class SiteBean {
    private int siteId;
    private int newsId;
    private File config;
    private String websiteNameEn;
    private String websiteColumnNameEn;
    private String websiteNameCn;
    private String websiteColumnNameCn;
    private String xmlDir;
    private String crawlType;
    private List<String> siteUrls;
    private String filePreUrl;
    private String detailPreUrl;
    private Rule rule;
    private Map<String, Map<String, Object>> dbTableConfig;
    private Map<String, Map<String, Object>> htmlConfig;
    private Map<String, String> websiteInfo;
    private Map<String, Map<String, String>> rssInfo;
    private Map<String, Map<String, Object>> seedPage;
    private String logo;

    public String getDetailPreUrl() {
        return detailPreUrl;
    }

    public void setDetailPreUrl(String detailPreUrl) {
        this.detailPreUrl = detailPreUrl;
    }

    public String getWebsiteNameCn() {
        return websiteNameCn;
    }

    public void setWebsiteNameCn(String websiteNameCn) {
        this.websiteNameCn = websiteNameCn;
    }

    public String getWebsiteColumnNameCn() {
        return websiteColumnNameCn;
    }

    public void setWebsiteColumnNameCn(String websiteColumnNameCn) {
        this.websiteColumnNameCn = websiteColumnNameCn;
    }

    public String getWebsiteColumnNameEn() {
        return websiteColumnNameEn;
    }

    public void setWebsiteColumnNameEn(String websiteColumnNameEn) {
        this.websiteColumnNameEn = websiteColumnNameEn;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Map<String, Map<String, Object>> getDbTableConfig() {
        return dbTableConfig;
    }

    public void setDbTableConfig(Map<String, Map<String, Object>> dbTableConfig) {
        this.dbTableConfig = dbTableConfig;
    }

    public Map<String, Map<String, Object>> getHtmlConfig() {
        return htmlConfig;
    }

    public void setHtmlConfig(Map<String, Map<String, Object>> htmlConfig) {
        this.htmlConfig = htmlConfig;
    }

    public Map<String, String> getWebsiteInfo() {
        return websiteInfo;
    }

    public void setWebsiteInfo(Map<String, String> websiteInfo) {
        this.websiteInfo = websiteInfo;
    }

    public Map<String, Map<String, String>> getRssInfo() {
        return rssInfo;
    }

    public void setRssInfo(Map<String, Map<String, String>> rssInfo) {
        this.rssInfo = rssInfo;
    }

    public Map<String, Map<String, Object>> getSeedPage() {
        return seedPage;
    }

    public void setSeedPage(Map<String, Map<String, Object>> seedPage) {
        this.seedPage = seedPage;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public int getNewsId() {
        return newsId;
    }

    public void setNewsId(int newsId) {
        this.newsId = newsId;
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
    }

    public String getWebsiteNameEn() {
        return websiteNameEn;
    }

    public void setWebsiteNameEn(String websiteNameEn) {
        this.websiteNameEn = websiteNameEn;
    }

    public String getXmlDir() {
        return xmlDir;
    }

    public void setXmlDir(String xmlDir) {
        this.xmlDir = xmlDir;
    }

    public String getCrawlType() {
        return crawlType;
    }

    public void setCrawlType(String crawlType) {
        this.crawlType = crawlType;
    }

    public List<String> getSiteUrls() {
        return siteUrls;
    }

    public void setSiteUrls(List<String> siteUrls) {
        this.siteUrls = siteUrls;
    }

    public String getFilePreUrl() {
        return filePreUrl;
    }

    public void setFilePreUrl(String filePreUrl) {
        this.filePreUrl = filePreUrl;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }
}
