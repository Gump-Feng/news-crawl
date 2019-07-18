package com.ertu.news.utils;

import com.ertu.news.model.bean.ConfigBean;
import org.apache.log4j.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * @author hxf
 * @date 2019/6/3 11:18
 * <p>
 *  个人钉邮：houxiaofeng0228@dingtalk.com
 */
public class MailUtils {
    private static Logger logger = Logger.getLogger(MailUtils.class);

    public static void main(String[] args) {

    }

    public static void sendErrorMail2Monitor(ConfigBean mailConfigBean) {

        String mailContent = getMailContent(mailConfigBean);
        Properties properties = new Properties();
        // 连接协议
        properties.put("mail.transport.protocol", "smtp");
        // 主机名
        properties.put("mail.smtp.host", "smtp.qq.com");
        // 端口号
        properties.put("mail.smtp.port", 465);
        properties.put("mail.smtp.auth", "true");
        // 设置是否使用ssl安全连接 ---一般都使用
        properties.put("mail.smtp.ssl.enable", "true");
        // 设置是否显示debug信息 true 会在控制台显示相关信息
        properties.put("mail.debug", "false");
        // 使用验证，创建一个Authenticator
//        Authenticator auth = new MailAuthenticator("798273024@qq.com", "");
        // 得到回话对象
        Session session = Session.getInstance(properties);
        // 获取邮件对象
        Message message = new MimeMessage(session);
        // 设置发件人邮箱地址
        try {
            message.setFrom(new InternetAddress("798273024@qq.com"));
            // 设置收件人邮箱地址
            message.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress("houxiaofeng0228@dingtalk.com"),new InternetAddress("kog3280@dingtalk.com")});
            // 设置邮件标题
            message.setSubject("资讯采集栏目出错入口连接");
            // 设置邮件内容
            message.setText(mailContent);
            // 得到邮差对象
            Transport transport = session.getTransport();
            // 连接自己的邮箱账户
            // 密码为QQ邮箱开通的stmp服务后得到的客户端授权码
            transport.connect("798273024@qq.com", "fqvalptmhrvnbdag");
            // 发送邮件
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            logger.info("成功向侯晓峰的钉邮发送一封邮件");
        } catch (MessagingException e) {
            logger.error("向侯晓峰的钉邮发送邮件失败，失败原因：\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getMailContent(ConfigBean configBean) {
        String xmlPath = configBean.getXmlPath();
        int siteId = configBean.getSiteBean().getSiteId();
        String websiteNameEn = configBean.getSiteBean().getWebsiteNameEn();
        String websiteColumnNameEn = configBean.getSiteBean().getWebsiteColumnNameEn();
        List<String> siteUrls = configBean.getSiteBean().getSiteUrls();
        Map<String, String> hostIpAndNameMap = getHostIpAndName();
        String localIp = hostIpAndNameMap.get("hostIp");
        String hostName = hostIpAndNameMap.get("hostName");
        return "请求出错栏目信息\n采集机器ip：" + localIp + "\n采集机器名称：" + hostName + "\n出错栏目id：" + siteId + "\n出错网站名称：" + websiteNameEn
                + "\n出错栏目名称：" + websiteColumnNameEn + "\n出错栏目配置文件路径：" + xmlPath + "\n出错栏目入口连接：" + siteUrls;
    }

    private static Map<String, String> getHostIpAndName() {
        Map<String, String> hostInfoMap = new HashMap<>(10);
        // 本地IP，如果没有配置外网IP则返回它
        String localIp = null;
        // 外网IP
        String netIp = null;
        //机器名称
        String hostName = null;
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip ;
            // 是否找到外网IP
            boolean isFind = false;
            while (netInterfaces.hasMoreElements() && !isFind) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = address.nextElement();
                    if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(":")) {
                        // 外网IP
                        netIp = ip.getHostAddress();
                        hostName = ip.getHostName();
                        isFind = true;
                        break;
                    } else if (ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(":")) {
                        // 内网IP
                        localIp = ip.getHostAddress();
                        hostName = ip.getHostName();
                    }
                }
            }
            if (netIp != null && !"".equals(netIp)) {
                hostInfoMap.put("hostIp", netIp);
            } else {
                hostInfoMap.put("hostIp", localIp);
            }
            hostInfoMap.put("hostName", hostName);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostInfoMap;
    }
}
