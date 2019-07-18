package com.ertu.news.model;

/**
 * @author hxf
 * @date 2019/7/5 10:02
 */
public enum FieldEnum {
    /**
     * 对应资讯news_info表中的主要字段
     */
    ID("id"), PUBLISH_DATE("publish_date"), PUBLISH_DATE_STR("publish_date_str"), AUTHOR("author")
    , DESCRIPTION("news_abstract"), CONTENT("content"), TITLE("title"), URL_SRC("url_src"), URL_MD5("url_md5")
    , SITE_NAME("site_name"), SITE_ID("site_id"), CATEGORY("category"), SITE_DESCRIPTION("site_description");

    private String field;

    FieldEnum(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
