package cn.skyln.utils;

import cn.skyln.constant.TimeConstant;
import cn.skyln.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * JWT工具类，采用RSA进行加解密
 */
public class JWTUtils {

    /**
     * 刷新token时间，1天
     */
    private static final int REFRESH_EXPIRE = 24;

    /**
     * subject
     */
    private static final String SUBJECT = "SKYLN-1024-SHOP";

    /**
     * 根据用户信息，生成token，设置过期时间为30分钟，使用rsa私钥加密
     *
     * @param user       用户对象
     * @param privateKey 私钥
     * @return java.lang.String
     */
    public static String generateToken(LoginUser user, PrivateKey privateKey) {
        return Jwts.builder()
                .setSubject(SUBJECT)
                .claim("head_img", user.getHeadImg())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .claim("mail", user.getMail())
                .setId(createJTI())
                .setIssuedAt(new Date())
                // todo 测试使用设置为30天
                .setExpiration(DateTime.now().plusDays(Math.toIntExact(TimeConstant.EXPIRATION_TIME_MINUTE)).toDate())
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    /**
     * 根据用户信息，生成token，设置过期时间为30分钟，使用rsa私钥加密
     *
     * @param user       用户对象
     * @param privateKey 私钥
     * @param ip         ip
     * @return java.lang.String
     */
    public static String generateToken(LoginUser user, PrivateKey privateKey, String ip) {
        if (StringUtils.isBlank(ip)) return generateToken(user, privateKey);
        return Jwts.builder()
                .setSubject(SUBJECT)
                .claim("head_img", user.getHeadImg())
                .claim("id", user.getId())
                .claim("ip", ip)
                .claim("name", user.getName())
                .claim("mail", user.getMail())
                .setId(createJTI())
                .setIssuedAt(new Date())
                // todo 测试使用设置为30天
                .setExpiration(DateTime.now().plusDays(Math.toIntExact(TimeConstant.EXPIRATION_TIME_MINUTE)).toDate())
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    /**
     * 未超过刷新token的刷新时间，重新生成token，设置过期时间为30分钟，使用rsa私钥加密
     *
     * @param claims     载荷
     * @param privateKey 私钥
     * @return java.lang.String
     */
    public static String refreshGenerateToken(Claims claims, PrivateKey privateKey) {
        return Jwts.builder()
                .setSubject(SUBJECT)
                .setClaims(claims)
                .setId(createJTI())
                .setIssuedAt(new Date())
                .setExpiration(DateTime.now().plusMinutes(Math.toIntExact(TimeConstant.EXPIRATION_TIME_MINUTE)).toDate())
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    /**
     * 根据用户信息，生成刷新校验token，设置刷新时间为1天，使用rsa私钥加密
     *
     * @param user       用户对象
     * @param privateKey 私钥
     * @return java.lang.String
     */
    public static String generateRefreshToken(LoginUser user, PrivateKey privateKey) {
        return Jwts.builder()
                .setSubject(SUBJECT)
                .claim("head_img", user.getHeadImg())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .claim("mail", user.getMail())
                .setId(createJTI())
                .setIssuedAt(new Date())
                .setExpiration(DateTime.now().plusHours(Math.toIntExact(TimeConstant.EXPIRATION_TIME_HOUR)).toDate())
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    /**
     * 根据用户信息，生成刷新校验token，设置刷新时间为1天，使用rsa私钥加密
     *
     * @param user       用户对象
     * @param privateKey 私钥
     * @param ip         ip
     * @return java.lang.String
     */
    public static String generateRefreshToken(LoginUser user, PrivateKey privateKey, String ip) {
        if (StringUtils.isBlank(ip)) return generateRefreshToken(user, privateKey);
        return Jwts.builder()
                .setSubject(SUBJECT)
                .claim("head_img", user.getHeadImg())
                .claim("id", user.getId())
                .claim("ip", ip)
                .claim("name", user.getName())
                .claim("mail", user.getMail())
                .setId(createJTI())
                .setIssuedAt(new Date())
                .setExpiration(DateTime.now().plusHours(Math.toIntExact(TimeConstant.EXPIRATION_TIME_HOUR)).toDate())
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    /**
     * 公钥解析token
     *
     * @param token     token
     * @param publicKey 公钥
     * @return claims
     */
    public static Claims checkJWTPublicKey(String token, PublicKey publicKey) {
        try {
            final Claims claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token).getBody();
            if (StringUtils.equals(claims.getSubject(), SUBJECT)) {
                return claims;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String createJTI() {
        return new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()));
    }
}
