package org.example.edufix.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.edufix.entity.Notice;
import org.example.edufix.mapper.NoticeMapper;
import org.example.edufix.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告服务类
 */
@Service
public class NoticeService {
    
    @Autowired
    private NoticeMapper noticeMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    /**
     * 获取已发布的公告列表
     */
    public List<Notice> getPublishedNotices() {
        // 从数据库查询
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1); // 已发布
        wrapper.orderByDesc(Notice::getPriority)
                .orderByDesc(Notice::getPublishTime);
        
        List<Notice> notices = noticeMapper.selectList(wrapper);
        
        return notices;
    }
    
    /**
     * 获取公告详情
     */
    public Notice getNoticeById(Long id) {
        // 先从Redis缓存获取
        Object cached = redisUtil.hGet("notice:detail", String.valueOf(id));
        if (cached != null) {
            return (Notice) cached;
        }
        
        // 从数据库查询
        Notice notice = noticeMapper.selectById(id);
        
        if (notice != null) {
            // 增加浏览次数
            notice.setViewCount(notice.getViewCount() + 1);
            noticeMapper.updateById(notice);
            
            // 缓存到Redis Hash(过期时间1小时)
            redisUtil.hSet("notice:detail", String.valueOf(id), notice);
        }
        
        return notice;
    }
    
    /**
     * 创建公告
     */
    public void createNotice(Notice notice) {
        noticeMapper.insert(notice);
        
        // 清除缓存
        clearNoticeCache();
    }
    
    /**
     * 更新公告
     */
    public void updateNotice(Notice notice) {
        noticeMapper.updateById(notice);
        
        // 清除缓存
        clearNoticeCache();
    }
    
    /**
     * 删除公告
     */
    public void deleteNotice(Long id) {
        noticeMapper.deleteById(id);
        
        // 清除缓存
        clearNoticeCache();
    }
    
    /**
     * 发布公告
     */
    public void publishNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice != null) {
            notice.setStatus(1);
            notice.setPublishTime(java.time.LocalDateTime.now());
            noticeMapper.updateById(notice);
            
            // 清除缓存
            clearNoticeCache();
        }
    }
    
    /**
     * 清除公告缓存
     */
    private void clearNoticeCache() {
        redisUtil.delete("notice:published:list");
        redisUtil.delete("notice:detail");
    }
}
