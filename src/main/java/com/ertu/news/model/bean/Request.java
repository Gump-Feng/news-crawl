package com.ertu.news.model.bean;

import java.util.Map;

/**
 * @author hxf
 * @date 2019/4/18 9:57
 */
public class Request {
    private Map<String, String> headers;
    private String domain;
    private String type;
    private Map<String, String> cookies;

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
