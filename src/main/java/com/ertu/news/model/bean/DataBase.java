package com.ertu.news.model.bean;

/**
 * 数据库的实体类
 */
public class DataBase {
    private String serverName;
    private String baseName;
    private String password;
    private String baseUrl;
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }


    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
