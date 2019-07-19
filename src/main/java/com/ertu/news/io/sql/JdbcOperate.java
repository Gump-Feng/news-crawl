package com.ertu.news.io.sql;

import com.ertu.news.utils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author hxf
 */
public class JdbcOperate {
    private static Logger logger = LoggerFactory.getLogger(JdbcOperate.class);

    public static void main(String[] args) {

    }

    public static Map<String, String> selectNewsFileByUrlMd5(Map<String, Object> fileTableMap, String href) {
        Connection connection = getSqlConn(fileTableMap);
        String md5Url = stringToMd5Url(href);
        String sql = "SELECT * FROM `" + fileTableMap.get("dbName") + "`.`" + fileTableMap.get("tableName") + "` where url_md5=" + "'" + md5Url + "'";
        try {
            if (connection != null) {
                Map<String, String> fileByUrlMd5 = selectNewsFileByUrlMd5(sql, connection);
                connection.close();
                return fileByUrlMd5;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int insertData(Map<String, Object> dataBaseMap, Map<String, String> mesMappro, Connection conn) {

        if ("website".equals(dataBaseMap.get("tableName"))) {
            int siteId = Integer.parseInt(mesMappro.get("Site_Id"));
            String selectId = selectBySiteId(dataBaseMap, conn, siteId);
            if (selectId == null) {
                try {
                    insertSql(dataBaseMap, mesMappro, conn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Map<String, Object> websiteMap = selectWebsiteBySiteId(dataBaseMap, conn, mesMappro.get("site_id"));
                return websiteMap.size();
            }
        } else {
            /*获得数据库插入实例map*/
            Map<String, String> mesMap = sqlIndex(mesMappro, dataBaseMap);
            String md5Hash;
            if ("news_static_file".equals(dataBaseMap.get("tableName")) || "news_info".equals(dataBaseMap.get("tableName"))) {
                md5Hash = stringToMd5Url(mesMap);
                mesMap.put("url_md5", md5Hash + "");
            } else {
                md5Hash = stringToMd5TitleUrl(mesMap);
                mesMap.put("url_title_md5", md5Hash + "");
            }

            String repeat = selectIdByMd5(dataBaseMap, conn, md5Hash);
            if (repeat == null) {
                insertSql(dataBaseMap, mesMap, conn);
                String idString = selectIdByMd5(dataBaseMap, conn, md5Hash);
                logger.debug("id值为" + idString);
                try {
                    return Integer.parseInt(idString);
                } catch (Exception e) {
                    logger.error(mesMap.toString() + e);
                    e.printStackTrace();
                }
            } else {
                logger.info("该链接内容已存在，跳过：" + mesMappro.get("url_src"));
            }
        }
        return -1;
    }

    public static void updateContentAndTranslateSignById(String content, int newsId, Map<String, Object> dataBaseColumnNameMap, Connection sqlConn) {
        //UPDATE news_info SET translate_sign=0 AND content='' WHERE id =100001229
        String updateSql = String.format("update `%s` set content=?,translate_sign=? where id=?", dataBaseColumnNameMap.get("tableName"));
        PreparedStatement ps;
        try {
            ps = sqlConn.prepareStatement(updateSql);
            ps.setString(1, content);
            ps.setInt(2, 0);
            ps.setInt(3, newsId);
            int executeUpdate = ps.executeUpdate();
            logger.info("id为: " + newsId + "的正文content已更新" + "---" + executeUpdate);
        } catch (SQLException e) {
            logger.error("id为: " + newsId + "的正文content更新失败" + "---\n失败原因：" + e.getMessage() + "\n" + e.getSQLState() + "\ncontent为：" + content);
            e.printStackTrace();
        }
    }

    /**
     * 执行update方法，进行添加，修改等操作
     *
     * @param sqlString sql语句
     * @param con       mysql数据库的链接
     */
    private static void updateSql(String sqlString, Connection con) {
        PreparedStatement pst;
        try {
            logger.debug(sqlString);
            pst = con.prepareStatement(sqlString);
            pst.execute();
        } catch (SQLException | NumberFormatException e) {
            logger.error(e.getMessage());
        }
    }

    private static String stringToMd5TitleUrl(Map<String, String> mesMap) {
        String url = mesMap.get("url_src");
        String title = mesMap.get("title");
        String fieldMes = url + title;
        return stringToMd5(fieldMes);
    }


    private static String stringToMd5Url(Map<String, String> mesMap) {
        String fieldMes = mesMap.get("url_src");
        return stringToMd5(fieldMes);
    }

    public static String stringToMd5Url(String url) {
        return stringToMd5(url);
    }

    public static String stringToMd5(String fieldMes) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(fieldMes.getBytes(StandardCharsets.UTF_8));
            byte[] encryption = md5.digest();
            StringBuilder strBuf = new StringBuilder();
            for (byte b : encryption) {
                if (Integer.toHexString(0xff & b).length() == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff & b));
                } else {
                    strBuf.append(Integer.toHexString(0xff & b));
                }
            }
            return strBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private static Map<String, String> sqlIndex(Map<String, String> hashMap, Map<String, Object> dataBaseMap) {
        String category = String.valueOf(dataBaseMap);
        logger.debug(category);
        if (!"news_static_file".equals(dataBaseMap.get("tableName"))) {
            if (!category.contains("isHavePdfurl")) {
                String titleTranslate = hashMap.get("title_translate");
                String contentTranslate = hashMap.get("content_translate");
                if (titleTranslate != null && contentTranslate != null && titleTranslate.length() > 0 && contentTranslate.length() > 0) {
                    hashMap.put("translate_sign", "1");
                } else {
                    hashMap.put("translate_sign", "-1");
                }
            } else {
                String pdfUrl = hashMap.get("pdf_url");
                if (pdfUrl != null && pdfUrl.length() > 0) {
                    hashMap.put("isHavePdfurl", "1");
                } else {
                    hashMap.put("isHavePdfurl", "0");
                }
                String isTransAbstract = hashMap.get("abstract_cn");
                if (isTransAbstract != null && isTransAbstract.length() > 0) {
                    hashMap.put("is_trans_abstract", "1");
                } else {
                    hashMap.put("is_trans_abstract", "0");
                }
                String isTransTitle = hashMap.get("title_cn");
                if (isTransTitle != null && isTransTitle.length() > 0) {
                    hashMap.put("is_trans_title", "1");
                } else {
                    hashMap.put("is_trans_title", "0");
                }
                String isDownloadPdf = hashMap.get("pdf_path");
                if (isDownloadPdf != null && isDownloadPdf.length() > 0) {
                    hashMap.put("isDownloadPdf", "1");
                } else {
                    hashMap.put("isDownloadPdf", "0");
                }
            }
        }
        return hashMap;
//        logger.debug("数据库字段信息为："+hashMap);
    }


    private static void insertSql(Map<String, Object> dataBaseMap, Map<String, String> hashmap, Connection conn) {
        String database = (String) dataBaseMap.get("dbName");
        String table = (String) dataBaseMap.get("tableName");
        String sql1 = String.format("INSERT INTO `%s`.`%s` (", database, table);
        String sqlString;
        StringBuilder srtValue = new StringBuilder();
        for (int i = 0; i < hashmap.size(); i++) {
            srtValue.append(",?");
        }
        srtValue = new StringBuilder(srtValue.substring(1));
        logger.debug(srtValue.toString());
        StringBuilder str1 = new StringBuilder();
        for (Map.Entry<String, String> mapEntry : hashmap.entrySet()) {
            str1.append("`").append(mapEntry.getKey()).append("`,");
        }
        str1 = new StringBuilder(str1.substring(0, str1.length() - 1));
        sqlString = sql1 + str1 + ")VALUES(" + srtValue + ")";
        com.mysql.jdbc.PreparedStatement ps = null;
        try {
            ps = (com.mysql.jdbc.PreparedStatement) conn.prepareStatement(sqlString);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int i = 0;
        for (Map.Entry<String, String> mapEntry : hashmap.entrySet()) {
            i += 1;
            String attribute = mapEntry.getValue();
            try {
                assert ps != null;
                ps.setString(i, attribute);
            } catch (SQLException e) {
                logger.debug("字段" + mapEntry.getKey() + "为" + attribute);
                e.printStackTrace();
            }
        }
        try {
            assert ps != null;
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("unchecked")
    public static void createTable(Map<String, Object> dataBaseMap, Connection con) {
        Object tableName = dataBaseMap.get("tableName");
        Object dbName = dataBaseMap.get("dbName");
        String sql = "SELECT table_name FROM information_schema.TABLES WHERE table_name ='" + tableName + "' and table_schema='" + dbName + "' ;";
        logger.debug(sql);
        String li = selectSql(sql, con);
        if (li == null) {
            //定义SQL语句sqlString
            //如果是与内容无关的表，则不需要建立链接的索引
            String sqlString = "CREATE TABLE `" + dataBaseMap.get("dbName") + "`.`" + tableName + "`  (`id` int(10) NOT NULL AUTO_INCREMENT,";
            logger.debug(tableName.toString());
            StringBuilder keyStr = new StringBuilder();
            //遍历map，找出值为map的value,即表的字段名
            for (Map.Entry<String, Object> entry : dataBaseMap.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Map) {
//                二次遍历，value为字段名、类型、长度
                    Map<String, String> fieldMap = (Map<String, String>) value;
//                后段SQL拼接
                    if ("save_time".equals(fieldMap.get("name"))) {
                        keyStr.append("`").append(fieldMap.get("name")).append("` ").append(fieldMap.get("type")).append("(").append(fieldMap.get("length")).append(") NOT NULL DEFAULT CURRENT_TIMESTAMP,");
                    } else if ("translate_time".equals(fieldMap.get("name"))) {
                        keyStr.append("`").append(fieldMap.get("name")).append("` ").append(fieldMap.get("type")).append("(").append(fieldMap.get("length")).append(") DEFAULT NULL,");

                    } else {
                        keyStr.append("`").append(fieldMap.get("name")).append("` ").append(fieldMap.get("type")).append("(").append(fieldMap.get("length")).append("),");
                    }
                }
            }
            //整体sql拼接
            sqlString += keyStr + "PRIMARY KEY (`id`))";
            sqlString = sqlString.replaceAll("text\\(0\\)", "text NUll");
            logger.info(sqlString);
            JdbcOperate.updateSql(sqlString, con);
            if (!"website".equals(tableName)) {
                String sqlStringIndex;
                if ("news_static_file".equals(tableName)) {
                    sqlStringIndex = "ALTER TABLE `" + dataBaseMap.get("dbName") + "`.`" + tableName + "`ADD INDEX `url_md5`(`url_md5`) USING HASH;";
                } else {
                    sqlStringIndex = "ALTER TABLE `" + dataBaseMap.get("dbName") + "`.`" + tableName + "`ADD INDEX `url_title_md5`(`url_title_md5`) USING HASH;";
                }
                JdbcOperate.updateSql(sqlStringIndex, con);
            }
        } else {
            logger.debug("the table has been created: " + tableName);
        }
    }

    public static Connection getSqlConn(Map<String, Object> dataBaseMap) {
//        获取数据库name
        String server = (String) dataBaseMap.get("serverName");
        String s = null;
        try {
            s = FileUtils.readFileToString(new File(PropertyUtils.getPathByName(PropertyUtils.SQL_CONFIG_PATH)));
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
//        对xml文件以&分隔符分割后进行遍历
        String[] sqlDb = new String[0];
        if (s != null) {
            sqlDb = s.split("&&");
        }
        for (String sbb : sqlDb) {
            String[] sqlDetail = sbb.split(";\r\n");

//            判断是否为我们要的数据库信息
            if (sqlDetail[0].contains(server)) {
                Map<String, String> sqlmap = new HashMap<>(16);
//                获得数据库信息后，转为map
                for (String sb : sqlDetail) {
                    if (sb.contains("=")) {
                        int index = sb.indexOf("=");
                        sqlmap.put(sb.substring(0, index), sb.substring(index + 1));
                    }
                }
                String driverClass = sqlmap.get("driverClass");
                String url = sqlmap.get("url");
                String username = sqlmap.get("username");
                String password = sqlmap.get("password");
                Connection con = null;
                try {
                    Class.forName(driverClass);
                    con = DriverManager.getConnection(url, username, password);
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }

                logger.debug("mysql-db connection has connected successfully ");
                return con;

            }
        }
        logger.error("mysql-db connection has connected failed" +
                "\n出错信息为：" + dataBaseMap.toString());
        return null;
    }

    private static String selectSql(String sqlString, Connection con) {
        ResultSet rs;
        String result = null;
        PreparedStatement pst;
        try {
            pst = con.prepareStatement(sqlString);
            rs = pst.executeQuery();
            while (rs.next()) {
                result = rs.getString(1);
            }
        } catch (SQLException e) {
            logger.error("数据库链接异常！！\n异常信息为：" + e.getSQLState() + "\n" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private static Map<String, String> selectNewsFileByUrlMd5(String sqlString, Connection con) {
        Map<String, String> resultHashMap = new HashMap<>(16);
        ResultSet rs;
        PreparedStatement pst;
        try {
            pst = con.prepareStatement(sqlString);
            rs = pst.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnContent = rs.getString(i);
                    resultHashMap.put(columnName, columnContent);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultHashMap;
    }

    public static String selectIdByMd5(Map<String, Object> dataBaseMap, Connection con, String md5Str) {
        String tableName = dataBaseMap.get("tableName").toString();
        String sql;
        if ("news_static_file".equals(tableName) || "news_info".equals(tableName)) {
            sql = "SELECT id FROM `" + dataBaseMap.get("dbName") + "`.`" + tableName + "` where url_md5=" + "'" + md5Str + "'";
        } else {
            sql = "SELECT id FROM `" + dataBaseMap.get("dbName") + "`.`" + tableName + "` where url_title_md5=" + "'" + md5Str + "'";
        }
        logger.debug(sql);
        return selectSql(sql, con);
    }

    private static String selectBySiteId(Map<String, Object> dataBaseMap, Connection con, Integer siteId) {
        String sql = "SELECT id FROM `" + dataBaseMap.get("dbName") + "`.`" + dataBaseMap.get("tableName") + "` where site_id=" + siteId;
        logger.debug(sql);
        return selectSql(sql, con);
    }

    public static Map<String, Object> selectWebsiteBySiteId(Map<String, Object> dataBaseMap, Connection con, String siteId) {
        String sql = "SELECT * FROM `" + dataBaseMap.get("dbName") + "`.`" + dataBaseMap.get("tableName") + "` where site_id='" + siteId + "'";
        logger.debug(sql);
        return selectSqlList(sql, con);
    }

    private static Map<String, Object> selectSqlList(String sqlString, Connection con) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(sqlString);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            ResultSet rs = null;
            if (pst != null) {
                rs = pst.executeQuery();
            }
            if (rs != null) {
                return convertMap(rs);
            }
        } catch (SQLException e) {
            try {
                logger.error(e.getSQLState() + "\n数据库的链接warning为：" + con.getWarnings()
                        + "\n连接状态为：" + con.isClosed() + "连接信息为：" + con.getMetaData());
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

        return null;
    }

    private static Map<String, Object> convertMap(ResultSet rs) {

        Map<String, Object> map = new TreeMap<>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    map.put(md.getColumnName(i), rs.getObject(i));
                }
            }

        } catch (SQLException e) {
            logger.error(e.getSQLState());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error(e.getSQLState());
                e.printStackTrace();
            }
        }
        return map;
    }
}


