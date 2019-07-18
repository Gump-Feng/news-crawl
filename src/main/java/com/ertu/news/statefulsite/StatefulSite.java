package com.ertu.news.statefulsite;

/**
 * @author hxf
 * @date 2019/7/4 17:12
 *
 * 动态抓取网站的接口
 */
public interface StatefulSite {

    /**
     * 抓取特定网站信息的方法
     */
    void crawl();
}
