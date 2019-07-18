package com.ertu.news.utils;


import com.ertu.news.model.bean.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hxf
 * 时间转换的工具类，入口方法为formatTime（String dateStr,String timeType）
 */
public class TimeUtils {
    private static Logger logger = LoggerFactory.getLogger(TimeUtils.class);

    public static Date formatTime(String dateStr, String regex, String timeType) {
        switch (timeType) {
            case "":
                return stringToDate(dateStr, "yyyy-MM-dd");
            case "date_Mdy":
                return dateMdy(dateStr, regex);
            case "date_dMy":
                return dateDMY(dateStr, regex);
            case "date_yMd":
                return dateYMD(dateStr, regex);
            case "date_Md":
                return dateMd(dateStr, regex);                
            case "date_dM":
                return datedM(dateStr, regex);
            case "date_yM":
                return dateYM(dateStr, regex);
            case "date_My":
                return dateMy(dateStr, regex);
            case "date_y":
                return dateY(dateStr, regex);
            default:
                return null;
        }
    }

    private static Date dateMd(String dateStr,String regex) {
    	String[] md = formatDate(dateStr, regex);
    	if(null==md) {
    		logger.warn("There is an error on regex of publishDate");
    		return null;
    	}else {
    		Integer dayFormat = dayFormat(md[1]);
    		Integer monthFormat = monthFormat(md[0]);
    		int yearFormat = LocalDate.now().getYear();
    		return stringToDate(yearFormat + "-" + monthFormat + "-" + dayFormat, "yyyy-MM-dd");
    	}
    }
    
    private static Date datedM(String dateStr, String regex) {
        String[] md = formatDate(dateStr, regex);
        if (null == md) {
            logger.warn("There is an error on regex of publishDate");
            return null;
        } else {
            Integer dayFormat = dayFormat(md[0]);
            Integer monthFormat = monthFormat(md[1]);
            int yearFormat = LocalDate.now().getYear();
            return stringToDate(yearFormat + "-" + monthFormat + "-" + dayFormat, "yyyy-MM-dd");
        }
    }

    private static Date dateY(String dateStr, String regex) {
        String[] yearArray = formatDate(dateStr, regex);
        if (yearArray == null || yearArray.length <= 0) {
            logger.warn("There is an error on regex of publishDate");
            return null;
        }
        Integer yearFormat = yearFormat(yearArray[0]);
        Integer monthFormat = 1;
        Integer dayFormat = 1;
        String formatTime = yearFormat + "-" + monthFormat + "-" + dayFormat;
        return stringToDate(formatTime, "yyyy-MM-dd");
    }

    private static Date dateMy(String dateStr, String regex) {
        String[] myArray = formatDate(dateStr, regex);
        if (myArray == null) {
            logger.warn("There is an error on regex of publishDate");
            return null;
        }
        Integer monthFormat = 1;
        Integer yearFormat = 2019;
        if (myArray.length == 1) {
            yearFormat = yearFormat(myArray[0]);
        }
        if (myArray.length == 2) {
            monthFormat = monthFormat(myArray[0]);
            yearFormat = yearFormat(myArray[1]);
        }
        String dateToString = dateToString(new Date(), "yyyy-MM-dd");
        String dayFormat = dateToString.substring(8);
        String formatTime = yearFormat + "-" + monthFormat + "-" + dayFormat;
        return stringToDate(formatTime, "yyyy-MM-dd");
    }


    private static Date stringToDate(String time, String format) {
        Date date;
        String year = "年";
        String day = "日";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        if (time.contains(year) && time.contains(day)) {
            time = time.trim().replaceAll("[^0-9]", "-");
        }
        try {
            date = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return date;
    }


    public static String dateToString(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format,Locale.CHINA);
        if (date == null) {
            return "";
        } else {
            return dateFormat.format(date);
        }
    }

    private static Date dateMdy(String dateStr, String regex) {
        String[] mdy = formatDate(dateStr, regex);
        if (null == mdy) {
            logger.warn("There is an error on regex of publishDate");
            return null;
        } else {
            Integer monthFormat = monthFormat(mdy[0]);
            Integer dayFormat = dayFormat(mdy[1]);
            Integer yearFormat = yearFormat(mdy[2]);
            String formatTime = yearFormat + "-" + monthFormat + "-" + dayFormat;
            return stringToDate(formatTime, "yyyy-MM-dd");
        }
    }


    private static Date dateDMY(String dateStr, String regex) {
        String[] dMy = formatDate(dateStr, regex);
        if (null == dMy) {
            logger.warn("There is an error on regex of publishDate");
            return null;
        } else {
            Integer dayFormat = dayFormat(dMy[0]);
            Integer monthFormat = monthFormat(dMy[1]);
            Integer yearFormat = yearFormat(dMy[2]);
            String formatTime = yearFormat + "-" + monthFormat + "-" + dayFormat;
            return stringToDate(formatTime, "yyyy-MM-dd");
        }
    }


    private static Date dateYMD(String dateStr, String regex) {
        dateStr = dateStr.trim().replaceAll("[^a-zA-Z0-9]", "_").replaceAll("[_]+", "_");
        if (!dateStr.contains("_")){
            return stringToDate(dateStr, "yyyyMMdd");
        }
        return dateTrans(dateStr, regex, logger);
    }

    private static Date dateTrans(String dateStr, String regex, Logger logger) {
        String[] dMy = formatDate(dateStr, regex);
        if (null == dMy) {
            logger.warn("There is an error on regex of publishDate");
            return null;
        } else {
            Integer yearFormat = yearFormat(dMy[0]);
            Integer monthFormat = monthFormat(dMy[1]);
            Integer dayFormat = 1;
            if (dMy.length > 2) {
                dayFormat = dayFormat(dMy[2]);
            }
            String formatTime = yearFormat + "-" + monthFormat + "-" + dayFormat;
            return stringToDate(formatTime, "yyyy-mm-dd");
        }
    }

    private static Date dateYM(String dateStr, String regex) {
        return dateTrans(dateStr, regex, logger);
    }

    private static String[] formatDate(String dateStr, String regex) {
        if ("".equals(regex)) {
            regex = ".+";
        }
        Pattern pattern = Pattern.compile(regex);
        //去掉空格进行匹配
        Matcher matcher = pattern.matcher(dateStr.trim());
        if (matcher.find()) {
            String group = matcher.group();
            if (null != group) {
                group = group.replaceAll("(?s)[^a-zA-Z0-9]", "_").replaceAll("(?s)[_]+", "_");
                return group.split("_");
            } else {
                return null;
            }
        }
        return null;
    }

    private static Integer yearFormat(String yearStr) {
        int year;
        int valid = 2;
        if (!"".equals(yearStr)) {
            if (yearStr.length() == valid) {
                yearStr = "20" + yearStr;
            }
            yearStr = yearStr.replaceAll("(?s)[^0-9]", "");
            year = Integer.parseInt(yearStr);
        } else {
            year = LocalDate.now().getYear();
        }
        return year;
    }

    private static Integer dayFormat(String dayStr) {
        int day;
        if (!"".equals(dayStr)) {
            dayStr = dayStr.replaceAll("(?s)[^0-9]", "");
            day = Integer.parseInt(dayStr);
        } else {
            day = 1;
        }
        return day;
    }

    private static Integer monthFormat(String monthStr) {
        int month = 1;
        int valid = 3;
        if (null != monthStr && !"".equals(monthStr) && monthStr.length() >= valid) {
            month = monthStringToInt(monthStr);
        } else {
            try{
                if (monthStr != null) {
                    month = Integer.parseInt(monthStr);
                }
            }catch (NumberFormatException e){
                logger.error("当前转换时间为："+monthStr);
            }

        }
        return month;
    }

    public static void main(String[] args) {
		/*Date date = new Date();
		String dateToString = DateToString(date, "yyyy-mm-dd");
		System.out.println(dateToString);*/
        String time = "2019年6月21日";
        Date date = stringToDate(time, "yyyy-MM-dd");
        //[a-zA-Z]{3,} [0-9]{1,2},[0-9]{4}
        //\d{4}
        System.out.println(date);


    }

    private static int monthStringToInt(String month) {
        int valid = 3;
        month = month.replaceAll("(?s)[^a-zA-Z]", "");
        if (month.length() >= valid) {
            month = month.toLowerCase().substring(0, 3);
        }

        switch (month) {
            case "jan":
                return 1;
            case "feb":
                return 2;
            case "mar":
                return 3;
            case "apr":
                return 4;
            case "may":
                return 5;
            case "jun":
                return 6;
            case "jul":
                return 7;
            case "aug":
                return 8;
            case "sep":
                return 9;
            case "oct":
                return 10;
            case "nov":
                return 11;
            case "dec":
                return 12;
            default:
                return 1;
        }

    }

    public static boolean isAdd2JobQueue(String timeStr, Site site) {
        if (timeStr != null && timeStr.contains(":")) {
            String[] timeSplit = timeStr.split(":");
            LocalTime settingTime = LocalTime.of(Integer.parseInt(timeSplit[0]), Integer.parseInt(timeSplit[1]));
            int settingSecond = settingTime.toSecondOfDay();
            int secondOfDay = LocalTime.now().toSecondOfDay();
            if (secondOfDay > settingSecond) {
                return false;
            }
            int difference = (settingSecond - secondOfDay) / 60;
            return difference <= 20;
        } else {
            logger.error("时间格式获取有问题\n"+"栏目路径为："+site.getConfigBean().getXmlPath());
            return false;
        }
    }
}
