package com.ertu.news.download.utils.proxy;

import com.ertu.news.model.bean.ProxyBean;
import com.ertu.news.utils.SerializeObjectTool;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;

/**
 * @author hxf
 * @date 2019/3/21 9:39
 */
public class ZDYProxy extends BaseProxy {
    public static JedisPool jedisPool = null;
    public static Jedis proxyJedis = null;
    public ZDYProxy(){
        initialPool();
    }

    {
        System.out.println(proxyJedis.dbSize());
        while(proxyJedis.dbSize()<160){
            CookieStore cookieStore = new BasicCookieStore();
            CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
            HttpGet httpRequest = new HttpGet(
                    "http://s.zdaye.com/?api=201902131647269240&count=5&px=2");

            httpRequest.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            httpRequest.setHeader("Upgrade-Insecure-Requests", "1");
            httpRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
            httpRequest.setHeader("Host", "s.zdaye.com");
            httpRequest.setHeader("Accept-Encoding", "gzip, deflate");
            httpRequest.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
            httpRequest.setHeader("Proxy-Connection", "keep-alive");

            CloseableHttpResponse execute;
            try {
                execute = client.execute(httpRequest);
                String body = EntityUtils.toString(execute.getEntity());
                System.out.println(body);
                String[] proxyArray = body.split("\n");
                System.out.println("获得代理数量为："+proxyArray.length);
                for (int i = 0; i < proxyArray.length; i++) {
                    String proxyStr = proxyArray[i];
                    String[] proxys = proxyStr.split(":");
                    ProxyBean proxyBean = new ProxyBean();
                    proxyBean.setIp(proxys[0]);
                    proxyBean.setPort(Integer.parseInt(proxys[1].trim()));
                    proxyJedis.set(("proxy"+proxys[0]+proxys[1]).getBytes(), SerializeObjectTool.serialize(proxyBean));
                    proxyJedis.expire(("proxy"+proxys[0]+proxys[1]).getBytes(), 360);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000*12);
                System.out.println("共有代理"+proxyJedis.dbSize());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ProxyBean getProxy() {

        return super.getProxy();
    }

    public static void getProxyJedis(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(20);
        config.setMaxIdle(5);
        config.setTestOnBorrow(false);

        jedisPool = new JedisPool(config, "127.0.0.1", 6379);
        proxyJedis = jedisPool.getResource();
        proxyJedis.select(0);
    }

    private void initialPool() {
        // 池基本配置
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(20);
        config.setMaxIdle(5);
        config.setTestOnBorrow(false);

        jedisPool = new JedisPool(config, "127.0.0.1", 6379);
        proxyJedis = jedisPool.getResource();
        proxyJedis.select(0);
        proxyJedis.flushDB();
    }
}
