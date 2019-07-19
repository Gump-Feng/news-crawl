package com.ertu.news.download.utils.proxy;

import com.ertu.news.model.bean.ProxyBean;
import org.apache.http.HttpHost;

/**
 * @author hxf
 * @date 2019/3/21 9:31
 */
public class LocalProxy extends BaseProxy {
    @Override
    public ProxyBean getProxy() {
        return super.getProxy();
    }


    public static HttpHost getHttpHost() {
        return new HttpHost("127.0.0.1", 1080);
    }
}
