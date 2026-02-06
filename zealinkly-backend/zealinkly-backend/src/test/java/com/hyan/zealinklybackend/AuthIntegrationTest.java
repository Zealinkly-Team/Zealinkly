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

import static org.junit.jupiter.api.Assertions.*;

/**
 * 注册和登录集成测试（不使用事务回滚，数据会真正保存到数据库）
 * 注意：这个测试会真正写入数据库，测试前会清理数据
 */
@SpringBootTest
class AuthIntegrationTest {

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
        elderRepository.findByUsername("realtestelder").ifPresent(elderRepository::delete);
        elderRepository.findByUsername("realtestvolunteer").ifPresent(elderRepository::delete);
        elderRepository.findByUsername("realtestadmin").ifPresent(elderRepository::delete);
        volunteerRepository.findByUsername("realtestelder").ifPresent(volunteerRepository::delete);
        volunteerRepository.findByUsername("realtestvolunteer").ifPresent(volunteerRepository::delete);
        volunteerRepository.findByUsername("realtestadmin").ifPresent(volunteerRepository::delete);
        adminRepository.findByUsername("realtestelder").ifPresent(adminRepository::delete);
        adminRepository.findByUsername("realtestvolunteer").ifPresent(adminRepository::delete);
        adminRepository.findByUsername("realtestadmin").ifPresent(adminRepository::delete);
    }

    /**
     * 测试注册老人（数据会真正保存到数据库）
     */
    @Test
    void testRegisterElderRealData() {
        System.out.println("\n==========================================");
        System.out.println("集成测试：注册老人（数据会保存到数据库）");
        System.out.println("==========================================");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("realtestelder");
        request.setPassword("123456");
        request.setRealName("真实测试老人");
        request.setPhone("13800138000");
        request.setAddress("测试小区1号楼");

        RegisterResponse response = authService.registerElder(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("realtestelder", response.getUsername());
        assertEquals("ELDER", response.getUserType());

        // 验证数据真正保存到数据库
        assertTrue(elderRepository.existsByUsername("realtestelder"));
        elderRepository.findByUsername("realtestelder").ifPresent(elder -> {
            assertEquals("真实测试老人", elder.getRealName());
            assertEquals("13800138000", elder.getPhone());
            assertEquals("测试小区1号楼", elder.getAddress());
        });

        System.out.println("✅ 注册成功！数据已保存到数据库");
        System.out.println("用户ID: " + response.getId());
        System.out.println("用户名: " + response.getUsername());
        System.out.println("用户类型: " + response.getUserType());
        System.out.println("==========================================\n");
    }

    /**
     * 测试注册志愿者（数据会真正保存到数据库）
     */
    @Test
    void testRegisterVolunteerRealData() {
        System.out.println("\n==========================================");
        System.out.println("集成测试：注册志愿者（数据会保存到数据库）");
        System.out.println("==========================================");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("realtestvolunteer");
        request.setPassword("123456");
        request.setRealName("真实测试志愿者");
        request.setPhone("13800138001");

        RegisterResponse response = authService.registerVolunteer(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("realtestvolunteer", response.getUsername());
        assertEquals("VOLUNTEER", response.getUserType());

        // 验证数据真正保存到数据库
        assertTrue(volunteerRepository.existsByUsername("realtestvolunteer"));
        volunteerRepository.findByUsername("realtestvolunteer").ifPresent(volunteer -> {
            assertEquals("真实测试志愿者", volunteer.getRealName());
            assertEquals("13800138001", volunteer.getPhone());
        });

        System.out.println("✅ 注册成功！数据已保存到数据库");
        System.out.println("用户ID: " + response.getId());
        System.out.println("用户名: " + response.getUsername());
        System.out.println("用户类型: " + response.getUserType());
        System.out.println("==========================================\n");
    }

    /**
     * 测试注册管理员（数据会真正保存到数据库）
     */
    @Test
    void testRegisterAdminRealData() {
        System.out.println("\n==========================================");
        System.out.println("集成测试：注册管理员（数据会保存到数据库）");
        System.out.println("==========================================");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("realtestadmin");
        request.setPassword("123456");
        request.setRealName("真实测试管理员");
        request.setRoleLevel(1);

        RegisterResponse response = authService.registerAdmin(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("realtestadmin", response.getUsername());
        assertEquals("ADMIN", response.getUserType());

        // 验证数据真正保存到数据库
        assertTrue(adminRepository.existsByUsername("realtestadmin"));
        adminRepository.findByUsername("realtestadmin").ifPresent(admin -> {
            assertEquals("真实测试管理员", admin.getRealName());
            assertEquals(1, admin.getRoleLevel());
        });

        System.out.println("✅ 注册成功！数据已保存到数据库");
        System.out.println("用户ID: " + response.getId());
        System.out.println("用户名: " + response.getUsername());
        System.out.println("用户类型: " + response.getUserType());
        System.out.println("==========================================\n");
    }

    /**
     * 测试完整流程：注册 + 登录（数据会真正保存到数据库）
     */
    @Test
    void testRegisterAndLoginRealData() {
        System.out.println("\n==========================================");
        System.out.println("集成测试：注册 + 登录完整流程（数据会保存到数据库）");
        System.out.println("==========================================");

        // 1. 注册老人
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("realtestflow");
        registerRequest.setPassword("123456");
        registerRequest.setRealName("流程测试用户");
        registerRequest.setPhone("13800138002");
        registerRequest.setAddress("测试地址");

        RegisterResponse registerResponse = authService.registerElder(registerRequest);
        System.out.println("✅ 步骤1：注册成功");
        System.out.println("   用户ID: " + registerResponse.getId());
        System.out.println("   用户名: " + registerResponse.getUsername());

        // 验证数据已保存
        assertTrue(elderRepository.existsByUsername("realtestflow"));

        // 2. 登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("realtestflow");
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
        System.out.println("✅ 数据已保存到数据库，可以在数据库中查看");
        System.out.println("==========================================\n");
    }
}
