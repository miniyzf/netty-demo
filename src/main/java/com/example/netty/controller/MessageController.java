package com.example.netty.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.netty.common.Common;
import com.example.netty.utils.UuidUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 *
 * @author Administrator
 */
@Controller
public class MessageController {

    @RequestMapping("/index")
    public ModelAndView  index(){
        ModelAndView mav=new ModelAndView("/webSocketDemo");
        String uid = UuidUtil.get6NumberUUID(6);
        mav.addObject("uid", uid);
        mav.addObject("indexType",0);
        mav.addObject("wsUrl", Common.WEBSOCKET_Client_URL);
        return mav;
    }
    @RequestMapping("/send")
    public ModelAndView  indexSend(){
        ModelAndView mav=new ModelAndView("/webSocketDemo");
        String uid = UuidUtil.get6NumberUUID(6);
        mav.addObject("uid", uid);
        mav.addObject("indexType",1);
        mav.addObject("wsUrl", Common.WEBSOCKET_Client_URL);
        return mav;
    }

    @RequestMapping("/demo")
    public ModelAndView  demo(){
        ModelAndView mav=new ModelAndView("/webRTCDemo");
        mav.addObject("wsUrl", Common.WEBSOCKET_Client_URL);
        return mav;
    }

    @RequestMapping("/test")
    public ModelAndView test(){
        ModelAndView mav = new ModelAndView("/simpleWebRTCDemo");
        mav.addObject("wsUrl", Common.WEBSOCKET_Client_URL);
        return mav;
    }

    @RequestMapping("/socket.io")
    public @ResponseBody String goSocket(HttpServletRequest request){
        // {"EIO":["3"],"transport":["polling"],"t":["1573804581100-1925"]}
        Map map = request.getParameterMap();
        JSONObject object = new JSONObject(map);
        //System.out.println(object.toJSONString());
        return object.toJSONString();
    }

}
