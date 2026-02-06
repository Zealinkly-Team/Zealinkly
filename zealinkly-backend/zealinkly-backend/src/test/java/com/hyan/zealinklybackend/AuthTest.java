package com.hyan.zealinklybackend;

import com.hyan.zealinklybackend.dto.request.LoginRequest;
import com.hyan.zealinklybackend.dto.request.RegisterRequest;
import com.hyan.zealinklybackend.dto.response.LoginResponse;
import com.hyan.zealinklybackend.dto.response.RegisterResponse;
import com.hyan.zealinklybackend.repository.ElderRepository;
import com.hyan.zealinklybackend.repository.VolunteerRepository;
import com.hyan.zealinklybackend.repository.AdminRepository;
import com.hyan.zealinklybackend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 注册和登录功能测试
 */
@SpringBootTest
@Transactional
class AuthTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ElderRepository elderRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @BeforeEach
    void setUp() {
        // 清理测试数据（如果存在）
        elderRepository.findByUsername("testelder").ifPresent(elderRepository::delete);
        volunteerRepository.findByUsername("testvolunteer").ifPresent(volunteerRepository::delete);
        adminRepository.findByUsername("testadmin").ifPresent(adminRepository::delete);
    }

    /**
     * 测试注册老人
     */
    @Test
    void testRegisterElder() {
        System.out.println("\n==========================================");
        System.out.println("测试：注册老人");
        System.out.println("==========================================");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testelder");
        request.setPassword("123456");
        request.setRealName("测试老人");
        request.setPhone("13800138000");
        request.setAddress("测试小区1号楼");

        RegisterResponse response = authService.registerElder(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("testelder", response.getUsername());
        assertEquals("ELDER", response.getUserType());

        System.out.println("✅ 注册成功！");
        System.out.println("用户ID: " + response.getId());
        System.out.println("用户名: " + response.getUsername());
        System.out.println("用户类型: " + response.getUserType());
        System.out.println("==========================================\n");
    }

    /**
     * 测试注册志愿者
     */
    @Test
    void testRegisterVolunteer() {
        System.out.println("\n==========================================");
        System.out.println("测试：注册志愿者");
        System.out.println("==========================================");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testvolunteer");
        request.setPassword("123456");
        request.setRealName("测试志愿者");
        request.setPhone("13800138001");

        RegisterResponse response = authService.registerVolunteer(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("testvolunteer", response.getUsername());
        assertEquals("VOLUNTEER", response.getUserType());

        System.out.println("✅ 注册成功！");
        System.out.println("用户ID: " + response.getId());
        System.out.println("用户名: " + response.getUsername());
        System.out.println("用户类型: " + response.getUserType());
        System.out.println("==========================================\n");
    }

    /**
     * 测试注册管理员
     */
    @Test
    void testRegisterAdmin() {
        System.out.println("\n==========================================");
        System.out.println("测试：注册管理员");
        System.out.println("==========================================");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testadmin");
        request.setPassword("123456");
        request.setRealName("测试管理员");
        request.setRoleLevel(1);

        RegisterResponse response = authService.registerAdmin(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("testadmin", response.getUsername());
        assertEquals("ADMIN", response.getUserType());

        System.out.println("✅ 注册成功！");
        System.out.println("用户ID: " + response.getId());
        System.out.println("用户名: " + response.getUsername());
        System.out.println("用户类型: " + response.getUserType());
        System.out.println("==========================================\n");
    }

    /**
     * 测试用户名重复注册
     */
    @Test
    void testRegisterDuplicateUsername() {
        System.out.println("\n==========================================");
        System.out.println("测试：用户名重复注册（应该失败）");
        System.out.println("==========================================");

        // 先注册一个老人
        RegisterRequest request1 = new RegisterRequest();
        request1.setUsername("duplicateuser");
        request1.setPassword("123456");
        authService.registerElder(request1);

        // 尝试用相同用户名注册志愿者（应该失败）
        RegisterRequest request2 = new RegisterRequest();
        request2.setUsername("duplicateuser");
        request2.setPassword("123456");

        Exception exception = assertThrows(Exception.class, () -> {
            authService.registerVolunteer(request2);
        });

        assertTrue(exception.getMessage().contains("用户名已存在"));
        System.out.println("✅ 正确拒绝重复用户名注册");
        System.out.println("错误信息: " + exception.getMessage());
        System.out.println("==========================================\n");
    }

    /**
     * 测试登录老人
     */
    @Test
    void testLoginElder() {
        System.out.println("\n==========================================");
        System.out.println("测试：登录老人");
        System.out.println("==========================================");

        // 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("loginelder");
        registerRequest.setPassword("123456");
        authService.registerElder(registerRequest);

        // 登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginelder");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("ELDER");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("loginelder", response.getUsername());
        assertEquals("ELDER", response.getUserType());
        assertNotNull(response.getUserId());

        System.out.println("✅ 登录成功！");
        System.out.println("Token: " + response.getToken().substring(0, Math.min(50, response.getToken().length())) + "...");
        System.out.println("用户ID: " + response.getUserId());
        System.out.println("用户名: " + response.getUsername());
        System.out.println("用户类型: " + response.getUserType());
        System.out.println("==========================================\n");
    }

    /**
     * 测试登录志愿者
     */
    @Test
    void testLoginVolunteer() {
        System.out.println("\n==========================================");
        System.out.println("测试：登录志愿者");
        System.out.println("==========================================");

        // 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("loginvolunteer");
        registerRequest.setPassword("123456");
        authService.registerVolunteer(registerRequest);

        // 登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginvolunteer");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("VOLUNTEER");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("loginvolunteer", response.getUsername());
        assertEquals("VOLUNTEER", response.getUserType());

        System.out.println("✅ 登录成功！");
        System.out.println("Token: " + response.getToken().substring(0, Math.min(50, response.getToken().length())) + "...");
        System.out.println("用户ID: " + response.getUserId());
        System.out.println("用户名: " + response.getUsername());
        System.out.println("用户类型: " + response.getUserType());
        System.out.println("==========================================\n");
    }

    /**
     * 测试登录管理员
     */
    @Test
    void testLoginAdmin() {
        System.out.println("\n==========================================");
        System.out.println("测试：登录管理员");
        System.out.println("==========================================");

        // 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("loginadmin");
        registerRequest.setPassword("123456");
        authService.registerAdmin(registerRequest);

        // 登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginadmin");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("ADMIN");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("loginadmin", response.getUsername());
        assertEquals("ADMIN", response.getUserType());

        System.out.println("✅ 登录成功！");
        System.out.println("Token: " + response.getToken().substring(0, Math.min(50, response.getToken().length())) + "...");
        System.out.println("用户ID: " + response.getUserId());
        System.out.println("用户名: " + response.getUsername());
        System.out.println("用户类型: " + response.getUserType());
        System.out.println("==========================================\n");
    }

    /**
     * 测试错误密码登录（应该失败）
     */
    @Test
    void testLoginWithWrongPassword() {
        System.out.println("\n==========================================");
        System.out.println("测试：错误密码登录（应该失败）");
        System.out.println("==========================================");

        // 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("wrongpassuser");
        registerRequest.setPassword("123456");
        authService.registerElder(registerRequest);

        // 用错误密码登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrongpassuser");
        loginRequest.setPassword("wrongpassword");
        loginRequest.setUserType("ELDER");

        Exception exception = assertThrows(Exception.class, () -> {
            authService.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("用户名或密码错误"));
        System.out.println("✅ 正确拒绝错误密码");
        System.out.println("错误信息: " + exception.getMessage());
        System.out.println("==========================================\n");
    }

    /**
     * 测试不存在的用户登录（应该失败）
     */
    @Test
    void testLoginNonExistentUser() {
        System.out.println("\n==========================================");
        System.out.println("测试：不存在用户登录（应该失败）");
        System.out.println("==========================================");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("ELDER");

        Exception exception = assertThrows(Exception.class, () -> {
            authService.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("用户名或密码错误"));
        System.out.println("✅ 正确拒绝不存在用户");
        System.out.println("错误信息: " + exception.getMessage());
        System.out.println("==========================================\n");
    }

    /**
     * 综合测试：注册 + 登录完整流程
     */
    @Test
    void testRegisterAndLoginFlow() {
        System.out.println("\n==========================================");
        System.out.println("综合测试：注册 + 登录完整流程");
        System.out.println("==========================================");

        // 1. 注册老人
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("flowtest");
        registerRequest.setPassword("123456");
        registerRequest.setRealName("流程测试用户");
        registerRequest.setPhone("13800138002");
        registerRequest.setAddress("测试地址");

        RegisterResponse registerResponse = authService.registerElder(registerRequest);
        System.out.println("✅ 步骤1：注册成功");
        System.out.println("   用户ID: " + registerResponse.getId());
        System.out.println("   用户名: " + registerResponse.getUsername());

        // 2. 登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("flowtest");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("ELDER");

        LoginResponse loginResponse = authService.login(loginRequest);
        System.out.println("✅ 步骤2：登录成功");
        System.out.println("   Token: " + loginResponse.getToken().substring(0, Math.min(50, loginResponse.getToken().length())) + "...");
        System.out.println("   用户ID: " + loginResponse.getUserId());

        // 验证数据一致性
        assertEquals(registerResponse.getId(), loginResponse.getUserId());
        assertEquals(registerResponse.getUsername(), loginResponse.getUsername());

        System.out.println("✅ 步骤3：数据一致性验证通过");
        System.out.println("==========================================\n");
    }
}
