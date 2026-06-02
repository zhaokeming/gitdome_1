package org.example.edufix.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.edufix.dto.StaffVO;
import org.example.edufix.entity.Staff;
import org.example.edufix.entity.User;
import org.example.edufix.mapper.StaffMapper;
import org.example.edufix.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工服务类
 */
@Service
public class StaffService {

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取所有员工列表（含用户信息）
     */
    public List<StaffVO> getAllStaff() {
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Staff::getRating);
        List<Staff> staffList = staffMapper.selectList(wrapper);

        if (staffList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> userIds = staffList.stream()
                .map(Staff::getUserId)
                .collect(Collectors.toList());

        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return staffList.stream().map(staff -> {
            StaffVO vo = new StaffVO();
            BeanUtils.copyProperties(staff, vo);
            User user = userMap.get(staff.getUserId());
            if (user != null) {
                vo.setRealName(user.getRealName());
                vo.setPhone(user.getPhone());
                vo.setEmail(user.getEmail());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 根据ID获取员工信息
     */
    public Staff getStaffById(Long id) {
        return staffMapper.selectById(id);
    }

    /**
     * 根据ID获取员工信息（含用户姓名、电话）
     */
    public StaffVO getStaffVOById(Long id) {
        Staff staff = staffMapper.selectById(id);
        if (staff == null) {
            return null;
        }
        StaffVO vo = new StaffVO();
        BeanUtils.copyProperties(staff, vo);
        User user = userMapper.selectById(staff.getUserId());
        if (user != null) {
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
        }
        return vo;
    }

    /**
     * 根据用户ID获取员工信息
     */
    public Staff getStaffByUserId(Long userId) {
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Staff::getUserId, userId);
        return staffMapper.selectOne(wrapper);
    }
}
