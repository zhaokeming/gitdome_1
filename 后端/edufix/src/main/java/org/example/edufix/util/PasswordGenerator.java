package org.example.edufix.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成工具（用于生成种子数据的BCrypt哈希）
 * 运行 main 方法即可输出哈希值，然后手动填入 schema.sql
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("admin / admin123 → " + encoder.encode("admin123"));
        System.out.println("staff1 / staff123 → " + encoder.encode("staff123"));
        System.out.println("student1 / student123 → " + encoder.encode("student123"));
    }
}
