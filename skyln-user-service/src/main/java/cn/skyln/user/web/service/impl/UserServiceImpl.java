package cn.skyln.user.web.service.impl;

import cn.skyln.user.web.model.DO.UserDO;
import cn.skyln.user.web.mapper.UserMapper;
import cn.skyln.user.web.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author skylamella
 * @since 2022-08-30
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

}
