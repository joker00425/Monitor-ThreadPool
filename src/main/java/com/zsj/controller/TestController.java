package com.zsj.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class TestController {
    @Value( "${server.port}" )
    String port;

    // 当num=1时，不报异常。当num!=1时，会报异常,测试cat配置是否正常
    @RequestMapping(value = "printnum")
    public Map login(@RequestParam("num") Integer num) {

        Map<String, Object> map = new HashMap<>();
        if (num == 1) {
            map.put("num", 1);
        } else {
            map.put("num", num / 0);
        }
        return map;
    }



    @GetMapping("hi")
    public String hi(String name) {

        return "hi " + name + " ,i am from port:" + port;
    }
}
