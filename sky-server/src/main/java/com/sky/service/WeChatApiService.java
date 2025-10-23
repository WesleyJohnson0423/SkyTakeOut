package com.sky.service;

/**
 * WeChat API Service Interface
 * Provides abstraction for WeChat API calls to enable testability
 */
public interface WeChatApiService {
    
    /**
     * Get WeChat user openid by authorization code
     * @param code WeChat authorization code
     * @return WeChat user openid
     * @throws RuntimeException if API call fails
     */
    String getOpenId(String code);
}
