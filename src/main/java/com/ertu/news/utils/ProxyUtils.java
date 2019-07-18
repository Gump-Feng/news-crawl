package com.ertu.news.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.ertu.news.model.bean.ProxyBean;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hxf
 */
public class ProxyUtils {


	public static List<ProxyBean> getProxy() throws Exception {
		List<ProxyBean> proxys = new ArrayList<ProxyBean>();
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet httpRequest = new HttpGet("http://127.0.0.1:8000/?count=500");

		CloseableHttpResponse execute = client.execute(httpRequest);
		String body = EntityUtils.toString(execute.getEntity());

		JSONArray proxyArray = JSON.parseArray(body);

		try {
			for (int i = 0; i < proxyArray.size(); i++) {
				ProxyBean proxyBean = new ProxyBean();
				// 对获取到的代理进行判空处理
				if (proxyArray.get(i) != null) {
					JSONArray proxy = (JSONArray) proxyArray.get(i);
					if (proxy.size() != 3) {
						continue;
					}
					int count = (Integer) proxy.get(2);
					if (count < 10) {
						continue;
					}
					String ip = (String) proxy.get(0);
					int port = (Integer) proxy.get(1);

					proxyBean.setIp(ip);
					proxyBean.setPort(port);
					proxyBean.setCount(count);
					proxys.add(proxyBean);

				} else {
					continue;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("获取代理数据：" + proxys.size());
		return proxys;
	}

	public static List<ProxyBean> getForeignProxy() throws Exception {
		List<ProxyBean> proxys = new ArrayList<ProxyBean>();
		HttpGet httpRequest = new HttpGet("http://localhost:8001/");
		CloseableHttpClient client = HttpClientBuilder.create().build();

		CloseableHttpResponse execute = client.execute(httpRequest);
		String body = EntityUtils.toString(execute.getEntity());

		JSONArray proxyArray = JSON.parseArray(body);

		try {
			for (int i = 0; i < proxyArray.size(); i++) {
				ProxyBean proxyBean = new ProxyBean();
				// 对获取到的代理进行判空处理
				if (proxyArray.get(i) != null) {
					JSONArray proxy = (JSONArray) proxyArray.get(i);
					if (proxy.size() != 3) {
						continue;
					}
					int count = (Integer) proxy.get(2);
					if (count < 9) {
						continue;
					}
					String ip = (String) proxy.get(0);
					int port = (Integer) proxy.get(1);

					proxyBean.setIp(ip);
					proxyBean.setPort(port);
					proxyBean.setCount(count);
					proxys.add(proxyBean);

				} else {
					continue;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("获取代理数据：" + proxys.size());
		return proxys;
	}

	public static List<ProxyBean> getPayProxy() throws ClientProtocolException, IOException {
		List<ProxyBean> beans = new ArrayList<ProxyBean>();
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet httpRequest = new HttpGet(
				"http://api.ip.data5u.com/dynamic/get.html?order=d6c1bbee7f170ac28ec59a2c33f30b7b&sep=5");

		CloseableHttpResponse execute = client.execute(httpRequest);
		String body = EntityUtils.toString(execute.getEntity());
		String ipPort = body.substring(0,body.length()-1);
		String[] proxyArray = ipPort.split(":");
		ProxyBean proxyBean = new ProxyBean();
		proxyBean.setIp(proxyArray[0]);
		proxyBean.setPort(Integer.parseInt(proxyArray[1]));
		beans.add(proxyBean);
		return beans;
	}

}
