package com.atguigu.gmall.index.controller;

import com.atguigu.gamll.pms.entity.CategoryEntity;
import com.atguigu.gmall.index.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping
    public String toIndex(Model model){
       List<CategoryEntity> categories = indexService.queryLevelOneCategories();
       model.addAttribute("categories",categories);
       return "index";
    }
}
