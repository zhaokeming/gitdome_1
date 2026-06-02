package org.example.edufix.controller;

import org.example.edufix.common.Result;
import org.example.edufix.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    ));
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @javax.annotation.PostConstruct
    public void init() {
        Path path = Paths.get(uploadPath);
        if (!path.isAbsolute()) {
            uploadPath = Paths.get(System.getProperty("user.dir"), uploadPath).normalize().toString();
        }
        try {
            Files.createDirectories(Paths.get(uploadPath));
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录: " + uploadPath, e);
        }
    }

    /**
     * 上传单个文件
     */
    @PostMapping
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        validateFile(file);

        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Path uploadDir = Paths.get(uploadPath, dateDir);
            Files.createDirectories(uploadDir);

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            }

            String newFileName = UUID.randomUUID().toString() + ext;
            Path filePath = uploadDir.resolve(newFileName);
            file.transferTo(filePath.toFile());

            String url = "/uploads/" + dateDir + "/" + newFileName;

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("name", originalName);

            return Result.success("上传成功", result);
        } catch (IOException e) {
            throw new BusinessException("上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传多个文件
     */
    @PostMapping("/batch")
    public Result<List<Map<String, String>>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            List<Map<String, String>> results = new ArrayList<>();

            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Path uploadDir = Paths.get(uploadPath, dateDir);
            Files.createDirectories(uploadDir);

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                validateFile(file);

                String originalName = file.getOriginalFilename();
                String ext = "";
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
                }
                String newFileName = UUID.randomUUID().toString() + ext;

                Path filePath = uploadDir.resolve(newFileName);
                file.transferTo(filePath.toFile());

                String url = "/uploads/" + dateDir + "/" + newFileName;

                Map<String, String> item = new HashMap<>();
                item.put("url", url);
                item.put("name", originalName);
                results.add(item);
            }

            return Result.success("上传成功", results);
        } catch (IOException e) {
            throw new BusinessException("上传失败: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过10MB");
        }
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new BusinessException("不支持的文件类型: " + ext);
            }
        }
    }
}
