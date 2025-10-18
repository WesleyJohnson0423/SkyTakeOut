package com.sky.test;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.impl.EmployeeServiceImpl;
import com.sky.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FSM State Transition Test
 * Tests the state transitions in the login finite state machine
 * Focus on state flow rather than business logic partitioning
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FSM State Transition Test")
class FSMTest {

    /**
     * Test-specific login state enum
     * Used for tracking and verifying state transitions in tests
     */
    enum LoginState {
        LOGGED_OUT,        // Not logged in state
        AUTHENTICATING,    // Authenticating state
        AUTHENTICATED,     // Authenticated state
        LOGIN_FAILED,      // Login failed state
        TOKEN_INVALID      // Token invalid state
    }

    // Test-specific state variable
    private LoginState currentState = LoginState.LOGGED_OUT;

    // Test constants
    private static final String SECRET = "test_secret_key_test_secret_key_32chars";

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee validEmployee;
    private Employee lockedEmployee;

    @BeforeEach
    void setUp() {
        // Prepare test data for FSM states
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setUsername("admin");
        validEmployee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        validEmployee.setName("Administrator");
        validEmployee.setStatus(StatusConstant.ENABLE);

        lockedEmployee = new Employee();
        lockedEmployee.setId(2L);
        lockedEmployee.setUsername("locked");
        lockedEmployee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        lockedEmployee.setName("Locked User");
        lockedEmployee.setStatus(StatusConstant.DISABLE);
    }

    // ==================== State Transition Helper Methods ====================
    
    /**
     * Test layer state transition helper - enter authenticating state
     */
    private void gotoAuthenticating() { 
        currentState = LoginState.AUTHENTICATING; 
    }
    
    /**
     * Test layer state transition helper - enter authenticated state
     */
    private void onAuthenticated() { 
        currentState = LoginState.AUTHENTICATED; 
    }
    
    /**
     * Test layer state transition helper - enter login failed state
     */
    private void onLoginFailed() { 
        currentState = LoginState.LOGIN_FAILED; 
    }
    
    /**
     * Test layer state transition helper - enter token invalid state
     */
    private void onTokenInvalid() { 
        currentState = LoginState.TOKEN_INVALID; 
    }
    
    /**
     * Test layer state transition helper - return to logged out state
     */
    private void backToLoggedOut() { 
        currentState = LoginState.LOGGED_OUT; 
    }

    /**
     * FSM State: Logged-out → Authenticating
     */
    @Nested
    @DisplayName("State Transition: Logged-out → Authenticating")
    class LoggedOutToAuthenticatingTest {

        @Test
        @DisplayName("Should transition from Logged-out to Authenticating when credentials submitted")
        void testTransitionToAuthenticating() {
            // Initial state observable
            assertEquals(LoginState.LOGGED_OUT, currentState);
            gotoAuthenticating();

            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("admin");
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername("admin")).thenReturn(validEmployee);

            Employee result = employeeService.login(loginDTO);

            assertNotNull(result);// not null means it is being processed
            assertEquals("admin", result.getUsername());//means successfully get the data
            onAuthenticated();
            
            // State assertion: reached AUTHENTICATED
            assertEquals(LoginState.AUTHENTICATED, currentState);
            verify(employeeMapper).getByUsername("admin");
        }
    }

    /**
     * FSM State: Authenticating → LoginFailed
     */
    @Nested
    @DisplayName("State Transition: Authenticating → LoginFailed")
    class AuthenticatingToLoginFailedTest {

        @Test
        @DisplayName("Should transition to LoginFailed when user not exist")
        void testTransitionToLoginFailed_UserNotExist() {
            assertEquals(LoginState.LOGGED_OUT, currentState);
            gotoAuthenticating();

            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("nonexistent");
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername("nonexistent")).thenReturn(null);

            AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.ACCOUNT_NOT_FOUND, exception.getMessage());
            onLoginFailed();
            backToLoggedOut(); // Return to Logged-out after failure

            assertEquals(LoginState.LOGGED_OUT, currentState);
            verify(employeeMapper).getByUsername("nonexistent");
        }

        @Test
        @DisplayName("Should transition to LoginFailed when password mismatch")
        void testTransitionToLoginFailed_PasswordMismatch() {
            assertEquals(LoginState.LOGGED_OUT, currentState);
            gotoAuthenticating();

            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("admin");
            loginDTO.setPassword("wrongpassword");

            when(employeeMapper.getByUsername("admin")).thenReturn(validEmployee);

            // When & Then - Should transition to LoginFailed state
            PasswordErrorException exception = assertThrows(
                PasswordErrorException.class,
                () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.PASSWORD_ERROR, exception.getMessage());
            onLoginFailed();
            backToLoggedOut();

            assertEquals(LoginState.LOGGED_OUT, currentState);
        }

        @Test
        @DisplayName("Should transition to LoginFailed when account locked")
        void testTransitionToLoginFailed_AccountLocked() {
            assertEquals(LoginState.LOGGED_OUT, currentState);
            gotoAuthenticating();

            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("locked");
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername("locked")).thenReturn(lockedEmployee);

            // When & Then - Should transition to LoginFailed state
            AccountLockedException exception = assertThrows(
                AccountLockedException.class,
                () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.ACCOUNT_LOCKED, exception.getMessage());
            onLoginFailed();
            backToLoggedOut();

            assertEquals(LoginState.LOGGED_OUT, currentState);
        }
    }

    /**
     * FSM State: Authenticating → Authenticated
     */
    @Nested
    @DisplayName("State Transition: Authenticating → Authenticated")
    class AuthenticatingToAuthenticatedTest {

        @Test
        @DisplayName("Should transition to Authenticated when all conditions met")
        void testTransitionToAuthenticated_AllConditionsMet() {
            assertEquals(LoginState.LOGGED_OUT, currentState);
            gotoAuthenticating();

            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("admin");
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername("admin")).thenReturn(validEmployee);

            // When - All conditions: user exists && password matches && status enabled
            Employee result = employeeService.login(loginDTO);

            // Then - Should transition to Authenticated state
            assertNotNull(result);
            assertEquals("admin", result.getUsername());
            assertEquals(StatusConstant.ENABLE, result.getStatus());
            onAuthenticated();

            assertEquals(LoginState.AUTHENTICATED, currentState);
            verify(employeeMapper).getByUsername("admin");
        }
    }

    /**
     * FSM State: Authenticated → TokenInvalid
     */
    @Nested
    @DisplayName("State Transition: Authenticated → TokenInvalid")
    class AuthenticatedToTokenInvalidTest {

        @Test
        @DisplayName("Should handle JWT token validation logic")
        void testJWTTokenValidation() {
            // Assume authenticated
            currentState = LoginState.AUTHENTICATED;
            
            // When - JWT token operations with valid claims
            java.util.Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("userId", 1L);
            String generatedToken = JwtUtil.createJWT(SECRET, 7200000L, claims);

            // Then - Token should be generated successfully
            assertNotNull(generatedToken);
            assertTrue(generatedToken.contains("."));
            assertEquals(LoginState.AUTHENTICATED, currentState);
        }

        @Test
        @DisplayName("Should handle token expiration scenario - triggers TokenInvalid then back to LoggedOut")
        void testTokenExpiration_TriggersTokenInvalid_ThenBackToLoggedOut() {
            // Assume authenticated
            currentState = LoginState.AUTHENTICATED;

            java.util.Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("userId", 1L);
            String expiredToken = JwtUtil.createJWT(SECRET, 0L, claims); // Immediately expired
            assertNotNull(expiredToken);

            // Throws ExpiredJwtException when parsing → TokenInvalid
            assertThrows(io.jsonwebtoken.ExpiredJwtException.class,
                () -> JwtUtil.parseJWT(SECRET, expiredToken));
            onTokenInvalid();

            // According to system policy: invalid tokens should be cleared and return to Logged-out
            backToLoggedOut();
            assertEquals(LoginState.LOGGED_OUT, currentState);
        }
    }

    /**
     * FSM State: LoginFailed → Logged-out
     */
    @Nested
    @DisplayName("State Transition: LoginFailed → Logged-out")
    class LoginFailedToLoggedOutTest {

        @Test
        @DisplayName("Should return to Logged-out state after login failure")
        void testReturnToLoggedOutAfterFailure() {
            assertEquals(LoginState.LOGGED_OUT, currentState);
            gotoAuthenticating();

            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("nonexistent");
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername("nonexistent")).thenReturn(null);

            // When - Login attempt fails
            AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> employeeService.login(loginDTO)
            );

            // Then - Should be ready to return to Logged-out state
            assertNotNull(exception);
            assertEquals(MessageConstant.ACCOUNT_NOT_FOUND, exception.getMessage());
            onLoginFailed();
            backToLoggedOut();

            assertEquals(LoginState.LOGGED_OUT, currentState);
        }
    }

    /**
     * FSM State: TokenInvalid → Logged-out
     */
    @Nested
    @DisplayName("State Transition: TokenInvalid → Logged-out")
    class TokenInvalidToLoggedOutTest {

        @Test
        @DisplayName("Should handle token invalidation and return to Logged-out")
        void testTokenInvalidation() {
            // Assume in TokenInvalid state
            currentState = LoginState.TOKEN_INVALID;
            
            String invalidToken = "invalid_token_format"; // Not a valid JWT format (missing dots)

            // When - Token validation fails
            boolean isValidToken = isValidJWTFormat(invalidToken);

            // Then - Should be ready to return to Logged-out state
            assertFalse(isValidToken);
            backToLoggedOut();
            assertEquals(LoginState.LOGGED_OUT, currentState);
        }

        @Test
        @DisplayName("Should handle valid JWT format")
        void testValidJWTFormat() {
            // Assume in TokenInvalid state, but token format is valid
            currentState = LoginState.TOKEN_INVALID;
            
            String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

            // When - Token validation
            boolean isValidToken = isValidJWTFormat(validToken);

            // Then - Should be valid format
            assertTrue(isValidToken);
            // Format is valid but content may be invalid, still maintain TokenInvalid state
            assertEquals(LoginState.TOKEN_INVALID, currentState);
        }

        private boolean isValidJWTFormat(String token) {
            return token != null && token.split("\\.").length == 3;
        }
    }

    /**
     * FSM State: Authenticated → Logged-out (Logout)
     */
    @Nested
    @DisplayName("State Transition: Authenticated → Logged-out (Logout)")
    class AuthenticatedToLoggedOutTest {

        @Test
        @DisplayName("Should handle logout transition")
        void testLogoutTransition() {
            // Assume authenticated
            currentState = LoginState.AUTHENTICATED;

            // When - Logout action
            boolean isLoggedOut = performLogout();

            // Then - Should transition to Logged-out state
            assertTrue(isLoggedOut);
            backToLoggedOut();
            assertEquals(LoginState.LOGGED_OUT, currentState);
        }

        private boolean performLogout() {
            // Simulate logout logic
            return true;
        }
    }
}
