package com.ertu.news.utils;

import com.ertu.news.model.bean.ProxyBean;
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;

import java.util.List;
import java.util.Random;

public class HttpProxyClient {
	
	public static RequestConfig getProxyConfig() {
		RequestConfig requestConfig = null;
		List<ProxyBean> proxyList = null;
		try {
			proxyList = ProxyUtils.getProxy();
			while (proxyList.size() <= 1) {
				proxyList = ProxyUtils.getProxy();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		int random = new Random().nextInt(proxyList.size()-1);
		ProxyBean proxyBean = proxyList.get(random);
		String ip = proxyBean.getIp();
		int port = proxyBean.getPort();
		
		HttpHost proxy = new HttpHost(ip, port);
		System.out.println("当前代理的IP："+ip+"//当前代理的端口为："+port+"//当前代理的有效标记为："+proxyBean.getCount());
		requestConfig = RequestConfig.custom().setProxy(proxy).setCookieSpec(CookieSpecs.STANDARD).setConnectTimeout(20000)
				.setConnectionRequestTimeout(20000).setSocketTimeout(20000).build();
		
		return requestConfig;
		
	}
	public static RequestConfig getForeignProxyConfig() {
		RequestConfig requestConfig = null;
		List<ProxyBean> proxyList = null;
		try {
			proxyList = ProxyUtils.getForeignProxy();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		int random = new Random().nextInt(proxyList.size()-1);
		ProxyBean proxyBean = proxyList.get(random);
		String ip = proxyBean.getIp();
		int port = proxyBean.getPort();
		
		HttpHost proxy = new HttpHost(ip, port);
		System.out.println("当前代理的IP："+ip+"//当前代理的端口为："+port+"//当前代理的有效标记为："+proxyBean.getCount());
		requestConfig = RequestConfig.custom().setProxy(proxy).setCookieSpec(CookieSpecs.STANDARD).setConnectTimeout(20000)
				.setConnectionRequestTimeout(20000).setSocketTimeout(20000).build();
		
		return requestConfig;
		
	}
	
	public static RequestConfig getPayProxyConfig() {
		RequestConfig requestConfig = null;
		List<ProxyBean> proxyList = null;
		try {
			proxyList = ProxyUtils.getPayProxy();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		ProxyBean proxyBean = proxyList.get(0);
		String ip = proxyBean.getIp();
		int port = proxyBean.getPort();
		
		HttpHost proxy = new HttpHost(ip, port);
		System.out.println("当前代理的IP："+ip+"//当前代理的端口为："+port+"//当前代理的有效标记为："+proxyBean.getCount());
		requestConfig = RequestConfig.custom().setProxy(proxy).setCookieSpec(CookieSpecs.STANDARD).setConnectTimeout(20000)
				.setConnectionRequestTimeout(20000).setSocketTimeout(20000).build();
		
		return requestConfig;
		
	}
}
