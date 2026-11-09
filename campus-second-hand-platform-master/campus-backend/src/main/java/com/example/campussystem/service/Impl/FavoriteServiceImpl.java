package com.example.campussystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.campussystem.dto.ProductResponse;
import com.example.campussystem.entity.Favorite;
import com.example.campussystem.entity.Product;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.mapper.FavoriteMapper;
import com.example.campussystem.mapper.ProductMapper;
import com.example.campussystem.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    public void addFavorite(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (count > 0) {
            throw new BusinessException("商品已在收藏列表中");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(favorite);
    }

    public void removeFavorite(Long userId, Long productId) {
        Favorite favorite = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (favorite == null) {
            throw new BusinessException("收藏记录不存在");
        }
        favoriteMapper.deleteById(favorite.getId());
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteMapper.exists(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getUserFavorites(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime));

        return favorites.stream()
                .map(favorite -> {
                    Product product = productMapper.selectById(favorite.getProductId());
                    if (product != null && product.getStatus() == 1) {
                        return new ProductResponse(product);
                    }
                    return null;
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUserFavorites(Long userId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId));
    }

    @Transactional(readOnly = true)
    public long countProductFavorites(Long productId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getProductId, productId));
    }
}
