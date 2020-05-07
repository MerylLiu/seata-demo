package com.swy.test3.controller;


import com.jds.core.actionResult.Json;
import com.jds.core.controller.JdsController;
import com.swy.test3.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 消费收银
 */

@Controller
@RequestMapping("/")
public class HomeController extends JdsController {
    @Autowired
    private TestService testService;

    @RequestMapping("/")
    public Json index() {
        String sentence = "黄色，武器，天安门，春天在哪里,回民吃猪肉";
        return json(sentence);
    }

    @RequestMapping("save")
    public Json save() {
        testService.txTest3();
        return json("success");
    }

    @RequestMapping("test")
    public Json test() {
        testService.test3();
        return json("success");
    }
}
