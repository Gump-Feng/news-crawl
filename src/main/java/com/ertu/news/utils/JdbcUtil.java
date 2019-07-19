package com.ertu.news.utils;

import com.ertu.news.model.FieldEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.sql.*;
import java.util.Map;

/**
 * @author hxf
 * @date 2019/7/5 10:46
 */
public class JdbcUtil {
    private static Logger logger = LoggerFactory.getLogger(JdbcUtil.class);

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // 建立连接
            conn = DriverManager
                    .getConnection(
                            "jdbc:mysql://localhost:3306/news_test?useUnicode=true&characterEncoding=UTF-8",
                            "root", "root");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }


    public static int insertNews2Db(Map<String, String> infoMap, Connection connection) {
        String siteId = infoMap.get(FieldEnum.SITE_ID.getField());
        MDC.put("site_id", siteId);
        connection = checkAndUpdateConn(connection);
        PreparedStatement ps;
        int id = 1;
        String insertSql = "INSERT INTO news_info SET title=?,author=?,news_abstract=?,content=?,publish_date=?,publish_date_str=?," +
                "site_name=?,site_description=?,site_id=?,url_src=?,url_md5=?";
        try {
            ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, infoMap.get(FieldEnum.TITLE.getField()));
            ps.setString(2, infoMap.get(FieldEnum.AUTHOR.getField()));
            ps.setString(3, infoMap.get(FieldEnum.DESCRIPTION.getField()));
            ps.setString(4, infoMap.get(FieldEnum.CONTENT.getField()));
            ps.setString(5, infoMap.get(FieldEnum.PUBLISH_DATE.getField()));
            ps.setString(6, infoMap.get(FieldEnum.PUBLISH_DATE_STR.getField()));
            ps.setString(7, infoMap.get(FieldEnum.SITE_NAME.getField()));
            ps.setString(8, infoMap.get(FieldEnum.DESCRIPTION.getField()));
            ps.setInt(9, Integer.parseInt(siteId));
            ps.setString(10, infoMap.get(FieldEnum.URL_SRC.getField()));
            ps.setString(11, infoMap.get(FieldEnum.URL_MD5.getField()));
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();

            while (generatedKeys.next()) {
                id = generatedKeys.getInt(1);
            }
            if (id > 0) {
                logger.info("资讯链接插入成功，id为：" + id + "/" + infoMap.get(FieldEnum.URL_SRC.getField()));
            }else {
                logger.error("资讯链接插入失败！！，id为：" + id + "/" + infoMap.get(FieldEnum.URL_SRC.getField()));
            }
        } catch (SQLException e) {
            logger.error("资讯链接插入失败！！，异常信息为："+ e.getMessage());
            e.printStackTrace();
        }
        return id;
    }

    private static Connection checkAndUpdateConn(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                if (connection != null) {
                    connection.close();
                }
                connection = getConnection();
                return connection;
            } else {
                return connection;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void updateContent(Map<String, String> infoMap, Connection connection, int id, String preUrl) {



    }
}
