package cn.skyln.component.impl;

import cn.skyln.component.MailComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * @Author: lamella
 * @Date: 2022/09/02/21:42
 * @Description:
 */
@Component
@Slf4j
public class MailComponentImpl implements MailComponent {
    /**
     * Spring Boot 提供了一个发送邮件的简单抽象，直接注入即可使用
     */
    @Autowired
    private JavaMailSender mailSender;
    /**
     * 配置文件中的发送邮箱
     */
    @Value("${spring.mail.from}")
    private String from;

    /**
     * 发送一封右键
     *
     * @param to      收件人
     * @param subject 邮件主题
     * @param content 邮件正文
     */
    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        //创建SimpleMailMessage对象
        SimpleMailMessage message = new SimpleMailMessage();
        //邮件发送人
        message.setFrom(from);
        //邮件接收人
        message.setTo(to);
        //邮件主题
        message.setSubject(subject);
        //邮件内容
        message.setText(content);
        //发送邮件
        mailSender.send(message);
        log.info("邮件发成功:{}", message.toString());
    }
}
