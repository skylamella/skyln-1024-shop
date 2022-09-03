package cn.skyln.user.component;

import com.qcloud.cos.model.PutObjectResult;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author: lamella
 * @Date: 2022/09/03/14:22
 * @Description:
 */
public interface CosComponent {
    String uploadFileResult(String folder, MultipartFile uploadFile, String useForName);
}
