package org.example.edufix.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.edufix.dto.EvaluationRequest;
import org.example.edufix.entity.Evaluation;
import org.example.edufix.entity.Staff;
import org.example.edufix.entity.Ticket;
import org.example.edufix.exception.BusinessException;
import org.example.edufix.mapper.EvaluationMapper;
import org.example.edufix.mapper.StaffMapper;
import org.example.edufix.mapper.TicketMapper;
import org.example.edufix.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 评价服务类
 */
@Service
public class EvaluationService {
    
    @Autowired
    private EvaluationMapper evaluationMapper;
    
    @Autowired
    private TicketMapper ticketMapper;
    
    @Autowired
    private StaffMapper staffMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    /**
     * 创建评价
     */
    @Transactional(rollbackFor = Exception.class)
    public void createEvaluation(EvaluationRequest request, Long userId) {
        // 检查工单是否存在且已完成
        Ticket ticket = ticketMapper.selectById(request.getTicketId());
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        if (!"COMPLETED".equals(ticket.getStatus())) {
            throw new BusinessException("只能评价已完成的工单");
        }

        if (!userId.equals(ticket.getUserId())) {
            throw new BusinessException("无权评价此工单");
        }

        // 检查是否已评价
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getTicketId, request.getTicketId());
        Long count = evaluationMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该工单已评价");
        }
        
        // 创建评价
        Evaluation evaluation = new Evaluation();
        evaluation.setTicketId(request.getTicketId());
        evaluation.setUserId(userId);
        evaluation.setStaffId(ticket.getStaffId());
        evaluation.setRating(request.getRating());
        evaluation.setTags(request.getTags());
        evaluation.setContent(request.getContent());
        
        evaluationMapper.insert(evaluation);
        
        // 更新员工评分
        updateStaffRating(ticket.getStaffId());
        
        // 缓存评价标签到Redis Hash
        cacheEvaluationTags();
    }
    
    /**
     * 获取工单评价
     */
    public Evaluation getEvaluationByTicketId(Long ticketId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getTicketId, ticketId);
        return evaluationMapper.selectOne(wrapper);
    }
    
    /**
     * 获取员工的评价列表
     */
    public List<Evaluation> getEvaluationsByStaffId(Long staffId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getStaffId, staffId);
        wrapper.orderByDesc(Evaluation::getCreateTime);
        return evaluationMapper.selectList(wrapper);
    }
    
    /**
     * 更新员工评分
     */
    private void updateStaffRating(Long staffId) {
        if (staffId == null) {
            return;
        }
        
        // 计算平均评分
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getStaffId, staffId);
        List<Evaluation> evaluations = evaluationMapper.selectList(wrapper);
        
        if (evaluations.isEmpty()) {
            return;
        }
        
        double sum = evaluations.stream()
                .mapToInt(Evaluation::getRating)
                .sum();
        
        BigDecimal avgRating = BigDecimal.valueOf(sum / evaluations.size())
                .setScale(2, RoundingMode.HALF_UP);
        
        // 更新员工评分
        Staff staff = staffMapper.selectById(staffId);
        if (staff != null) {
            staff.setRating(avgRating);
            staffMapper.updateById(staff);
        }
    }
    
    /**
     * 缓存评价标签到Redis Hash
     */
    private void cacheEvaluationTags() {
        // 统计所有评价标签
        List<Evaluation> allEvaluations = evaluationMapper.selectList(null);
        
        // 清空旧缓存
        redisUtil.delete("evaluation:tags");
        
        // 统计标签出现次数
        java.util.Map<String, Integer> tagCountMap = new java.util.HashMap<>();
        for (Evaluation eval : allEvaluations) {
            if (eval.getTags() != null && !eval.getTags().isEmpty()) {
                String[] tags = eval.getTags().split(",");
                for (String tag : tags) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        tagCountMap.merge(trimmedTag, 1, Integer::sum);
                    }
                }
            }
        }
        
        // 存入Redis Hash
        for (java.util.Map.Entry<String, Integer> entry : tagCountMap.entrySet()) {
            redisUtil.hSet("evaluation:tags", entry.getKey(), entry.getValue());
        }
    }
}
