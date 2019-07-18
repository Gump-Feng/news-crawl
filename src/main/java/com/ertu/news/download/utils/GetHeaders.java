package com.ertu.news.download.utils;


import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hxf
 * @date 2019/3/20 13:54
 */

public class GetHeaders {
    public static String projectRootPath = System.getProperty("user.dir");
//    static{
//        File userAgentFile = new File(projectRootPath +"/src/main/resources/static/windows_UA.txt");
//        try {
//            String fileToString = FileUtils.readFileToString(userAgentFile);
//            winUserAgentArray = fileToString.split("\n");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 设置httpclient的请求头，自主设置域名
     * @param hostName  域名
     * @return  请求头信息
     */
    public static Header[] getHeader(String hostName) {
        Header[] header = getHeader();
        Header hostHeader = new BasicHeader("Host", hostName);
        List<Header> headers = new ArrayList<>(Arrays.asList(header));
        headers.add(hostHeader);
        return headers.toArray(header);
    }

    /**
     * 设置httpclient的请求头，不设置域名
     * @return  请求头信息
     */
    public static Header[] getHeader() {
//        int random = new Random().nextInt(winUserAgentArray.length - 1);
//        String userAgent = winUserAgentArray[random];
//        logger.debug("选用的userAgent为:"+userAgent);
        Header acceptHeader = new BasicHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//        Header upgradeHeader = new BasicHeader("Upgrade-Insecure-Requests", "1");
        Header connHeader = new BasicHeader("Connection", "keep-alive");
        Header userAgentHeader = new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36");
//        Header cookieHeader = new BasicHeader("Cookie","eZSESSID=iq7oj9nlp6qat7m54hoet2luo2; nlbi_999863=wEwSG1BuSn7JfPwzysOOugAAAADIBGNJMcbZrM2z7i9bqzfx; visid_incap_999863=x4ej6R6yTNCwoVKVXjOfbnlrvlwAAAAAQUIPAAAAAACU5/laz1QFXBlRgVgIuNn8; incap_ses_165_999863=mDoBFVFSDCYazgcMHjRKAn1rvlwAAAAAW9xXPY4RGbCnqJTdbggAsQ==; xtvrn=$130831$");
//        Header acceptEncodeHeader = new BasicHeader("Accept-Encoding", "gzip, deflate, br");
//        Header acceptLangHeader = new BasicHeader("Accept-Language", "zh-CN,zh;q=0.9");
//        Header cacheHeader = new BasicHeader("Cache-Control", "max-age=0");
//        Header refererHeader = new BasicHeader("Referer", "https://fas.org/sgp/crs/");
//        return new Header[]{acceptHeader,userAgentHeader, acceptEncodeHeader, acceptLangHeader};
        return new Header[]{userAgentHeader, connHeader, acceptHeader};

    }

    public static String getDomainByUrl(String url) {
        String regex = "://[^/]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String domain = matcher.group(0);
            if (null == domain || "".equals(domain)) {
                return "";
            } else {
                return domain.substring(3);
            }
        }
        return "";
    }
}
