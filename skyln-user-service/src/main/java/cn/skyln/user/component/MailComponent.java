package cn.skyln.user.component;

/**
 * @Author: lamella
 * @Date: 2022/09/02/21:42
 * @Description:
 */
public interface MailComponent {

    void sendSimpleMail(String to, String subject, String content);
}
