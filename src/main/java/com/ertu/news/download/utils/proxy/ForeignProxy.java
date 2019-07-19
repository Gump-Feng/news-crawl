package com.ertu.news.download.utils.proxy;


import com.ertu.news.model.bean.ProxyBean;

/**
 * @author hxf
 * @date 2019/3/21 9:25
 * 国外代理的获取，利用小飞机进行翻墙
 *
 */
public class ForeignProxy extends BaseProxy {
    @Override
    public ProxyBean getProxy() {
        ProxyBean proxyBean = new ProxyBean();
        proxyBean.setIp("127.0.0.1");
        proxyBean.setPort(1080);
        return proxyBean;
    }
}
