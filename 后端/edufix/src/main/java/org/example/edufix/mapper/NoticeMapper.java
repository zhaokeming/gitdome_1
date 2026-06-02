package org.example.edufix.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.edufix.entity.Notice;

/**
 * 公告Mapper
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
