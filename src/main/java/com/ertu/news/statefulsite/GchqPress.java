package com.ertu.news.statefulsite;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ertu.news.download.utils.HttpClientUtils;
import com.ertu.news.io.sql.JdbcOperate;
import com.ertu.news.model.FieldEnum;
import com.ertu.news.utils.JdbcUtil;
import com.ertu.news.utils.TimeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hxf
 * @date 2019/7/4 17:31
 */
public class GchqPress implements StatefulSite {
    public static void main(String[] args) {
        GchqPress gchqPress = new GchqPress();
        gchqPress.crawl();
    }

    private CloseableHttpClient sslClientDefault;
    private Connection connection = JdbcUtil.getConnection();

    @Override
    public void crawl() {
        sslClientDefault = HttpClientUtils.createSSLClientDefault();
        Map<String, String> infoMap = new HashMap<>(32);
        infoMap.put(FieldEnum.SITE_NAME.getField(), "GCHQ");
        infoMap.put(FieldEnum.SITE_ID.getField(), "10063000");
        infoMap.put(FieldEnum.CATEGORY.getField(), "category");
        infoMap.put(FieldEnum.SITE_DESCRIPTION.getField(), "英国情报机构政府通信总部");
        getListJson(infoMap);


    }

    private void getListJson(Map<String, String> infoMap) {
        String newsListUrl = "https://www.gchq.gov.uk/api/v1/search/query.json?q=10&defaultTypes=news&sort=date%2Bdesc";
        HttpGet httpGet = new HttpGet(newsListUrl);
        try {
            CloseableHttpResponse execute = sslClientDefault.execute(httpGet);
            HttpEntity entity = execute.getEntity();
            String body = EntityUtils.toString(entity);
            analysisJson(infoMap, body);
            //资讯入库，不带图片的转换路径
            int id = JdbcUtil.insertNews2Db(infoMap, connection);

            String preUrl = "https://www.gchq.gov.uk";
            String content = infoMap.get(FieldEnum.CONTENT.getField());
            content = formatContent(content, preUrl);



            JdbcUtil.updateContent(infoMap, connection, id, preUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatContent(String content, String preUrl) {
        Document document = Jsoup.parse(content);
        Elements imgElems = document.getElementsByTag("img");
        if (imgElems != null && !imgElems.isEmpty()){
            for (Object imgEleObj : imgElems) {
                if (imgEleObj instanceof Element){
                    Element imgEle = (Element)imgEleObj;
                    String imgSrc = imgEle.attr("src");
                    imgSrc = preUrl + imgSrc;

                }
            }
        }
        return content;
    }

    private void analysisJson(Map<String, String> infoMap, String body) {
        JSONObject jsonObject = (JSONObject) JSONObject.parse(body);
        JSONArray jsonArray = jsonObject.getJSONArray("documents");
        String preUrl = "https://www.gchq.gov.uk/api/1/services/v1/article-content.json?url=";
        if (jsonArray != null && !jsonArray.isEmpty()) {
            JSONObject newsJson = (JSONObject) jsonArray.get(0);
            //添加出版时间
            addPubDate(newsJson, infoMap);
            //添加标题
            addTitleAndDescription(newsJson, infoMap);
            //添加详情页连接
            addContentAndFile(infoMap, newsJson, preUrl);
        }
    }

    private void addContentAndFile(Map<String, String> infoMap, JSONObject newsJson, String preUrl) {
        String pageUrl = newsJson.getString("pageUrl");
        if (pageUrl != null && !pageUrl.isEmpty()) {
            pageUrl = preUrl + pageUrl;
            String stringToMd5 = JdbcOperate.stringToMd5(pageUrl);
            infoMap.put(FieldEnum.URL_MD5.getField(), stringToMd5);
            infoMap.put(FieldEnum.URL_SRC.getField(), pageUrl);
        }
        HttpGet contentGet = new HttpGet(pageUrl);
        try {
            CloseableHttpResponse execute = sslClientDefault.execute(contentGet);
            String contentBody = EntityUtils.toString(execute.getEntity());
            JSONObject contentJson = (JSONObject) JSONObject.parse(contentBody);
            if (contentJson != null) {
                JSONObject pageJson = (JSONObject) contentJson.get("page");
                JSONObject content = (JSONObject) pageJson.get("content");
                JSONArray items = content.getJSONArray("items");
                if (items != null && !items.isEmpty()) {
                    JSONObject o = (JSONObject) items.get(0);
                    String contentStr = o.getString("description");
                    infoMap.put(FieldEnum.CONTENT.getField(), contentStr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTitleAndDescription(JSONObject newsJson, Map<String, String> infoMap) {
        String title = newsJson.getString("title");
        infoMap.put(FieldEnum.TITLE.getField(), title);
        String description = newsJson.getString("description");
        infoMap.put(FieldEnum.DESCRIPTION.getField(), description);
    }

    private void addPubDate(JSONObject newsJson, Map<String, String> infoMap) {
        String originalDate = newsJson.getString("date");
        infoMap.put(FieldEnum.PUBLISH_DATE_STR.getField(), originalDate);
        Date date = TimeUtils.formatTime(originalDate, "", "date_dMy");
        String publishDate = TimeUtils.dateToString(date, "yyyy-MM-dd");
        infoMap.put(FieldEnum.PUBLISH_DATE.getField(), publishDate);
    }
}
