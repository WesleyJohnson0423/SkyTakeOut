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
 * Admin Employee Login Partition Testing
 * Partition testing for different business scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Admin Employee Login Test")
class EmployeeLoginTest {

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeLoginDTO validLoginDTO;
    private Employee validEmployee;
    private Employee validEmployee_casesensative;
    private Employee ValidEmployee_unicode;

    @BeforeEach
    void setUp() {
        // Prepare test data
        validLoginDTO = new EmployeeLoginDTO();
        validLoginDTO.setUsername("admin");
        validLoginDTO.setPassword("123456");

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setUsername("admin");
        validEmployee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        validEmployee.setName("Administrator");
        validEmployee.setStatus(StatusConstant.ENABLE);

        validEmployee_casesensative = new Employee();
        validEmployee_casesensative.setId(2L);
        validEmployee_casesensative.setUsername("admin2");
        validEmployee_casesensative.setPassword(DigestUtils.md5DigestAsHex("rightpassword".getBytes(StandardCharsets.UTF_8)));
        validEmployee_casesensative.setName("Administrator_2");
        validEmployee_casesensative.setStatus(StatusConstant.ENABLE);

        ValidEmployee_unicode = new Employee();
        ValidEmployee_unicode.setId(3L);
        ValidEmployee_unicode.setUsername("测试用户");
        ValidEmployee_unicode.setPassword(DigestUtils.md5DigestAsHex("测试密码".getBytes(StandardCharsets.UTF_8)));
        ValidEmployee_unicode.setName("Administrator_3");
        ValidEmployee_unicode.setStatus(StatusConstant.ENABLE);


    }

    /**
     * Partition 1: Account Not Exists Test
     */
    @Nested
    @DisplayName("Account Not Exists Test Partition")
    class AccountNotExistsTest {

        @Test
        @DisplayName("Test account not exists - should throw AccountNotFoundException")
        void testLoginWithNonExistentAccount() {
            // Given
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("nonexistent");
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername("nonexistent")).thenReturn(null);

            // When & Then
            AccountNotFoundException exception = assertThrows(
                    AccountNotFoundException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.ACCOUNT_NOT_FOUND, exception.getMessage());
            verify(employeeMapper).getByUsername("nonexistent");
        }

        @Test
        @DisplayName("Test empty username - should throw AccountNotFoundException")
        void testLoginWithEmptyUsername() {
            // Given
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("");
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername("")).thenReturn(null);

            // When & Then
            AccountNotFoundException exception = assertThrows(
                    AccountNotFoundException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.ACCOUNT_NOT_FOUND, exception.getMessage());
        }

        @Test
        @DisplayName("Test null username - should throw AccountNotFoundException")
        void testLoginWithNullUsername() {
            // Given
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername(null);
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername(null)).thenReturn(null);

            // When & Then
            AccountNotFoundException exception = assertThrows(
                    AccountNotFoundException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.ACCOUNT_NOT_FOUND, exception.getMessage());
        }

        @Test
        @DisplayName("Test special characters username - should throw AccountNotFoundException")
        void testLoginWithSpecialCharactersUsername() {
            // Given
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("admin@#$%");
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername("admin@#$%")).thenReturn(null);

            // When & Then
            AccountNotFoundException exception = assertThrows(
                    AccountNotFoundException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.ACCOUNT_NOT_FOUND, exception.getMessage());
        }
    }

    /**
     * Partition 2: Wrong Password Test
     */
    @Nested
    @DisplayName("Wrong Password Test Partition")
    class WrongPasswordTest {

        @Test
        @DisplayName("Test wrong password - should throw PasswordErrorException")
        void testLoginWithWrongPassword() {
            // Given
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("admin");
            loginDTO.setPassword("wrongpassword");

            when(employeeMapper.getByUsername("admin")).thenReturn(validEmployee);

            // When & Then
            PasswordErrorException exception = assertThrows(
                    PasswordErrorException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.PASSWORD_ERROR, exception.getMessage());
            verify(employeeMapper).getByUsername("admin");
        }

        @Test
        @DisplayName("Test empty password - should throw PasswordErrorException")
        void testLoginWithEmptyPassword() {
            // Given
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("admin");
            loginDTO.setPassword("");

            when(employeeMapper.getByUsername("admin")).thenReturn(validEmployee);

            // When & Then
            PasswordErrorException exception = assertThrows(
                    PasswordErrorException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.PASSWORD_ERROR, exception.getMessage());
        }

        @Test
        @DisplayName("Test null password - should throw NullPointerException")
        void testLoginWithNullPassword() {
            // Given
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("admin");
            loginDTO.setPassword(null);

            when(employeeMapper.getByUsername("admin")).thenReturn(validEmployee);

            // When & Then
            // Note: null password will cause NullPointerException because of calling getBytes() method
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertNotNull(exception);
        }

        @Test
        @DisplayName("Test case sensitive password - should throw PasswordErrorException")
        void testLoginWithCaseSensitivePassword() {
            // Given
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("admin2");
            loginDTO.setPassword("RIGHTPASSWORD"); // Completely different password

            when(employeeMapper.getByUsername("admin2")).thenReturn(validEmployee_casesensative);

            // When & Then
            PasswordErrorException exception = assertThrows(
                    PasswordErrorException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.PASSWORD_ERROR, exception.getMessage());
        }

        @Test
        @DisplayName("Test password containing spaces - should throw PasswordErrorException")
        void testLoginWithPasswordContainingSpaces() {
            // Given
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("admin");
            loginDTO.setPassword(" 123456 "); // Password with spaces

            when(employeeMapper.getByUsername("admin")).thenReturn(validEmployee);

            // When & Then
            PasswordErrorException exception = assertThrows(
                    PasswordErrorException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.PASSWORD_ERROR, exception.getMessage());
        }
    }

    /**
     * Partition 3: Account Locked Test
     */
    @Nested
    @DisplayName("Account Locked Test Partition")
    class AccountLockedTest {

        @Test
        @DisplayName("Test account locked - should throw AccountLockedException")
        void testLoginWithLockedAccount() {
            // Given
            Employee lockedEmployee = new Employee();
            lockedEmployee.setId(2L);
            lockedEmployee.setUsername("lockeduser");
            lockedEmployee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
            lockedEmployee.setName("Locked User");
            lockedEmployee.setStatus(StatusConstant.DISABLE); // Account disabled

            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("lockeduser");
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername("lockeduser")).thenReturn(lockedEmployee);

            // When & Then
            AccountLockedException exception = assertThrows(
                    AccountLockedException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.ACCOUNT_LOCKED, exception.getMessage());
            verify(employeeMapper).getByUsername("lockeduser");
        }

        @Test
        @DisplayName("Test active status account - should login successfully")
        void testLoginWithActiveStatusAccount() {
            // Given
            Employee activeEmployee = new Employee();
            activeEmployee.setId(4L);
            activeEmployee.setUsername("activeuser");
            activeEmployee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
            activeEmployee.setName("Active User");
            activeEmployee.setStatus(StatusConstant.ENABLE); // Account enabled

            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("activeuser");
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername("activeuser")).thenReturn(activeEmployee);

            // When
            Employee result = employeeService.login(loginDTO);

            // Then
            assertNotNull(result);
            assertEquals(4L, result.getId());
            assertEquals("activeuser", result.getUsername());
            assertEquals("Active User", result.getName());
            assertEquals(StatusConstant.ENABLE, result.getStatus());
            verify(employeeMapper).getByUsername("activeuser");
        }

    }

    /**
     * Partition 4: Valid Login Test
     */
    @Nested
    @DisplayName("Valid Login Test Partition")
    class ValidLoginTest {

        @Test
        @DisplayName("Test valid login - should return Employee object")
        void testLoginWithValidCredentials() {
            // Given
            when(employeeMapper.getByUsername("admin")).thenReturn(validEmployee);

            // When
            Employee result = employeeService.login(validLoginDTO);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("admin", result.getUsername());
            assertEquals("Administrator", result.getName());
            assertEquals(StatusConstant.ENABLE, result.getStatus());
            verify(employeeMapper).getByUsername("admin");
        }

        @Test
        @DisplayName("Test password MD5 encryption correctness")
        void testPasswordEncryption() {
            // Given
            String originalPassword = "123456";
            String expectedEncryptedPassword = DigestUtils.md5DigestAsHex(originalPassword.getBytes(StandardCharsets.UTF_8));

            Employee employeeWithEncryptedPassword = new Employee();
            employeeWithEncryptedPassword.setId(1L);
            employeeWithEncryptedPassword.setUsername("admin");
            employeeWithEncryptedPassword.setPassword(expectedEncryptedPassword);
            employeeWithEncryptedPassword.setName("Administrator");
            employeeWithEncryptedPassword.setStatus(StatusConstant.ENABLE);

            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("admin");
            loginDTO.setPassword(originalPassword);

            when(employeeMapper.getByUsername("admin")).thenReturn(employeeWithEncryptedPassword);

            // When
            Employee result = employeeService.login(loginDTO);

            // Then
            assertNotNull(result);
            assertEquals(expectedEncryptedPassword, result.getPassword());
        }

        @Test
        @DisplayName("Test login with different valid usernames")
        void testLoginWithDifferentValidUsernames() {
            // Given
            Employee differentEmployee = new Employee();
            differentEmployee.setId(2L);
            differentEmployee.setUsername("testuser");
            differentEmployee.setPassword(DigestUtils.md5DigestAsHex("testpass".getBytes(StandardCharsets.UTF_8)));
            differentEmployee.setName("Test User");
            differentEmployee.setStatus(StatusConstant.ENABLE);

            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("testuser");
            loginDTO.setPassword("testpass");

            when(employeeMapper.getByUsername("testuser")).thenReturn(differentEmployee);

            // When
            Employee result = employeeService.login(loginDTO);

            // Then
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertEquals("Test User", result.getName());
        }
    }

    /**
     * Partition 5: Edge Case Test
     */
    @Nested
    @DisplayName("Edge Case Test Partition")
    class EdgeCaseTest {

        @Test
        @DisplayName("Test very long username")
        void testLoginWithVeryLongUsername() {
            // Given
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("a");
            }
            String longUsername = sb.toString(); // 1000 character username
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername(longUsername);
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername(longUsername)).thenReturn(null);

            // When & Then
            AccountNotFoundException exception = assertThrows(
                    AccountNotFoundException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.ACCOUNT_NOT_FOUND, exception.getMessage());
        }

        @Test
        @DisplayName("Test very long password")
        void testLoginWithVeryLongPassword() {
            // Given
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("a");
            }
            String longPassword = sb.toString(); // 1000 character password
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("admin");
            loginDTO.setPassword(longPassword);

            when(employeeMapper.getByUsername("admin")).thenReturn(validEmployee);

            // When & Then
            PasswordErrorException exception = assertThrows(
                    PasswordErrorException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.PASSWORD_ERROR, exception.getMessage());
        }

        @Test
        @DisplayName("Test login with special characters password - should succeed when correct")
        void testLoginWithSpecialCharactersPassword() {
            // Given - 创建一个密码就是特殊字符的员工
            String specialPassword = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            Employee employeeWithSpecialPassword = new Employee();
            employeeWithSpecialPassword.setId(5L);
            employeeWithSpecialPassword.setUsername("specialuser");
            employeeWithSpecialPassword.setPassword(DigestUtils.md5DigestAsHex(specialPassword.getBytes(StandardCharsets.UTF_8)));
            employeeWithSpecialPassword.setName("Special Password User");
            employeeWithSpecialPassword.setStatus(StatusConstant.ENABLE);

            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("specialuser");
            loginDTO.setPassword(specialPassword); // 使用相同的特殊字符密码

            when(employeeMapper.getByUsername("specialuser")).thenReturn(employeeWithSpecialPassword);

            // When
            Employee result = employeeService.login(loginDTO);

            // Then - 应该成功登录！
            assertNotNull(result);
            assertEquals("specialuser", result.getUsername());
            assertEquals(StatusConstant.ENABLE, result.getStatus());
            verify(employeeMapper).getByUsername("specialuser");
        }

        @Test
        @DisplayName("Test login with Unicode username and password")
        void testLoginWithUnicodeCharactersAllowed() {
            // Given
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("测试用户");
            loginDTO.setPassword("测试密码");

            when(employeeMapper.getByUsername("测试用户")).thenReturn(ValidEmployee_unicode);

            // When
            Employee result = employeeService.login(loginDTO);

            // Then
            assertNotNull(result);
            assertEquals("测试用户", result.getUsername());
            verify(employeeMapper).getByUsername("测试用户");
        }



        @Test
        @DisplayName("Test numeric username")
        void testLoginWithNumericUsername() {
            // Given
            EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
            loginDTO.setUsername("123456");
            loginDTO.setPassword("123456");

            when(employeeMapper.getByUsername("123456")).thenReturn(null);

            // When & Then
            AccountNotFoundException exception = assertThrows(
                    AccountNotFoundException.class,
                    () -> employeeService.login(loginDTO)
            );

            assertEquals(MessageConstant.ACCOUNT_NOT_FOUND, exception.getMessage());
            verify(employeeMapper).getByUsername("123456");
        }
    }
}