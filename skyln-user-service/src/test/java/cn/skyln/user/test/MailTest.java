package cn.skyln.user.test;

import cn.skyln.user.UserApplication;
import cn.skyln.user.component.MailComponent;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: lamella
 * @Date: 2022/09/02/22:08
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserApplication.class)
@Slf4j
public class MailTest {

    @Autowired
    private MailComponent mailComponent;

    @Test
    public void testSendMail(){
        mailComponent.sendSimpleMail("734769912@qq.com","这是一个测试邮件的标题","这是一个测试邮件的正文");
    }
}
