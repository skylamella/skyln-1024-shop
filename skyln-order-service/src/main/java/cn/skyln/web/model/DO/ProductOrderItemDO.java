package cn.skyln.web.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author skylamella
 * @since 2022-09-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_order_item")
public class ProductOrderItemDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private Long productOrderId;

    private String outTradeNo;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图⽚
     */
    private String productImg;

    /**
     * 购买数	ᰁ
     */
    private Integer buyNum;

    private Date createTime;

    /**
     * 购物项商品总价格
     */
    private BigDecimal totalAmount;

    /**
     * 购物项商品单价
     */
    private BigDecimal amount;


}
