package com.ertu.news.model.bean;

import org.dom4j.Element;

import java.util.List;
import java.util.Map;

/**
 * @author hxf
 * @date 2019/4/23 10:39
 * <p>
 * 统计配置网站中的所有的链接抓取规则
 */
public class Rule {
    private List<String> dbUrlRegList;
    private List<String> listUrlRegList;
    private List<String> dbAndListUrlRegList;
    private Map<String, Object> spliceDbUrlMap;
    private Map<String, Object> spliceDeepUrlMap;
    private List<String> extractLocation;
    private List<Element> dbUrlXpathList;
    private List<Element> listUrlXpathList;
    private List<Element> dbAndListUrlXpathList;

    public List<String> getDbAndListUrlRegList() {
        return dbAndListUrlRegList;
    }

    public void setDbAndListUrlRegList(List<String> dbAndListUrlRegList) {
        this.dbAndListUrlRegList = dbAndListUrlRegList;
    }

    public List<Element> getDbAndListUrlXpathList() {
        return dbAndListUrlXpathList;
    }

    public void setDbAndListUrlXpathList(List<Element> dbAndListUrlXpathList) {
        this.dbAndListUrlXpathList = dbAndListUrlXpathList;
    }

    public List<Element> getDbUrlXpathList() {
        return dbUrlXpathList;
    }

    public void setDbUrlXpathList(List<Element> dbUrlXpathList) {
        this.dbUrlXpathList = dbUrlXpathList;
    }

    public List<Element> getListUrlXpathList() {
        return listUrlXpathList;
    }

    public void setListUrlXpathList(List<Element> listUrlXpathList) {
        this.listUrlXpathList = listUrlXpathList;
    }

    public List<String> getExtractLocation() {
        return extractLocation;
    }

    public void setExtractLocation(List<String> extractLocation) {
        this.extractLocation = extractLocation;
    }

    public List<String> getDbUrlRegList() {
        return dbUrlRegList;
    }

    public void setDbUrlRegList(List<String> dbUrlRegList) {
        this.dbUrlRegList = dbUrlRegList;
    }

    public List<String> getListUrlRegList() {
        return listUrlRegList;
    }

    public void setListUrlRegList(List<String> listUrlRegList) {
        this.listUrlRegList = listUrlRegList;
    }

    public Map<String, Object> getSpliceDbUrlMap() {
        return spliceDbUrlMap;
    }

    public void setSpliceDbUrlMap(Map<String, Object> spliceDbUrlMap) {
        this.spliceDbUrlMap = spliceDbUrlMap;
    }

    public Map<String, Object> getSpliceDeepUrlMap() {
        return spliceDeepUrlMap;
    }

    public void setSpliceDeepUrlMap(Map<String, Object> spliceDeepUrlMap) {
        this.spliceDeepUrlMap = spliceDeepUrlMap;
    }
}
