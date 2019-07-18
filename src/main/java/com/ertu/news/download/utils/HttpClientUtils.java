package com.ertu.news.download.utils;

import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.*;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * @author hxf
 * @date 2019/3/20 13:34
 */
public class HttpClientUtils {
    private static SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(false).setSoReuseAddress(true)
            .setSoTimeout(15000).setTcpNoDelay(true).build();

    /**
     * 跳过web验证
     *
     * @return CloseableHttpClient
     */
    public static CloseableHttpClient createSSLClientWithHandShakeProtocol() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory;
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1", "TLSv1.1", "TLSv1.2", "SSLv3"},
                    new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA256"},
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());

            return HttpClients.custom().setDefaultSocketConfig(socketConfig).setSSLSocketFactory(sslConnectionSocketFactory).setDefaultSocketConfig(socketConfig).build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        return HttpClients.createDefault();
    }

    public static CloseableHttpClient createHttpClient(CookieStore cookieStore) {
        return HttpClientBuilder.create().setDefaultSocketConfig(socketConfig).setDefaultCookieStore(cookieStore).build();
    }

    public static CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create().setDefaultSocketConfig(socketConfig).build();
    }

    public static CloseableHttpClient createSSLClientDefault() {
        try {
            // 设置BasicAuth
            CredentialsProvider provider = new BasicCredentialsProvider();
            // 信任所有
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            return HttpClients.custom().setSSLSocketFactory(sslsf).setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                protected URI createLocationURI(String location) {
                    try {
                        location = location.replace(" ", "%20");
                        int index = location.indexOf("?");
                        if (index != -1) {
                            String url = location.substring(0, index + 1);
                            String params = location.substring(index + 1);
                            StringBuilder sb = new StringBuilder(url);
                            String[] pairs = params.split("&");
                            if (pairs.length > 0) {
                                for (String pair : pairs) {
                                    String[] param = pair.split("=", 2);
                                    if (param.length == 2) {
                                        sb.append(param[0]).append("=").append(URLEncoder.encode(param[1], "utf-8"))
                                                .append("&");
                                    }
                                }
                                location = sb.toString();
                            } else if (params.length() > 0) {
                                String[] param = params.split("=", 2);
                                sb.append(param[0]).append("=").append(URLEncoder.encode(param[1], "utf-8"));
                                location = sb.toString();
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    try {
                        return super.createLocationURI(location);
                    } catch (org.apache.http.ProtocolException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }).setDefaultCredentialsProvider(provider).build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        return HttpClients.createDefault();
    }

    public static void main(String[] args) {

    }
}
