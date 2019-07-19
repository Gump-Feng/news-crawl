package com.ertu.news.download;

import com.ertu.news.download.utils.GetHeaders;
import com.ertu.news.download.utils.HttpClientUtils;
import com.ertu.news.model.bean.ProxyBean;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author hxf
 * @date 2019/3/20 13:30
 */
public class DownLoader {

    private static Logger logger = LoggerFactory.getLogger(DownLoader.class);
    /**
     * 不使用代理的请求
     *
     * @param url 请求连接
     * @return 返回类型为输出字节流
     */
    public static byte[] download(String url) {
        CloseableHttpClient sslClient = HttpClientUtils.createSSLClientDefault();
        HttpGet httpGet = new HttpGet(url);
        return request(httpGet, sslClient);
    }

    /**
     * 根据链接、域名和代理进行下载
     */
    public static byte[] download(String url, HttpHost host) {
        CloseableHttpClient sslClient = HttpClientUtils.createSSLClientDefault();
        int rightStatus = 200;
        int reTryTime = 3;
        byte[] bytes = null;
        for (int i = 0; i < reTryTime; i++) {
            logger.debug("当前访问连接为："+url);
            HttpGet httpGet = new HttpGet(url);
            String domainByUrl = GetHeaders.getDomainByUrl(url);
            Header[] headers = GetHeaders.getHeader(domainByUrl);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000)
                    .setConnectionRequestTimeout(10000).setSocketTimeout(10000).build();
            httpGet.setHeaders(headers);
            httpGet.setConfig(requestConfig);

            try {
                CloseableHttpResponse execute = sslClient.execute(httpGet);
                bytes = EntityUtils.toByteArray(execute.getEntity());
                StatusLine statusLine = execute.getStatusLine();
                if (statusLine.getStatusCode() == rightStatus) {
                    return bytes;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * 直接传入client进行请求
     *
     * @param url        请求连接
     * @param httpClient HttpClient实体
     * @return 返回字节数组
     */
    public static byte[] download(String url, CloseableHttpClient httpClient) {
        HttpGet httpGet = new HttpGet(url);
        return request(httpGet, httpClient);
    }

    private static byte[] request(HttpGet httpGet, CloseableHttpClient httpClient) {
        Header[] headers = GetHeaders.getHeader();
        return requestHelper(httpGet, httpClient, headers);
    }

    private static byte[] requestHelper(HttpGet httpGet, CloseableHttpClient httpClient, Header[] headers) {
        httpGet.setHeaders(headers);
        byte[] entityContent = null;
        try {
            CloseableHttpResponse execute = httpClient.execute(httpGet);

            HttpEntity entity = execute.getEntity();
            entityContent = EntityUtils.toByteArray(entity);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return entityContent;
    }

    public static byte[] download(String url, String domain, CloseableHttpClient httpClient) {
        Header[] headers = GetHeaders.getHeader(domain);
        HttpGet httpGet = new HttpGet(url);
        return requestHelper(httpGet, httpClient, headers);
    }

    /**
     * 根据链接、代理ip和port、域名进行下载页面
     */
    public static byte[] download(String url, ProxyBean proxyBean, String domain) {
        CloseableHttpClient sslClientDefault = HttpClientUtils.createSSLClientDefault();
        HttpHost host = new HttpHost(proxyBean.getIp(), proxyBean.getPort());
        RequestConfig requestConfig = RequestConfig.custom().setProxy(host).setConnectTimeout(20000)
                .setConnectionRequestTimeout(20000).setSocketTimeout(20000).build();
        Header[] headers = GetHeaders.getHeader(domain);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeaders(headers);
        httpGet.setConfig(requestConfig);
        return request(httpGet, sslClientDefault);
    }

    /**
     * 根据链接、域名下载数据到指定文件
     */
    public static byte[] download(String url, String domain) {
        CloseableHttpClient sslClient = HttpClientUtils.createSSLClientDefault();
        HttpGet httpGet = new HttpGet(url);
        Header[] headers = GetHeaders.getHeader(domain);
        httpGet.setHeaders(headers);
        try {
            CloseableHttpResponse execute = sslClient.execute(httpGet);
            HttpEntity entity = execute.getEntity();
            return EntityUtils.toByteArray(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
