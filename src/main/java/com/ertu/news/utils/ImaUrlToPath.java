package com.ertu.news.utils;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;

public class ImaUrlToPath {
	private static CloseableHttpClient client = HttpClients.createDefault();
	private static RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000)
			.setConnectionRequestTimeout(10000).setSocketTimeout(10000)
			.build();

	@SuppressWarnings("unused")
	private static void action(String imgUrl, String path) throws Exception {
		HttpGet httpGet = new HttpGet(imgUrl);
		{
			httpGet.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			httpGet.setHeader("Accept-Encoding", "gzip, deflate, br");
			httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
			httpGet.setHeader("Connection", "keep-alive");
			httpGet.setHeader("Host", "images-cn.ssl-images-amazon.com");
			httpGet.setHeader("Upgrade-Insecure-Requests", "1");
			httpGet.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36");

			httpGet.setConfig(requestConfig);
		}

		HttpResponse httpResponse = client.execute(httpGet);
		HttpEntity entity = httpResponse.getEntity();

		byte[] byteArray = EntityUtils.toByteArray(entity);
		String content = new String(byteArray);
		if (content.contains("html") || content.contains("body")) {
			throw new RuntimeException("下载图片失败");
		}
		File file = new File(path);
		FileUtils.writeStringToFile(file, content);

	}

}
