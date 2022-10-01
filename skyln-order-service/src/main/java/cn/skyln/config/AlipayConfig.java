package cn.skyln.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;

/**
 * @Author: lamella
 * @Date: 2022/09/26/21:53
 * @Description:
 */
public class AlipayConfig {

    /**
     * 应用私钥
     */
    private static final String APP_PRI_KEY = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCkhih+KSdxpL/eeInm1QWlDbMhHXJ0RDhqSgAV1/Xj5MOUwsXSM84MHhYG4rg6sCKvs5j1HI/M2c9q6sxK6S7129p0rQ6q75Zp6UdESig8nI9YhyPmrkNqcvT3o6MqKJF/lmKiLcYjhwF6BlgyfuNEV8oGHAyxqG4mA+IqLn+lfT34UqJr8lRYYiQcbXsFAvjc/ZgSgCzWLN044Pp3F7xc8aqrhr9OPtbKjQRp6AcxxvJ2q2zSNzHxJy+cDVjlhg4AVraO42r+T3bDhB7zI7nf7g9FwFxixKNdys6ozNw8qS1LN72c1yrTk63Al+M67339TTQBRHWwYl/BWBZ29urpAgMBAAECggEBAIXNQdNQvu5ykURp/aUIa/3PBXaSD0o1TThMFIOvTh/YJaOvP9jzHsjGEx3odvHr21nPkz8MynpMTZ7jc+Acgn+bHLA1Ki+kiEx8VuRX9x9MKFxUE8OAx/0jnsxjfchElAgSkwbYeULdgjruO/5dKZGkX/3WBBDcLTCnieDKTi+UNYkFIopFoJ65NgsC0M2DNi2YCcMrUNWgyMrIKWKqNJ7bctv6azvGaTblDQ+1WiyFvL6d7LNwH2Y/4CJnf5Hakv7Dxftqw0sXP6EEuStD/cqw1oT5In2PWJuYdb2LWVD96P8No2d98P8GAZPkGiGNPogKX6Fy6bS2rGwSWAZwuT0CgYEA/SI2L/0MtdRJBIbRzQNdiF8ZthxKqzT74Oon315f+141/WJP7BUY3Uavya0bV5BO4hFlGdtDmjvpQu4y3aRXJCcDslBPWHMNrU13AV7yuzhvwuaAoeIHICSUcaXXAjDQnJcuznd2J9eiOglYHhbAjuVVPg7BM8vMx5OSW+MzI/8CgYEApmMVXVOSRFEqvgNpi8JBRb2I7zUq5XUXWDst9GTh49nNQytSvB6TFO3/MAD1T6knhQvt3mp3fH3rJqfosuCmZ9As7eUOB3WKIySoBiKRlHCyV/e3FA2e3P5ZorgEbMxeATrzYWf435wLbJuUfKZiPX0C/rkx34DcWSACAdUFURcCgYBnvdytYC0UXBMsysAzIFz15dvYudGURxuveueqcLgvgdXoqaENpTvSfATRR6O8CEV7+xsPNMCI9mLL7mlkUtTv2Rl3u9g47LY/P081fZ9HQYL0QpmXPtYiwzsfNJNgr3bfpkoljeobvBEdbfTyJ+r05WJBE7T0sGKVrJJAHGY6KwKBgQCQsAz+ERRacDK49YKs1Amqub0ANr3Mt1vLj/rQUYHVf6rtRBk4GWZRhvyoI80urqv9GHiLNw4cper8vHqwQoayM+c+IDdo6R9snA24+AqciLutn24G9Ck3h78urtK6QaVlcHwAMkfLO4j7svcWxiEb0z/tfeIOt6Hlpt3X9gQflQKBgQCDyBXiqs8yNNEqZMTyB5OEZnW63CQX0tPB7trfBEZtumFpmbeDv3U3Se/hPhRnrx0t039782kuwWkHw6h8ImpJ1gcCKFm/vMqUuUcLvS0iIrjxqpJI4iBySVXCuw5bajtFB1HJCQpEEhhmENRRf51WC+l+fcfLdXESvPr6a9dDlw==";

    /**
     * 支付宝公钥
     */
    public static final String ALIPAY_PUB_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuZpexJcEWmXnnHEGR1mDFassB54thQVZouL92dJeSwfI2WqY5Ec3YMRcXq8A8xj0t1IlD9bbOPWyknX2B7ArZHajG37vGn8iXn0yTlovyCyjMAScFFMUMNieaww5bx6OkyrP0Q4sPdrjR3W6xmcdBl9vrbSRWAK6W/T1K1pL5/6trK9Fc0zuceoJuB9p0IC6aspuTKwjeKXX5Wi2PEzHN0EnEMWoepOwRyhDJ5meiqp9jy1Y4RnVbBJ513384YP+TPo4Ud+kaU+41AMCKCRK6tcLxtTA3uY6lhtu8RlVkMIDbUIoMPme6ObinOhFFhGW0Ww+wMH6x+wycLcGPIONkQIDAQAB";

    /**
     * 支付宝沙箱APPID
     */
    private static final String APPID = "2021000121671000";

    /**
     * 签名类型
     */
    public static final String SIGN_TYPE = "RSA2";

    /**
     * 编码
     */
    public static final String CHARSET = "UTF-8";

    /**
     * 返回参数格式
     */
    public static final String FORMAT = "json";

    /**
     * 沙箱网关
     */
    private static final String PAY_GATEWAY = "https://openapi.alipaydev.com/gateway.do";

    /**
     * 构造函数私有化
     */
    private AlipayConfig() {

    }

    private volatile static AlipayClient instance = null;

    /**
     * 单例模式获取，双重锁校验
     *
     * @return AlipayClient
     */
    public static AlipayClient getInstance() {
        if (instance == null) {
            synchronized (AlipayConfig.class) {
                if (instance == null) {
                    instance = new DefaultAlipayClient(PAY_GATEWAY, APPID, APP_PRI_KEY, FORMAT, CHARSET, ALIPAY_PUB_KEY, SIGN_TYPE);
                }
            }
        }
        return instance;
    }
}
