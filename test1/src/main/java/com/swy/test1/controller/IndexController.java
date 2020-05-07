package com.swy.test1.controller;


import com.jds.core.actionResult.Json;
import com.jds.core.controller.JdsController;
import com.swy.test1.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class IndexController extends JdsController {
    @Autowired
    private TestService testService;

    @RequestMapping("/")
    public Json index() {
        String sentence = "黄色，武器，天安门，春天在哪里,回民吃猪肉";
        return json(null);
    }

    @RequestMapping("/save")
    public Json save() {
        testService.txTest();
        return json(null);
    }

    @RequestMapping("/test")
    public Json saveTest() {
        testService.test();
        return json(null);
    }
}
