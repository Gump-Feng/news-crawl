package com.ertu.news.processor;

/**
 * @author hxf
 * @date 2019/4/29 10:35
 *
 * 系统抓取的统一接口
 */
public interface SpiderProcessor {
    /**
     * 开启线程的抓取方法
     */
    void spider();
}
