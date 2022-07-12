package com.eachnow.linebot.domain.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class PageController {

    @RequestMapping("/linebot/notify/subscribe")
    public ModelAndView lineNotifySubscribeSuccessPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("remind/success.html");
        return modelAndView;
    }
}
