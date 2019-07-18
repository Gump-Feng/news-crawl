package com.ertu.news.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hxf
 * @date 2019/3/27 16:14
 */
public class StringUtils {
    /**
     *
     */
    private static String[] windowsCharArray = {"/", ":", "*", "?", "<", ">", "|", "。", ","};
    private static String[] linuxCharArray = {"@", "￥", "#", "&", "-", "/"};


    public static void main(String[] args) {
        String data = "https://translate.google.cn/#view=home&op=translate&sl=zh-CN&tl=en&text=%E5%88%86%E7%B1%BB";
        String regex = "=.";
        matchStrByReg(data, regex, "");
    }

    public static List<String> matchStrByReg(String data, String regex, String preUrl) {
        Html html = new Html(data);
        List<String> matchesList = new ArrayList<>();
        Selectable links = html.xpath("//a/@href");
        if (links != null && links.all() != null && links.all().size() != 0) {
            List<String> linkXpath = links.all();
            for (String link : linkXpath
            ) {
                String queueUrl;
                if (!link.contains("http")) {
                    //获取配置文件中对应的链接前缀，默认为空
                    if (link.startsWith("../")){
                        link = link.replaceAll("(?s)(\\.\\./)+","/");
                    }
                    queueUrl = preUrl + link;
                } else {
                    queueUrl = link;
                }
                Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(queueUrl);
                if (matcher.find()) {
                    String group = matcher.group();
                    if (!matchesList.contains(group)) {
                        matchesList.add(group);
                    }
                }
            }
        }
        return matchesList;
    }

    public static String matchStrByReg(String data, String regex) {
        String result = "";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            result = matcher.group();
        }
        return result;
    }

    public static String matchStrByReg(String data, Element regexEle) {
        String result = "";
        String regex = ".+";
        int group = 0;
        if (regexEle != null){
            regex = regexEle.getText();
            Attribute groupAttr = regexEle.attribute("group");
            if (groupAttr != null){
                group = Integer.parseInt(groupAttr.getValue());
            }
        }
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            result = matcher.group(group);
        }
        return result;
    }


    public static String titleFilter(String title) {
        String regEx = "[`~!@#$%^&*()\\-+={}':;,\\[\\].<>/?￥%…（）_+|【】|‘；：\\.”“’。\"，、？\\s]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(title);
        String titleFormat = m.replaceAll("_").trim();
        if (titleFormat.contains("..")){
            titleFormat = titleFormat.replace("..","");
        }
        return titleFormat;
    }
    /**
     * 去除标题中的特殊字符，规范标题
     *
     * @param title 原标题名
     * @return 标准标题
     * windows特殊字符：\ / : * ? " < > | 。
     * Linux特殊字符：@ # ￥ & - 空格 /
     */
    public static String formatTitle(String title, String os) {
        if ("windows".equals(os)) {
            for (String charStr : windowsCharArray
                 ) {
                if (title.contains(charStr)){
                    title = title.replace(charStr,"_");
                }

            }
        }
        if ("linux".equals(os)) {
            for (String charStr : linuxCharArray
                 ) {
                if (title.contains(charStr)){
                    title = title.replace(charStr,"_");
                }

            }
        }
        return title;
    }

    public static String getUrlByHtmlTag(String htmlTag) {
        Document htmlTagDocument = Jsoup.parse(htmlTag);
        Elements urlEle = htmlTagDocument.getElementsByTag("img");
        if(urlEle != null){
            return urlEle.attr("src");
        }
        return "";
    }

    public static String getContentByJsonStr(String result) {
        String content = "";
        JSONObject jsonObj = (JSONObject)JSONObject.parse(result);
        JSONObject transResult = (JSONObject)jsonObj.get("trans_result");
        JSONArray data = (JSONArray)transResult.get("data");
        JSONObject dataJSONObj = (JSONObject) data.get(0);
        JSONArray dataJSONArray = (JSONArray) dataJSONObj.get("result");
        if (!dataJSONArray.isEmpty()){
            content = ((JSONArray)dataJSONArray.get(0)).get(1).toString();
        }
        return content;
    }
}
