package com.hyan.zealinklybackend;

import com.hyan.zealinklybackend.dto.request.LoginRequest;
import com.hyan.zealinklybackend.dto.request.RegisterRequest;
import com.hyan.zealinklybackend.dto.request.UpdateUserInfoRequest;
import com.hyan.zealinklybackend.dto.response.LoginResponse;
import com.hyan.zealinklybackend.dto.response.RegisterResponse;
import com.hyan.zealinklybackend.dto.response.UserInfoResponse;
import com.hyan.zealinklybackend.repository.ElderRepository;
import com.hyan.zealinklybackend.repository.VolunteerRepository;
import com.hyan.zealinklybackend.repository.AdminRepository;
import com.hyan.zealinklybackend.security.JwtTokenProvider;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.AuthService;
import com.hyan.zealinklybackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 个人信息功能测试
 */
@SpringBootTest
@Transactional
class UserInfoTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ElderRepository elderRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        elderRepository.findByUsername("infotestelder").ifPresent(elderRepository::delete);
        volunteerRepository.findByUsername("infotestvolunteer").ifPresent(volunteerRepository::delete);
        adminRepository.findByUsername("infotestadmin").ifPresent(adminRepository::delete);
    }

    /**
     * 测试获取老人个人信息
     */
    @Test
    void testGetElderInfo() {
        System.out.println("\n==========================================");
        System.out.println("测试：获取老人个人信息");
        System.out.println("==========================================");

        // 1. 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("infotestelder");
        registerRequest.setPassword("123456");
        registerRequest.setRealName("信息测试老人");
        registerRequest.setPhone("13800138000");
        registerRequest.setAddress("测试地址1");
        registerRequest.setLat(new BigDecimal("36.12345678"));
        registerRequest.setLng(new BigDecimal("117.12345678"));

        RegisterResponse registerResponse = authService.registerElder(registerRequest);

        // 2. 登录获取 UserPrincipal
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("infotestelder");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("ELDER");

        LoginResponse loginResponse = authService.login(loginRequest);
        UserPrincipal userPrincipal = jwtTokenProvider.getUserPrincipalFromToken(loginResponse.getToken());

        // 3. 获取个人信息
        UserInfoResponse infoResponse = userService.getUserInfo(userPrincipal);

        assertNotNull(infoResponse);
        assertEquals("infotestelder", infoResponse.getUsername());
        assertEquals("信息测试老人", infoResponse.getRealName());
        assertEquals("13800138000", infoResponse.getPhone());
        assertEquals("ELDER", infoResponse.getUserType());
        assertEquals("测试地址1", infoResponse.getAddress());
        assertEquals(0, infoResponse.getPoints());
        assertNotNull(infoResponse.getLat());
        assertNotNull(infoResponse.getLng());
        assertNotNull(infoResponse.getCreatedAt());

        System.out.println("✅ 获取个人信息成功！");
        System.out.println("用户ID: " + infoResponse.getId());
        System.out.println("用户名: " + infoResponse.getUsername());
        System.out.println("真实姓名: " + infoResponse.getRealName());
        System.out.println("电话: " + infoResponse.getPhone());
        System.out.println("地址: " + infoResponse.getAddress());
        System.out.println("积分: " + infoResponse.getPoints());
        System.out.println("==========================================\n");
    }

    /**
     * 测试获取志愿者个人信息
     */
    @Test
    void testGetVolunteerInfo() {
        System.out.println("\n==========================================");
        System.out.println("测试：获取志愿者个人信息");
        System.out.println("==========================================");

        // 1. 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("infotestvolunteer");
        registerRequest.setPassword("123456");
        registerRequest.setRealName("信息测试志愿者");
        registerRequest.setPhone("13800138001");

        authService.registerVolunteer(registerRequest);

        // 2. 登录获取 UserPrincipal
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("infotestvolunteer");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("VOLUNTEER");

        LoginResponse loginResponse = authService.login(loginRequest);
        UserPrincipal userPrincipal = jwtTokenProvider.getUserPrincipalFromToken(loginResponse.getToken());

        // 3. 获取个人信息
        UserInfoResponse infoResponse = userService.getUserInfo(userPrincipal);

        assertNotNull(infoResponse);
        assertEquals("infotestvolunteer", infoResponse.getUsername());
        assertEquals("信息测试志愿者", infoResponse.getRealName());
        assertEquals("13800138001", infoResponse.getPhone());
        assertEquals("VOLUNTEER", infoResponse.getUserType());
        assertEquals(0, infoResponse.getPoints());
        assertFalse(infoResponse.getIdCardStatus()); // 默认 false
        assertNotNull(infoResponse.getCreatedAt());

        System.out.println("✅ 获取个人信息成功！");
        System.out.println("用户ID: " + infoResponse.getId());
        System.out.println("用户名: " + infoResponse.getUsername());
        System.out.println("真实姓名: " + infoResponse.getRealName());
        System.out.println("电话: " + infoResponse.getPhone());
        System.out.println("积分: " + infoResponse.getPoints());
        System.out.println("实名认证状态: " + infoResponse.getIdCardStatus());
        System.out.println("==========================================\n");
    }

    /**
     * 测试获取管理员个人信息
     */
    @Test
    void testGetAdminInfo() {
        System.out.println("\n==========================================");
        System.out.println("测试：获取管理员个人信息");
        System.out.println("==========================================");

        // 1. 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("infotestadmin");
        registerRequest.setPassword("123456");
        registerRequest.setRealName("信息测试管理员");
        registerRequest.setRoleLevel(2);

        authService.registerAdmin(registerRequest);

        // 2. 登录获取 UserPrincipal
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("infotestadmin");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("ADMIN");

        LoginResponse loginResponse = authService.login(loginRequest);
        UserPrincipal userPrincipal = jwtTokenProvider.getUserPrincipalFromToken(loginResponse.getToken());

        // 3. 获取个人信息
        UserInfoResponse infoResponse = userService.getUserInfo(userPrincipal);

        assertNotNull(infoResponse);
        assertEquals("infotestadmin", infoResponse.getUsername());
        assertEquals("信息测试管理员", infoResponse.getRealName());
        assertEquals("ADMIN", infoResponse.getUserType());
        assertEquals(2, infoResponse.getRoleLevel());
        assertNotNull(infoResponse.getCreatedAt());

        System.out.println("✅ 获取个人信息成功！");
        System.out.println("用户ID: " + infoResponse.getId());
        System.out.println("用户名: " + infoResponse.getUsername());
        System.out.println("真实姓名: " + infoResponse.getRealName());
        System.out.println("角色级别: " + infoResponse.getRoleLevel());
        System.out.println("==========================================\n");
    }

    /**
     * 测试更新老人个人信息
     */
    @Test
    void testUpdateElderInfo() {
        System.out.println("\n==========================================");
        System.out.println("测试：更新老人个人信息");
        System.out.println("==========================================");

        // 1. 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("updateelder");
        registerRequest.setPassword("123456");
        registerRequest.setRealName("原始姓名");
        registerRequest.setPhone("13800138000");
        registerRequest.setAddress("原始地址");

        authService.registerElder(registerRequest);

        // 2. 登录获取 UserPrincipal
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("updateelder");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("ELDER");

        LoginResponse loginResponse = authService.login(loginRequest);
        UserPrincipal userPrincipal = jwtTokenProvider.getUserPrincipalFromToken(loginResponse.getToken());

        // 3. 更新个人信息
        UpdateUserInfoRequest updateRequest = new UpdateUserInfoRequest();
        updateRequest.setRealName("更新后的姓名");
        updateRequest.setPhone("13800138001");
        updateRequest.setAddress("更新后的地址");
        updateRequest.setLat(new BigDecimal("36.87654321"));
        updateRequest.setLng(new BigDecimal("117.87654321"));

        UserInfoResponse updatedInfo = userService.updateUserInfo(userPrincipal, updateRequest);

        assertNotNull(updatedInfo);
        assertEquals("更新后的姓名", updatedInfo.getRealName());
        assertEquals("13800138001", updatedInfo.getPhone());
        assertEquals("更新后的地址", updatedInfo.getAddress());
        assertEquals(new BigDecimal("36.87654321"), updatedInfo.getLat());
        assertEquals(new BigDecimal("117.87654321"), updatedInfo.getLng());

        System.out.println("✅ 更新个人信息成功！");
        System.out.println("更新前姓名: 原始姓名");
        System.out.println("更新后姓名: " + updatedInfo.getRealName());
        System.out.println("更新后电话: " + updatedInfo.getPhone());
        System.out.println("更新后地址: " + updatedInfo.getAddress());
        System.out.println("更新后纬度: " + updatedInfo.getLat());
        System.out.println("更新后经度: " + updatedInfo.getLng());
        System.out.println("==========================================\n");
    }

    /**
     * 测试更新志愿者个人信息
     */
    @Test
    void testUpdateVolunteerInfo() {
        System.out.println("\n==========================================");
        System.out.println("测试：更新志愿者个人信息");
        System.out.println("==========================================");

        // 1. 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("updatevolunteer");
        registerRequest.setPassword("123456");
        registerRequest.setRealName("原始姓名");
        registerRequest.setPhone("13800138000");

        authService.registerVolunteer(registerRequest);

        // 2. 登录获取 UserPrincipal
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("updatevolunteer");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("VOLUNTEER");

        LoginResponse loginResponse = authService.login(loginRequest);
        UserPrincipal userPrincipal = jwtTokenProvider.getUserPrincipalFromToken(loginResponse.getToken());

        // 3. 更新个人信息
        UpdateUserInfoRequest updateRequest = new UpdateUserInfoRequest();
        updateRequest.setRealName("更新后的姓名");
        updateRequest.setPhone("13800138001");

        UserInfoResponse updatedInfo = userService.updateUserInfo(userPrincipal, updateRequest);

        assertNotNull(updatedInfo);
        assertEquals("更新后的姓名", updatedInfo.getRealName());
        assertEquals("13800138001", updatedInfo.getPhone());

        System.out.println("✅ 更新个人信息成功！");
        System.out.println("更新前姓名: 原始姓名");
        System.out.println("更新后姓名: " + updatedInfo.getRealName());
        System.out.println("更新后电话: " + updatedInfo.getPhone());
        System.out.println("==========================================\n");
    }

    /**
     * 测试更新管理员个人信息
     */
    @Test
    void testUpdateAdminInfo() {
        System.out.println("\n==========================================");
        System.out.println("测试：更新管理员个人信息");
        System.out.println("==========================================");

        // 1. 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("updateadmin");
        registerRequest.setPassword("123456");
        registerRequest.setRealName("原始姓名");
        registerRequest.setRoleLevel(1);

        authService.registerAdmin(registerRequest);

        // 2. 登录获取 UserPrincipal
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("updateadmin");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("ADMIN");

        LoginResponse loginResponse = authService.login(loginRequest);
        UserPrincipal userPrincipal = jwtTokenProvider.getUserPrincipalFromToken(loginResponse.getToken());

        // 3. 更新个人信息
        UpdateUserInfoRequest updateRequest = new UpdateUserInfoRequest();
        updateRequest.setRealName("更新后的姓名");
        updateRequest.setRoleLevel(2);

        UserInfoResponse updatedInfo = userService.updateUserInfo(userPrincipal, updateRequest);

        assertNotNull(updatedInfo);
        assertEquals("更新后的姓名", updatedInfo.getRealName());
        assertEquals(2, updatedInfo.getRoleLevel());

        System.out.println("✅ 更新个人信息成功！");
        System.out.println("更新前姓名: 原始姓名");
        System.out.println("更新后姓名: " + updatedInfo.getRealName());
        System.out.println("更新前角色级别: 1");
        System.out.println("更新后角色级别: " + updatedInfo.getRoleLevel());
        System.out.println("==========================================\n");
    }

    /**
     * 测试部分更新（只更新部分字段）
     */
    @Test
    void testPartialUpdateElderInfo() {
        System.out.println("\n==========================================");
        System.out.println("测试：部分更新老人信息（只更新姓名）");
        System.out.println("==========================================");

        // 1. 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("partialupdate");
        registerRequest.setPassword("123456");
        registerRequest.setRealName("原始姓名");
        registerRequest.setPhone("13800138000");
        registerRequest.setAddress("原始地址");

        authService.registerElder(registerRequest);

        // 2. 登录获取 UserPrincipal
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("partialupdate");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("ELDER");

        LoginResponse loginResponse = authService.login(loginRequest);
        UserPrincipal userPrincipal = jwtTokenProvider.getUserPrincipalFromToken(loginResponse.getToken());

        // 3. 只更新姓名，其他字段保持原样
        UpdateUserInfoRequest updateRequest = new UpdateUserInfoRequest();
        updateRequest.setRealName("只更新姓名");

        UserInfoResponse updatedInfo = userService.updateUserInfo(userPrincipal, updateRequest);

        assertNotNull(updatedInfo);
        assertEquals("只更新姓名", updatedInfo.getRealName());
        assertEquals("13800138000", updatedInfo.getPhone()); // 电话未更新，保持原值
        assertEquals("原始地址", updatedInfo.getAddress()); // 地址未更新，保持原值

        System.out.println("✅ 部分更新成功！");
        System.out.println("更新后姓名: " + updatedInfo.getRealName());
        System.out.println("电话（未更新）: " + updatedInfo.getPhone());
        System.out.println("地址（未更新）: " + updatedInfo.getAddress());
        System.out.println("==========================================\n");
    }

    /**
     * 综合测试：注册 -> 登录 -> 获取信息 -> 更新信息 -> 再次获取信息
     */
    @Test
    void testCompleteUserInfoFlow() {
        System.out.println("\n==========================================");
        System.out.println("综合测试：完整的个人信息流程");
        System.out.println("==========================================");

        // 1. 注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("completeflow");
        registerRequest.setPassword("123456");
        registerRequest.setRealName("流程测试用户");
        registerRequest.setPhone("13800138000");
        registerRequest.setAddress("初始地址");

        RegisterResponse registerResponse = authService.registerElder(registerRequest);
        System.out.println("✅ 步骤1：注册成功");

        // 2. 登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("completeflow");
        loginRequest.setPassword("123456");
        loginRequest.setUserType("ELDER");

        LoginResponse loginResponse = authService.login(loginRequest);
        UserPrincipal userPrincipal = jwtTokenProvider.getUserPrincipalFromToken(loginResponse.getToken());
        System.out.println("✅ 步骤2：登录成功");

        // 3. 获取初始信息
        UserInfoResponse initialInfo = userService.getUserInfo(userPrincipal);
        assertEquals("流程测试用户", initialInfo.getRealName());
        assertEquals("初始地址", initialInfo.getAddress());
        System.out.println("✅ 步骤3：获取初始信息成功");
        System.out.println("   初始姓名: " + initialInfo.getRealName());
        System.out.println("   初始地址: " + initialInfo.getAddress());

        // 4. 更新信息
        UpdateUserInfoRequest updateRequest = new UpdateUserInfoRequest();
        updateRequest.setRealName("更新后的流程用户");
        updateRequest.setAddress("更新后的地址");

        UserInfoResponse updatedInfo = userService.updateUserInfo(userPrincipal, updateRequest);
        assertEquals("更新后的流程用户", updatedInfo.getRealName());
        assertEquals("更新后的地址", updatedInfo.getAddress());
        System.out.println("✅ 步骤4：更新信息成功");
        System.out.println("   更新后姓名: " + updatedInfo.getRealName());
        System.out.println("   更新后地址: " + updatedInfo.getAddress());

        // 5. 再次获取信息验证
        UserInfoResponse finalInfo = userService.getUserInfo(userPrincipal);
        assertEquals("更新后的流程用户", finalInfo.getRealName());
        assertEquals("更新后的地址", finalInfo.getAddress());
        System.out.println("✅ 步骤5：验证更新成功");
        System.out.println("   最终姓名: " + finalInfo.getRealName());
        System.out.println("   最终地址: " + finalInfo.getAddress());
        System.out.println("==========================================\n");
    }
}
