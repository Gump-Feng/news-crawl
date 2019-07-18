package com.ertu.news.processor;

import com.ertu.news.statefulsite.GchqPress;
import com.ertu.news.utils.ClazzUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author hxf
 * @date 2019/7/4 18:15
 */
public class StatefulProcessor implements SpiderProcessor {
    @Override
    public void spider() {
        ConcurrentLinkedQueue<String> clazzNameList = ClazzUtils.getClazzName();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("news_file-transport-thread-%d").build();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), namedThreadFactory);
        executorService.scheduleAtFixedRate(() -> {
            for (String className : clazzNameList) {
                try {
                    Class<?> aClass = Class.forName(className);
                    GchqPress gchqPress = (GchqPress) aClass.newInstance();
                    gchqPress.crawl();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

}
