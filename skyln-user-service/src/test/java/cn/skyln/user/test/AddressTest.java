package cn.skyln.user.test;

import cn.skyln.user.UserApplication;
import cn.skyln.user.web.model.DO.AddressDO;
import cn.skyln.user.web.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: lamella
 * @Date: 2022/09/01/21:05
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserApplication.class)
@Slf4j
public class AddressTest {
    @Autowired
    private AddressService addressService;

    @Test
    public void testAddressDetail(){
        AddressDO addressDO = addressService.getOneById(1L);
        log.info(addressDO.toString());
    }
}
