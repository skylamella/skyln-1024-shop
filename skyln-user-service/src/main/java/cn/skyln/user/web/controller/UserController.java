package cn.skyln.user.web.controller;


import cn.skyln.common.enums.BizCodeEnum;
import cn.skyln.common.utils.JsonData;
import cn.skyln.user.component.CosComponent;
import cn.skyln.user.web.model.REQ.UserLoginRequest;
import cn.skyln.user.web.model.REQ.UserRegisterRequest;
import cn.skyln.user.web.model.VO.UserVO;
import cn.skyln.user.web.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author skylamella
 * @since 2022-08-30
 */
@Api(tags = "用户模块")
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private CosComponent component;

    @Autowired
    private UserService userService;

    @ApiOperation("用户头像上传")
    @PostMapping("avatar/upload")
    public JsonData uploadUserAvatar(@ApiParam(value = "头像文件", required = true) @RequestPart("file") MultipartFile file) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String folder = "user/avatar/" + dtf.format(now);
        String uploadUserAvatar = component.uploadFileResult(folder, file, "uploadUserAvatar");
        if (StringUtils.isNotBlank(uploadUserAvatar)) {
            return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS, uploadUserAvatar);
        } else {
            return JsonData.returnJson(BizCodeEnum.FILE_UPLOAD_USER_IMG_FAIL);
        }
    }

    @ApiOperation("用户注册")
    @PostMapping("register")
    public JsonData register(@ApiParam(value = "用户注册对象", required = true) @RequestBody UserRegisterRequest userRegisterRequest) {
        return userService.userRegister(userRegisterRequest);
    }

    @ApiOperation("用户登录")
    @PostMapping("login")
    public JsonData login(@ApiParam(value = "用户登录对象", required = true) @RequestBody UserLoginRequest userLoginRequest,
                          HttpServletRequest request) {
        return userService.userLogin(userLoginRequest, request);
    }

    @ApiOperation("用户个人信息")
    @PostMapping("detail")
    public JsonData userDetail() {
        UserVO userVO = userService.findUserDetail();
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, userVO);
    }

}

