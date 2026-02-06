package com.hyan.zealinklybackend;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConnectionTest {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试 PostgreSQL 数据库连接
     */
    @Test
    void testDatabaseConnection() {
        try {
            // 获取数据库连接
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            assertNotNull(connection, "数据库连接不应为 null");

            // 获取数据库元数据
            DatabaseMetaData metaData = connection.getMetaData();
            assertNotNull(metaData, "数据库元数据不应为 null");

            // 打印连接信息
            System.out.println("==========================================");
            System.out.println("✅ PostgreSQL 数据库连接成功！");
            System.out.println("数据库 URL: " + metaData.getURL());
            System.out.println("数据库用户名: " + metaData.getUserName());
            System.out.println("数据库产品名称: " + metaData.getDatabaseProductName());
            System.out.println("数据库产品版本: " + metaData.getDatabaseProductVersion());
            System.out.println("驱动名称: " + metaData.getDriverName());
            System.out.println("驱动版本: " + metaData.getDriverVersion());
            System.out.println("==========================================");

            // 执行简单查询测试
            String result = jdbcTemplate.queryForObject("SELECT version()", String.class);
            assertNotNull(result, "数据库查询结果不应为 null");
            System.out.println("PostgreSQL 版本: " + result);

            // 关闭连接
            connection.close();
        } catch (Exception e) {
            System.err.println("❌ 数据库连接失败: " + e.getMessage());
            e.printStackTrace();
            fail("数据库连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试 Redis 连接
     */
    @Test
    void testRedisConnection() {
        try {
            // 测试 Redis 连接（通过执行 PING 命令）
            redisTemplate.getConnectionFactory().getConnection().ping();

            // 测试 Redis 读写操作
            String testKey = "connection:test:key";
            String testValue = "Hello Redis from Zealinkly!";

            // 写入测试数据
            redisTemplate.opsForValue().set(testKey, testValue);
            System.out.println("==========================================");
            System.out.println("✅ Redis 连接成功！");
            System.out.println("写入测试键: " + testKey);
            System.out.println("写入测试值: " + testValue);

            // 读取测试数据
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            assertNotNull(retrievedValue, "Redis 读取的值不应为 null");
            assertEquals(testValue, retrievedValue, "Redis 读取的值应与写入的值一致");

            System.out.println("读取测试值: " + retrievedValue);
            System.out.println("✅ Redis 读写测试通过！");

            // 清理测试数据
            redisTemplate.delete(testKey);
            System.out.println("已清理测试数据");
            System.out.println("==========================================");
        } catch (Exception e) {
            System.err.println("❌ Redis 连接失败: " + e.getMessage());
            e.printStackTrace();
            fail("Redis 连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 综合测试：同时测试数据库和 Redis
     */
    @Test
    void testAllConnections() {
        System.out.println("\n开始综合连接测试...\n");
        testDatabaseConnection();
        System.out.println();
        testRedisConnection();
        System.out.println("\n✅ 所有连接测试完成！\n");
    }
}
