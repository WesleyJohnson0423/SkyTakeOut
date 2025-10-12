package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "用户相关接口")
@RequestMapping("/user/user")
@Slf4j
@RestController
public class UserController {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> Login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("用户登陆：{}",userLoginDTO);
        // 获取到用户的Openid
        User user = userService.login(userLoginDTO);
        // 构建JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey()
                , jwtProperties.getUserTtl()
                , claims);
        // 构建vo对象
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .token(token)
                .openid(user.getOpenid())
                .build();
        return Result.success(userLoginVO);
    }

    /**
     * 用户登出
     * @return
     */
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<String> logout(){
        return Result.success(MessageConstant.EXIT_OK);
    }


}
