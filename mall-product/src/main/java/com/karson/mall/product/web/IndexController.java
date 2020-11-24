package com.karson.mall.product.web;

import com.karson.mall.product.entity.CategoryEntity;
import com.karson.mall.product.service.CategoryService;
import com.karson.mall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author Karson
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        //1.查出所有的1级分类
        List<CategoryEntity> entityList = categoryService.getLevel1Category();

        model.addAttribute("categories", entityList);
        //视图解析器进行拼串
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        return categoryService.getCatalogJson();
    }
}
