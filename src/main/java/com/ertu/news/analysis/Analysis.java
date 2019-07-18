package com.ertu.news.analysis;

import com.ertu.news.model.bean.Site;

import java.util.HashMap;

/**
 * @author hxf
 * @date 2019/6/25 15:55
 *
 * 页面解析的接口
 */
public interface Analysis {
    /**
     * 解析采集过程中返回的页面，不同的页面解析方法不同
     * @param site  网站栏目对应的实体类
     * @param siteUrl 栏目的入口链接
     */
    void analysis(Site site, String siteUrl);

    /**
     * 对于正常Html的页面进行解析
     * @param site 网站栏目对应的实体类
     * @param body 栏目的入口链接
     * @param infoMap   解析返回的信息
     */
    void analysis(Site site, byte[] body, HashMap<String, String> infoMap);
}
