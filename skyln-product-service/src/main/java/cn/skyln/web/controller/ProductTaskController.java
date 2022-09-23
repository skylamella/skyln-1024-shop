package cn.skyln.web.controller;


import cn.skyln.web.service.ProductTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author skylamella
 * @since 2022-09-19
 */
@RestController
@RequestMapping("/productTaskDO")
public class ProductTaskController {
    @Autowired
    private ProductTaskService productTaskService;
}

