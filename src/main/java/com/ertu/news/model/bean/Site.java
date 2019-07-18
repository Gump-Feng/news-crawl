package com.ertu.news.model.bean;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author hxf
 * @date 2019/5/5 10:44
 *
 * 抓取目标网站，包含网站的全部信息
 */
public class Site {
    /**
     *   栏目的配置信息
     */
    private ConfigBean configBean;
    private boolean IsFinished;
    private Request request;
    private BlockingQueue<PageBean> detailUrlQueue;
    private BlockingQueue<String> listUrlQueue;
    private Set<String> duplicatedUrls;


    public BlockingQueue<PageBean> getDetailUrlQueue() {
        return detailUrlQueue;
    }

    public void setDetailUrlQueue(BlockingQueue<PageBean> detailUrlQueue) {
        this.detailUrlQueue = detailUrlQueue;
    }

    public BlockingQueue<String> getListUrlQueue() {
        return listUrlQueue;
    }

    public void setListUrlQueue(BlockingQueue<String> listUrlQueue) {
        this.listUrlQueue = listUrlQueue;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Set<String> getDuplicatedUrls() {
        return duplicatedUrls;
    }

    public void setDuplicatedUrls(Set<String> duplicatedUrls) {
        this.duplicatedUrls = duplicatedUrls;
    }

    public ConfigBean getConfigBean() {
        return configBean;
    }

    public void setConfigBean(ConfigBean configBean) {
        this.configBean = configBean;
    }

    public boolean isFinished() {
        return IsFinished;
    }

    public void setFinished(boolean finished) {
        IsFinished = finished;
    }
}
