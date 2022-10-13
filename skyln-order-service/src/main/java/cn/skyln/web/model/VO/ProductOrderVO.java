package cn.skyln.web.model.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author skylamella
 * @since 2022-09-12
 */
@Data
public class ProductOrderVO {

    private Long id;

    /**
     * 订单唯⼀标识
     */
    private String outTradeNo;

    /**
     * NEW	未⽀付订单,PAY已经⽀付订单,CANCEL超时取消订单
     */
    private String state;

    /**
     * 订单⽣成时间
     */
    private Date createTime;

    /**
     * 订单总⾦额
     */
    private BigDecimal totalAmount;

    /**
     * 订单实际⽀付价格
     */
    private BigDecimal payAmount;

    /**
     * ⽀付类型，微信-银⾏-⽀付宝
     */
    private String payType;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String headImg;

    /**
     * ⽤户	id
     */
    private Long userId;

    /**
     * 0表示未删除，	1表示已经删除
     */
    private Integer del;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 订单类型 DAILY普通单，PROMOTION促销订单
     */
    private String orderType;

    /**
     * 收货地址 json存储
     */
    private String receiverAddress;

    List<OrderItemVO> orderItemVOList;

}
