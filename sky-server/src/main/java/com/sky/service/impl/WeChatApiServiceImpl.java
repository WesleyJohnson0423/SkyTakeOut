package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.WeChatProperties;
import com.sky.service.WeChatApiService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Production implementation of WeChat API Service
 * Makes real HTTP calls to WeChat API
 */
@Service
public class WeChatApiServiceImpl implements WeChatApiService {

    @Autowired
    private WeChatProperties weChatProperties;

    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Override
    public String getOpenId(String code) {
        Map<String, String> map = new HashMap<>();
        map.put("appId", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        
        return openid;
    }
}
