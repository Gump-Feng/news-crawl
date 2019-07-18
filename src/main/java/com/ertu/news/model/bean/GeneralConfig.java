package com.ertu.news.model.bean;

import java.util.List;
import java.util.Map;

/**
 * @author hxf
 * @date 2019/5/5 10:53
 * 栏目的采集基本信息
 */
public class GeneralConfig {
    private List<String> scheduleTimes;
    private boolean useForeignProxy;
    private String frequency;
    private Map<String, String> contentTypes;
    private Integer maxKBytes;
    private String extRegExp;
    private String contentTypeRegExp;
    private boolean enabled;
    private boolean caseSensitiveFilesystem;
    private String charset;

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isCaseSensitiveFilesystem() {
        return caseSensitiveFilesystem;
    }

    public void setCaseSensitiveFilesystem(boolean caseSensitiveFilesystem) {
        this.caseSensitiveFilesystem = caseSensitiveFilesystem;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getScheduleTimes() {
        return scheduleTimes;
    }

    public void setScheduleTimes(List<String> scheduleTimes) {
        this.scheduleTimes = scheduleTimes;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public boolean isUseForeignProxy() {
        return useForeignProxy;
    }

    public void setUseForeignProxy(boolean useForeignProxy) {
        this.useForeignProxy = useForeignProxy;
    }

    public Map<String, String> getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes(Map<String, String> contentTypes) {
        this.contentTypes = contentTypes;
    }

    public Integer getMaxKBytes() {
        return maxKBytes;
    }

    public void setMaxKBytes(Integer maxKBytes) {
        this.maxKBytes = maxKBytes;
    }

    public String getExtRegExp() {
        return extRegExp;
    }

    public void setExtRegExp(String extRegExp) {
        this.extRegExp = extRegExp;
    }

    public String getContentTypeRegExp() {
        return contentTypeRegExp;
    }

    public void setContentTypeRegExp(String contentTypeRegExp) {
        this.contentTypeRegExp = contentTypeRegExp;
    }
}
