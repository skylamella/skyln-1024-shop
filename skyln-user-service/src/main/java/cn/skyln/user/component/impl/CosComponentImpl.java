package cn.skyln.user.component.impl;

import cn.hutool.json.JSONObject;
import cn.skyln.common.utils.CommonUtils;
import cn.skyln.user.component.CosComponent;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sts.v20180813.StsClient;
import com.tencentcloudapi.sts.v20180813.models.GetFederationTokenRequest;
import com.tencentcloudapi.sts.v20180813.models.GetFederationTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: lamella
 * @Date: 2022/09/03/14:22
 * @Description:
 */
@Component
@Slf4j
public class CosComponentImpl implements CosComponent {
    @Value("${cos.secretId}")
    private String secretId;
    @Value("${cos.secretKey}")
    private String secretKey;
    @Value("${cos.region}")
    private String regionStr;
    @Value("${cos.bucketName}")
    private String bucketName;

    private static final String CAM_POLICY = "{\"version\":\"2.0\",\"statement\":[{\"action\":[\"cos:*\"],\"resource\":\"*\",\"effect\":\"allow\"},{\"effect\":\"allow\",\"action\":[\"monitor:*\",\"cam:ListUsersForGroup\",\"cam:ListGroups\",\"cam:GetGroup\"],\"resource\":\"*\"}]}";

    /**
     * 上传文件到腾讯云COS
     *
     * @param folder     文件路径
     * @param uploadFile 待上传文件
     * @param useForName 用途
     * @return 文件访问URL
     */
    @Override
    public String uploadFileResult(String folder, MultipartFile uploadFile, String useForName) {
        TransferManager transferManager = createTransferManager(useForName);
        if (Objects.isNull(transferManager)) {
            return null;
        }
        String uploadFullName = uploadFile.getOriginalFilename();
        String suffix = uploadFullName.substring(uploadFullName.lastIndexOf("."));
        String key = getUploadKey(folder, CommonUtils.MD5(uploadFullName), suffix);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, uploadFile.getInputStream(), objectMetadata);
            UploadResult uploadResult = transferManager.upload(putObjectRequest).waitForUploadResult();
            return uploadResult.getKey();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            transferManager.shutdownNow(true);
        }
    }

    private String getUploadKey(String folder, String fileName, String suffix) {
        StringBuilder sb = new StringBuilder();
        if (folder.contains("/")) {
            for (String str : folder.split("/")) {
                if (StringUtils.isNotBlank(str)) {
                    sb.append("/").append(str);
                }
            }
        } else {
            sb.append("/").append(folder);
        }
        sb.append("/").append(fileName).append(suffix);
        return sb.toString();
    }

    private TransferManager createTransferManager(String useForName) {
        // 创建一个 COSClient 实例，这是访问 COS 服务的基础实例。
        // 详细代码参见本页: 简单操作 -> 创建 COSClient
        COSClient cosClient = getCOSClient(useForName);
        if (Objects.isNull(cosClient)) {
            return null;
        }
        // 自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        TransferManager transferManager = new TransferManager(cosClient, threadPool);
        // 设置高级接口的配置项
        // 分块上传阈值和分块大小分别为 5MB 和 1MB
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(5 * 1024 * 1024);
        transferManagerConfiguration.setMinimumUploadPartSize(1024 * 1024);
        transferManager.setConfiguration(transferManagerConfiguration);

        return transferManager;
    }

    private COSClient getCOSClient(String useForName) {
        JSONObject jsonObject = getTmpFederationToken(useForName);
        if (jsonObject.isEmpty()) {
            return null;
        }
        BasicSessionCredentials cred = new BasicSessionCredentials(jsonObject.getStr("TmpSecretId"),
                jsonObject.getStr("TmpSecretKey"),
                jsonObject.getStr("Token"));
        // 2 设置 bucket 的地域, COS 地域的简称请参阅 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参阅源码或者常见问题 Java SDK 部分
        Region region = new Region(regionStr);
        ClientConfig clientConfig = new ClientConfig(region);
        // 3 生成 cos 客户端
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(cred, clientConfig);
    }

    private JSONObject getTmpFederationToken(String useForName) {
        Credential cred = new Credential(secretId, secretKey);
        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("sts.tencentcloudapi.com");
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        // 实例化要请求产品的client对象,clientProfile是可选的
        StsClient client = new StsClient(cred, regionStr, clientProfile);
        // 实例化一个请求对象,每个接口都会对应一个request对象
        GetFederationTokenRequest req = new GetFederationTokenRequest();
        req.setName(useForName);
        req.setPolicy(CAM_POLICY);
        req.setDurationSeconds(1800L);
        // 返回的resp是一个GetFederationTokenResponse的实例，与请求对象对应
        GetFederationTokenResponse resp = null;
        try {
            resp = client.GetFederationToken(req);
            JSONObject jsonObject = new JSONObject(GetFederationTokenResponse.toJsonString(resp));
            log.info("[获取临时token成功 {}]", jsonObject.getStr("Expiration"));
            return jsonObject.get("Credentials", JSONObject.class);
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
}
