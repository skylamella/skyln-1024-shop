package cn.skyln.user.test;

import cn.skyln.model.LoginUser;
import cn.skyln.utils.JWTUtils;
import cn.skyln.utils.RsaUtils;
import cn.skyln.UserApplication;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: lamella
 * @Date: 2022/09/04/21:30
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserApplication.class)
@Slf4j
public class JWTTest {

    @Test
    public void testRsaJwt() {
        try {
            LoginUser loginUser = new LoginUser();
            loginUser.setId(1L);
            loginUser.setMail("demo@demo.com");
            loginUser.setHeadImg("demoImg.jpg");
            loginUser.setName("demoName");
            String token = JWTUtils.generateToken(loginUser, RsaUtils.getPrivateKey());
            log.info("[token] {}", token);
            Claims claims = JWTUtils.checkJWTPublicKey(token, RsaUtils.getPublicKey());
            assert claims != null;
            log.info("[claims:{}] {}", "head_img", claims.get("head_img"));
            log.info("[claims:{}] {}", "id", claims.get("id"));
            log.info("[claims:{}] {}", "name", claims.get("name"));
            log.info("[claims:{}] {}", "mail", claims.get("mail"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
