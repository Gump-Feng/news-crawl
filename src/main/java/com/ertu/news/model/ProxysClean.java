package com.ertu.news.model;

import java.math.BigDecimal;
import java.util.Date;

public class ProxysClean {
    private Integer id;

    private String ip;

    private Integer port;

    private Integer types;

    private Integer protocol;

    @Override
    public String toString() {
        return "ProxysClean{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", types=" + types +
                ", protocol=" + protocol +
                ", country='" + country + '\'' +
                ", area='" + area + '\'' +
                ", updatetime=" + updatetime +
                ", speed=" + speed +
                ", score=" + score +
                ", websiteName='" + websiteName + '\'' +
                ", matchDegree=" + matchDegree +
                '}';
    }

    private String country;

    private String area;

    private Date updatetime;

    private BigDecimal speed;

    private Integer score;

    private String websiteName;

    private Integer matchDegree;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getTypes() {
        return types;
    }

    public void setTypes(Integer types) {
        this.types = types;
    }

    public Integer getProtocol() {
        return protocol;
    }

    public void setProtocol(Integer protocol) {
        this.protocol = protocol;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public BigDecimal getSpeed() {
        return speed;
    }

    public void setSpeed(BigDecimal speed) {
        this.speed = speed;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getWebsiteName() {
        return websiteName;
    }

    public void setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
    }

    public Integer getMatchDegree() {
        return matchDegree;
    }

    public void setMatchDegree(Integer matchDegree) {
        this.matchDegree = matchDegree;
    }
}