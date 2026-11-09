package com.example.campussystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campussystem.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
