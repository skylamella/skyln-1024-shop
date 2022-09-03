package cn.skyln.user.test;

import cn.hutool.json.JSONObject;
import cn.skyln.user.UserApplication;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sts.v20180813.StsClient;
import com.tencentcloudapi.sts.v20180813.models.GetFederationTokenRequest;
import com.tencentcloudapi.sts.v20180813.models.GetFederationTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: lamella
 * @Date: 2022/09/03/14:36
 * @Description:
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserApplication.class)
@Slf4j
public class COSTest {

    @Value("${cos.secretId}")
    private String secretId;
    @Value("${cos.secretKey}")
    private String secretKey;
    @Value("${cos.region}")
    private String regionStr;
    @Value("${cos.bucketName}")
    private String bucketName;

    @Test
    public void testTmpFederationToken() throws TencentCloudSDKException {
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
        req.setName("testTmpFederationToken");
        req.setPolicy("{\"version\":\"2.0\",\"statement\":[{\"action\":[\"cos:*\"],\"resource\":\"*\",\"effect\":\"allow\"},{\"effect\":\"allow\",\"action\":[\"monitor:*\",\"cam:ListUsersForGroup\",\"cam:ListGroups\",\"cam:GetGroup\"],\"resource\":\"*\"}]}");
        req.setDurationSeconds(1800L);
        // 返回的resp是一个GetFederationTokenResponse的实例，与请求对象对应
        GetFederationTokenResponse resp = client.GetFederationToken(req);
        System.out.println(new JSONObject(GetFederationTokenResponse.toJsonString(resp)));
    }
}
