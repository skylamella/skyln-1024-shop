package cn.skyln.web.model.DO;

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
 * @since 2022-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("cart")
public class CartDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;


}
