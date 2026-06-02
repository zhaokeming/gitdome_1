package org.example.edufix.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.edufix.entity.TicketLog;

/**
 * 工单日志Mapper
 */
@Mapper
public interface TicketLogMapper extends BaseMapper<TicketLog> {
}
