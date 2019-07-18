package com.ertu.news.statefulsite;

/**
 * @author hxf
 * @date 2019/7/4 17:12
 */
public class NasaNews implements StatefulSite {

    @Override
    public void crawl() {
        System.out.println("NASA的动态抓取类");
    }
}
