package com.github.bannirui.msb.dal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends BaseMapper<User> {
    User mySelectById(Long id);
}