package cn.skyln.component;

/**
 * @Author: lamella
 * @Date: 2022/09/02/21:42
 * @Description:
 */
public interface MailComponent {

    /**
     * 发送一封右键
     *
     * @param to      收件人
     * @param subject 邮件主题
     * @param content 邮件正文
     */
    void sendSimpleMail(String to, String subject, String content);
}
