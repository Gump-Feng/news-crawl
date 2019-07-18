package com.ertu.news.processor;


import com.ertu.news.model.bean.Site;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author hxf
 * @date 2019/5/9 16:33
 * <p>
 * 管理任务队列的工具类
 */
class TaskManager {

    private static ConcurrentLinkedQueue<Site> jobsQueue = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<Site> detailPageTaskQueue = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<Site> listPageTaskQueue = new ConcurrentLinkedQueue<>();

    static Site getDetailTask() {
        return detailPageTaskQueue.poll();
    }

    static Site getListPageTask() {
        return listPageTaskQueue.poll();
    }

    static void addDetailPageTask(Site site){
        detailPageTaskQueue.add(site);
    }

    static void addListPageTask(Site site){
        listPageTaskQueue.add(site);
    }

    static Site getJobTask() {
        return jobsQueue.poll();
    }

    static void addJobTask(Site site) {
        jobsQueue.add(site);
    }
}
