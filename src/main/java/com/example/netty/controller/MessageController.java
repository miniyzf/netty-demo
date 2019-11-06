package com.example.netty.controller;

import com.example.netty.utils.UuidUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Administrator
 */
@Controller
public class MessageController {

    @RequestMapping("/index")
    public ModelAndView  index(){
        ModelAndView mav=new ModelAndView("/nettySocket");
        String uid = UuidUtil.get6NumberUUID(6);
        mav.addObject("uid", uid);
        return mav;
    }

}
