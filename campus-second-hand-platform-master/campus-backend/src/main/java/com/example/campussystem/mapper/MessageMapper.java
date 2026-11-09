package com.example.campussystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campussystem.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
