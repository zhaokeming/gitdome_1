package org.example.edufix.config;

import org.example.edufix.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    private String getResolvedUploadPath() {
        java.nio.file.Path path = Paths.get(uploadPath);
        if (!path.isAbsolute()) {
            return Paths.get(System.getProperty("user.dir"), uploadPath).normalize().toString();
        }
        return uploadPath;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resolvedPath = getResolvedUploadPath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + resolvedPath + "/");
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/notice/published",
                    "/api/notice/{id}",
                    "/uploads/**"
                );
    }
}
