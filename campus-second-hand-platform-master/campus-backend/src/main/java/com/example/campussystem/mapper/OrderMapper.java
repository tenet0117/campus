package com.example.campussystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campussystem.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
