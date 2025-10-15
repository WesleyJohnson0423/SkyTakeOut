package com.sky.test;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.common.utils.HttpUtil;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

//@SpringBootTest
public class httpClientTest {

    @Test
    public void GetTest() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 构造请求对象
        HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");
        // 发送请求
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        // 解析响应结果
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        System.out.println("响应码：" + statusCode);
        HttpEntity entity = httpResponse.getEntity();
        String s = EntityUtils.toString(entity);
        System.out.println("相应回来的数据：" + s);
        // 关闭资源
        httpClient.close();
        httpResponse.close();
    }

    @Test
    public void testPost() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 构造请求对象
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");
        // 设置发送内容
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username","admin");
        jsonObject.put("password","123456");

        StringEntity entity = new StringEntity(jsonObject.toString());
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        // 发送请求
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        // 解析响应结果
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        System.out.println("响应码：" + statusCode);
        HttpEntity httpEntity = httpResponse.getEntity();
        String s = EntityUtils.toString(httpEntity);
        System.out.println("相应回来的数据：" + s);
        // 关闭资源
        httpClient.close();
        httpResponse.close();
    }

}
