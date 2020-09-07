package com.atguigu.gmall.index.controller;

import com.atguigu.gamll.pms.entity.CategoryEntity;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.sun.security.auth.NTSid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @GetMapping("/index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLevelTwoCategoriesWithSub(@PathVariable("pid")Long pid){
        List<CategoryEntity> categoryEntities = indexService.queryLevelTwoCategoriesWithSub(pid);
        return ResponseVo.ok(categoryEntities);

    }

    @GetMapping("index/test/lock")
    @ResponseBody
    public ResponseVo<Object> testLock(){
        indexService.testLock();
        return ResponseVo.ok();
    }

    @GetMapping("index/test/read")
    @ResponseBody
    public ResponseVo<Object> testRead(){
        indexService.testRead();
        return ResponseVo.ok("读取成功！！！");
    }

    @GetMapping("index/test/write")
    @ResponseBody
    public ResponseVo<Object> testWrite(){
        indexService.testWrite();
        return ResponseVo.ok("写入成功！！！");
    }

    @GetMapping("index/test/countdown")
    @ResponseBody
    public ResponseVo<Object> testCoundDown(){
        indexService.testCoundDown();
        return ResponseVo.ok("出来了一位同学！！！");
    }

    @GetMapping("index/test/latch")
    @ResponseBody
    public ResponseVo<Object> testLatch(){
        indexService.testLatch();
        return ResponseVo.ok("班长锁门了！！！");
    }
}
