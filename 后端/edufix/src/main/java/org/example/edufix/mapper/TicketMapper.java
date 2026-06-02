package org.example.edufix.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.edufix.entity.Ticket;

/**
 * 工单Mapper
 */
@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {
}
