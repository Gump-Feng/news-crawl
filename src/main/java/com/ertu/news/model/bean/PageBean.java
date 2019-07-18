package com.ertu.news.model.bean;

import java.util.Map;

/**
 * @author hxf
 * @date 2019/4/28 16:35
 *
 * 存放列表页中需要入库的的内容
 */
public class PageBean {
    private String url;
    private Map<String, String> infoMap;
    private boolean isPdf;

    public boolean isPdf() {
        return isPdf;
    }

    public void setPdf(boolean pdf) {
        isPdf = pdf;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getInfoMap() {
        return infoMap;
    }

    public void setInfoMap(Map<String, String> infoMap) {
        this.infoMap = infoMap;
    }
}
