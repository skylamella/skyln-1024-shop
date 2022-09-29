package cn.skyln.mqError.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author skylamella
 * @since 2022-09-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("mq_error_log")
public class MqErrorLogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String outTradeNo;


}
