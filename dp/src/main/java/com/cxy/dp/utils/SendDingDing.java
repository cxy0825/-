package com.cxy.dp.utils;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Slf4j
public class SendDingDing {
    private static final String keyWord = "验证码";
    private static final String url =
            "https://oapi.dingtalk.com/robot/send?access_token=8a3fd0d74366f1fe5616a6043b7cefb686be69f76db57152703a655cd9c08ee3";

    public static JSONObject sendMsg(String msg) {
        //    log.info("运行了");
        RestTemplate restTemplate = new RestTemplate();
        restTemplate
                .getMessageConverters()
                .set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        JSONObject data = new JSONObject();
        JSONObject text = new JSONObject();
        text.put("content", keyWord + "\n" + msg);
        data.put("msgtype", "text");
        data.put("text", text);
        String resultStr = restTemplate.postForObject(url, data, String.class);
        log.info(resultStr);
        return JSON.parseObject(resultStr);
    }


}
