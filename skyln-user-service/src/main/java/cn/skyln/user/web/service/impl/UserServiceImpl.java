package cn.skyln.user.web.service.impl;

import cn.skyln.user.web.mapper.UserMapper;
import cn.skyln.user.web.model.DO.UserDO;
import cn.skyln.user.web.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author skylamella
 * @since 2022-08-30
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDO getOneByMail(String mail) {
        return userMapper.selectOne(new QueryWrapper<UserDO>().eq("mail", mail));
    }

    @Override
    public void saveOne(UserDO userDO) {
        userMapper.insert(userDO);
    }

    @Override
    public void updateOne(UserDO userDO) {
        userMapper.updateById(userDO);
    }
}
