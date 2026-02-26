package com.hyan.zealinklybackend;

import com.hyan.zealinklybackend.controller.GreetingController;
import com.hyan.zealinklybackend.dto.request.RegisterRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.repository.ElderRepository;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 问候功能测试
 */
@SpringBootTest
@Transactional
class GreetingTest {

    @Autowired
    private GreetingController greetingController;

    @Autowired
    private AuthService authService;

    @Autowired
    private ElderRepository elderRepository;

    @BeforeEach
    void setUp() {
        elderRepository.findByUsername("greetingelder").ifPresent(elderRepository::delete);
    }

    /**
     * 测试匿名用户问候
     */
    @Test
    void testAnonymousGreeting() {
        System.out.println("\n==========================================");
        System.out.println("测试：匿名用户问候");
        System.out.println("==========================================");

        ApiResponse<String> response = greetingController.greet(null);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("你好！", response.getData());

        System.out.println("✅ 匿名问候成功！");
        System.out.println("问候语: " + response.getData());
        System.out.println("==========================================\n");
    }

    /**
     * 测试已登录用户个性化问候
     */
    @Test
    void testPersonalizedGreeting() {
        System.out.println("\n==========================================");
        System.out.println("测试：已登录用户个性化问候");
        System.out.println("==========================================");

        // 注册用户
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("greetingelder");
        registerRequest.setPassword("123456");
        registerRequest.setRealName("张三");
        registerRequest.setPhone("13800138000");

        authService.registerElder(registerRequest);

        UserPrincipal userPrincipal = new UserPrincipal("ELDER", null, "greetingelder");
        // 从数据库中获取实际的用户ID
        elderRepository.findByUsername("greetingelder").ifPresent(elder -> userPrincipal.setUserId(elder.getId()));

        ApiResponse<String> response = greetingController.greet(userPrincipal);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData().contains("你好"));
        assertTrue(response.getData().contains("张三"));

        System.out.println("✅ 个性化问候成功！");
        System.out.println("问候语: " + response.getData());
        System.out.println("==========================================\n");
    }
}
