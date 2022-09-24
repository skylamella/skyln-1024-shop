package cn.skyln.web.controller;


import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.DTO.LockProductDTO;
import cn.skyln.web.model.VO.ProductDetailVO;
import cn.skyln.web.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author skylamella
 * @since 2022-09-10
 */
@Api(tags = "商品模块")
@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @ApiOperation("分页查看商品")
    @GetMapping("page_product")
    public JsonData pageProduct(@ApiParam(value = "第几页", required = true) @RequestParam(value = "page", defaultValue = "1") int page,
                                @ApiParam(value = "一页显示几条", required = true) @RequestParam(value = "size", defaultValue = "8") int size) {

        Map<String, Object> pageMap = productService.pageProductActivity(page, size);
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, pageMap);
    }

    @ApiOperation("根据ID查看商品详情")
    @GetMapping("/detail/{product_id}")
    public JsonData detail(@ApiParam(value = "商品ID", required = true) @PathVariable("product_id") long productId){
        ProductDetailVO productDetailVO = productService.findDetailById(productId);
        if(Objects.isNull(productDetailVO)){
            return JsonData.returnJson(BizCodeEnum.PRODUCT_NOT_EXIT);
        }
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS,productDetailVO);
    }

    @ApiOperation("商品库存锁定")
    @PostMapping("lock_products")
    public JsonData lockProducts(@ApiParam(value = "商品库存锁定对象", required = true) @RequestBody LockProductDTO lockProductDTO){
        return productService.lockProductStock(lockProductDTO);
    }

}

