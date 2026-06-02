package org.example.edufix.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Component
public class RedisUtil {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // ==================== String操作 ====================
    
    /**
     * 设置字符串
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    /**
     * 设置字符串并设置过期时间(秒)
     */
    public void setEx(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }
    
    /**
     * 获取字符串
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 删除key
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
    
    /**
     * 判断key是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    /**
     * 设置过期时间(秒)
     */
    public Boolean expire(String key, long timeout) {
        return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }
    
    // ==================== Hash操作 ====================
    
    /**
     * Hash设置字段
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }
    
    /**
     * Hash获取字段
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }
    
    /**
     * Hash获取所有字段
     */
    public Object hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }
    
    /**
     * Hash删除字段
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }
    
    // ==================== Set操作 ====================
    
    /**
     * Set添加成员
     */
    public Long sAdd(String key, Object... members) {
        return redisTemplate.opsForSet().add(key, members);
    }
    
    /**
     * Set移除成员
     */
    public Long sRemove(String key, Object... members) {
        return redisTemplate.opsForSet().remove(key, members);
    }
    
    /**
     * Set获取所有成员
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }
    
    /**
     * Set判断是否为成员
     */
    public Boolean sIsMember(String key, Object member) {
        return redisTemplate.opsForSet().isMember(key, member);
    }
    
    /**
     * Set获取成员数量
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }
    
    // ==================== ZSet操作 ====================
    
    /**
     * ZSet添加成员
     */
    public Boolean zAdd(String key, Object member, double score) {
        return redisTemplate.opsForZSet().add(key, member, score);
    }
    
    /**
     * ZSet获取成员分数
     */
    public Double zScore(String key, Object member) {
        return redisTemplate.opsForZSet().score(key, member);
    }
    
    /**
     * ZSet按分数降序获取范围
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }
    
    /**
     * ZSet按分数升序获取范围
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }
    
    /**
     * ZSet移除成员
     */
    public Long zRemove(String key, Object... members) {
        return redisTemplate.opsForZSet().remove(key, members);
    }
    
    /**
     * ZSet获取成员数量
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }
}
