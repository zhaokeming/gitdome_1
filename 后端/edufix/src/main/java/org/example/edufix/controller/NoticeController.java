package org.example.edufix.controller;

import org.example.edufix.common.Result;
import org.example.edufix.entity.Notice;
import org.example.edufix.exception.BusinessException;
import org.example.edufix.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 获取已发布的公告列表
     */
    @GetMapping("/published")
    public Result<List<Notice>> getPublishedNotices() {
        List<Notice> notices = noticeService.getPublishedNotices();
        return Result.success(notices);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<Notice> getNotice(@PathVariable Long id) {
        Notice notice = noticeService.getNoticeById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        return Result.success(notice);
    }

    /**
     * 创建公告(管理员)
     */
    @PostMapping
    public Result<Void> createNotice(@RequestBody Notice notice, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权操作");
        }

        Long userId = (Long) httpRequest.getAttribute("userId");
        notice.setPublisherId(userId);
        noticeService.createNotice(notice);
        return Result.success("创建成功", null);
    }

    /**
     * 发布公告(管理员)
     */
    @PutMapping("/{id}/publish")
    public Result<Void> publishNotice(@PathVariable Long id, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权操作");
        }

        noticeService.publishNotice(id);
        return Result.success("发布成功", null);
    }
}
