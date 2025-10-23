package com.sky.test;

import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.service.WeChatApiService;
import com.sky.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test cases for improved UserServiceImpl
 * Tests the newly testable functionality with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Test - Improved Testable Design")
class UserServiceImplImprovedTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private WeChatApiService weChatApiService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserLoginDTO validLoginDTO;
    private User existingUser;
    private User newUser;

    @BeforeEach
    void setUp() {
        // Prepare test data
        validLoginDTO = new UserLoginDTO();
        validLoginDTO.setCode("valid_wechat_code");

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setOpenid("existing_openid");
        existingUser.setCreateTime(LocalDateTime.now().minusDays(1));

        newUser = new User();
        newUser.setId(2L);
        newUser.setOpenid("new_openid");
        newUser.setCreateTime(LocalDateTime.now());
    }

    /**
     * Test successful login for existing user
     * Verifies that existing users are returned without creating new records
     */
    @Test
    @DisplayName("Should return existing user when user already exists")
    void testLogin_ExistingUser_Success() {
        // Given
        String openid = "existing_openid";
        when(weChatApiService.getOpenId("valid_wechat_code")).thenReturn(openid);
        when(userMapper.selectCountByOpenId(openid)).thenReturn(existingUser);

        // When
        User result = userService.login(validLoginDTO);

        // Then
        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getId());
        assertEquals(openid, result.getOpenid());
        
        // Verify interactions
        verify(weChatApiService).getOpenId("valid_wechat_code");
        verify(userMapper).selectCountByOpenId(openid);
        verify(userMapper, never()).saveNewUser(any(User.class));
    }

    /**
     * Test successful login for new user
     * Verifies that new users are automatically registered
     */
    @Test
    @DisplayName("Should create new user when user does not exist")
    void testLogin_NewUser_Success() {
        // Given
        String openid = "new_openid";
        when(weChatApiService.getOpenId("valid_wechat_code")).thenReturn(openid);
        when(userMapper.selectCountByOpenId(openid)).thenReturn(null);
        doNothing().when(userMapper).saveNewUser(any(User.class));

        // When
        User result = userService.login(validLoginDTO);

        // Then
        assertNotNull(result);
        assertEquals(openid, result.getOpenid());
        assertNotNull(result.getCreateTime());
        
        // Verify interactions
        verify(weChatApiService).getOpenId("valid_wechat_code");
        verify(userMapper).selectCountByOpenId(openid);
        verify(userMapper).saveNewUser(any(User.class));
    }

    /**
     * Test login failure when WeChat API returns null openid
     * Verifies proper exception handling for invalid WeChat responses
     */
    @Test
    @DisplayName("Should throw LoginFailedException when WeChat API returns null openid")
    void testLogin_NullOpenId_ThrowsException() {
        // Given
        when(weChatApiService.getOpenId("invalid_code")).thenReturn(null);
        validLoginDTO.setCode("invalid_code");

        // When & Then
        LoginFailedException exception = assertThrows(
                LoginFailedException.class,
                () -> userService.login(validLoginDTO)
        );

        assertEquals(MessageConstant.LOGIN_FAILED, exception.getMessage());
        
        // Verify interactions
        verify(weChatApiService).getOpenId("invalid_code");
        verify(userMapper, never()).selectCountByOpenId(anyString());
        verify(userMapper, never()).saveNewUser(any(User.class));
    }

    /**
     * Test login failure when WeChat API throws exception
     * Verifies proper exception handling for WeChat API failures
     */
    @Test
    @DisplayName("Should handle WeChat API exceptions properly")
    void testLogin_WeChatApiException_Handled() {
        // Given
        validLoginDTO.setCode("error_code");
        when(weChatApiService.getOpenId("error_code"))
                .thenThrow(new RuntimeException("WeChat API unavailable"));

        // When & Then
        assertThrows(
                RuntimeException.class,
                () -> userService.login(validLoginDTO)
        );

        // Verify interactions
        verify(weChatApiService).getOpenId("error_code");
        verify(userMapper, never()).selectCountByOpenId(anyString());
        verify(userMapper, never()).saveNewUser(any(User.class));
    }

    /**
     * Test edge case: empty openid string
     * Verifies handling of empty string responses from WeChat API
     */
    @Test
    @DisplayName("Should throw LoginFailedException when WeChat API returns empty openid")
    void testLogin_EmptyOpenId_ThrowsException() {
        // Given
        when(weChatApiService.getOpenId("empty_code")).thenReturn("");
        validLoginDTO.setCode("empty_code");

        // When & Then
        LoginFailedException exception = assertThrows(
                LoginFailedException.class,
                () -> userService.login(validLoginDTO)
        );

        assertEquals(MessageConstant.LOGIN_FAILED, exception.getMessage());
    }

    /**
     * Test database interaction verification
     * Verifies that database operations are called with correct parameters
     */
    @Test
    @DisplayName("Should call database methods with correct parameters")
    void testLogin_DatabaseInteraction_Verified() {
        // Given
        String openid = "test_openid";
        when(weChatApiService.getOpenId("test_code")).thenReturn(openid);
        when(userMapper.selectCountByOpenId(openid)).thenReturn(null);
        doNothing().when(userMapper).saveNewUser(any(User.class));
        validLoginDTO.setCode("test_code");

        // When
        userService.login(validLoginDTO);

        // Then
        verify(userMapper).selectCountByOpenId(openid);
        verify(userMapper).saveNewUser(argThat(user -> 
            openid.equals(user.getOpenid()) && 
            user.getCreateTime() != null
        ));
    }

    /**
     * Test performance: verify minimal external calls
     * Ensures that WeChat API is called only once per login attempt
     */
    @Test
    @DisplayName("Should call WeChat API only once per login attempt")
    void testLogin_WeChatApiCalledOnce() {
        // Given
        String openid = "performance_test_openid";
        when(weChatApiService.getOpenId("performance_code")).thenReturn(openid);
        when(userMapper.selectCountByOpenId(openid)).thenReturn(existingUser);
        validLoginDTO.setCode("performance_code");

        // When
        userService.login(validLoginDTO);

        // Then
        verify(weChatApiService, times(1)).getOpenId("performance_code");
        verifyNoMoreInteractions(weChatApiService);
    }
}
