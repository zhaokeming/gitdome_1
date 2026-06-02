package org.example.edufix.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.edufix.entity.Staff;

/**
 * 员工Mapper
 */
@Mapper
public interface StaffMapper extends BaseMapper<Staff> {
}
