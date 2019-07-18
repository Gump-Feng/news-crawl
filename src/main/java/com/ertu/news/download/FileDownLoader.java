package com.ertu.news.download;

import com.lwlh.download.utils.GetHeaders;
import com.lwlh.download.utils.HttpClientUtils;
import com.lwlh.model.ProxysClean;
import com.lwlh.model.bean.ProxyBean;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import us.codecraft.webmagic.utils.UrlUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hxf
 * @date 2019/3/20 13:30
 * <p>
 * 下载html中包含的各种文件
 */
public class FileDownLoader {
    private static Logger logger = Logger.getLogger(FileDownLoader.class);

    /**
     * 不使用代理的请求
     *
     * @param url  请求连接
     * @param host 代理
     * @return 返回类型为输出字节流
     */
    public static Map<String, Object> download(String url, HttpHost host) {
        url = UrlUtils.encodeIllegalCharacterInUrl(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000).setSocketTimeout(10000).build();
        //第一种请求情况：信任所有证书，但是不加握手协议
        HttpGet httpGet = new HttpGet(url);
        Header[] header = GetHeaders.getHeader();
        httpGet.setConfig(requestConfig);
        Map<String, Object> responseMap;
        CloseableHttpClient sslClient = HttpClientUtils.createSSLClientDefault();
        responseMap = request(httpGet, sslClient);
        if (responseMap != null && !responseMap.isEmpty()) {
            return responseMap;
        } else {
            //第二种请求情况：信任所有证书，加握手协议
            CloseableHttpClient withHandShakeProtocolClient = HttpClientUtils.createSSLClientWithHandShakeProtocol();
            responseMap = request(httpGet, withHandShakeProtocolClient);
            if (responseMap != null && !responseMap.isEmpty()) {
                return responseMap;
            } else {
                //第三种请求情况，最普通的client
                CloseableHttpClient httpClient = HttpClientUtils.createHttpClient();
                responseMap = request(httpGet, httpClient);
                return responseMap;
            }
        }
    }

    /**
     * 根据链接、域名和代理进行下载
     */
    public static Map<String, Object> download(String url, List<ProxysClean> proxysList) {
        Map<String, Object> responseMap = new HashMap<>(5);
        CloseableHttpClient sslClient = HttpClientUtils.createSSLClientDefault();
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000).setSocketTimeout(10000).build();

        int rightStatus = 200;
        int reTryTime = 3;
        for (int i = 0; i < reTryTime; i++) {
            HttpGet httpGet = new HttpGet(url);
            Header[] headers = GetHeaders.getHeader();
            httpGet.setHeaders(headers);
            httpGet.setConfig(requestConfig);
            try {
                CloseableHttpResponse execute = sslClient.execute(httpGet);
                StatusLine statusLine = execute.getStatusLine();
                if (statusLine.getStatusCode() == rightStatus) {
                    return saveResult2Map(responseMap, execute);
                }
            } catch (Exception e) {
                logger.error(httpGet.toString() +
                        "\n出错信息为：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Map<String, Object> saveResult2Map(Map<String, Object> responseMap, CloseableHttpResponse execute) throws IOException {
        HttpEntity entity = execute.getEntity();
        String fileType = entity.getContentType().getValue();
        byte[] bytes = EntityUtils.toByteArray(entity);
        responseMap.put("content", bytes);
        responseMap.put("type", fileType);
        return responseMap;
    }

    /**
     * 直接传入client进行请求
     *
     * @param url        请求连接
     * @param httpClient HttpClient实体
     * @return 返回字节数组
     */
    public static Map<String, Object> download(String url, CloseableHttpClient httpClient) {
        HttpGet httpGet = new HttpGet(url);

        return request(httpGet, httpClient);
    }

    private static Map<String, Object> request(HttpGet httpGet, CloseableHttpClient httpClient) {
        Header[] headers = GetHeaders.getHeader();
        return requestHelper(httpGet, httpClient, headers);
    }

    private static Map<String, Object> requestHelper(HttpGet httpGet, CloseableHttpClient httpClient, Header[] headers) {
        Map<String, Object> responseMap = new HashMap<>(16);
        httpGet.setHeaders(headers);
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
        try {
            CloseableHttpResponse execute = httpClient.execute(httpGet);
//            if (execute.getStatusLine().)
            saveResult2Map(responseMap, execute);
        } catch (Exception e) {
            logger.error(httpGet.toString() + "\n出错信息为：" + e.getMessage());
            e.printStackTrace();
            responseMap = null;
        }
        return responseMap;
    }

    public static Map<String, Object> download(String url, String domain, CloseableHttpClient httpClient) {
        Header[] headers = GetHeaders.getHeader(domain);
        HttpGet httpGet = new HttpGet(url);
        return requestHelper(httpGet, httpClient, headers);
    }

    /**
     * 根据链接、代理ip和port、域名进行下载页面
     */
    public static Map<String, Object> download(String url, ProxyBean proxyBean, String domain) {
        CloseableHttpClient sslClientDefault = HttpClientUtils.createSSLClientDefault();
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(20000)
                .setConnectionRequestTimeout(20000).setSocketTimeout(20000).build();
        Header[] headers = GetHeaders.getHeader();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeaders(headers);
        httpGet.setConfig(requestConfig);
        return request(httpGet, sslClientDefault);
    }

    /**
     * 根据链接、域名下载数据到指定文件
     */
    public static Map<String, Object> download(String url, String domain) {
        Map<String, Object> responseMap = new HashMap<>(16);
        CloseableHttpClient sslClient = HttpClientUtils.createSSLClientDefault();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig config = RequestConfig.custom().build();
        httpGet.setConfig(config);
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
        try {
            CloseableHttpResponse execute = sslClient.execute(httpGet);
            return saveResult2Map(responseMap, execute);
        } catch (Exception e) {
            logger.error("出错链接为：" + url +
                    "\n出错信息为：" + e.getMessage() + "\n域名为：" + domain);
        }
        return null;
    }
}
