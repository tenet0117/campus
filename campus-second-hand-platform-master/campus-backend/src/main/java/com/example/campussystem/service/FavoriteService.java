package com.example.campussystem.service;

import com.example.campussystem.dto.ProductResponse;

import java.util.List;

public interface FavoriteService {

    void addFavorite(Long userId, Long productId);

    void removeFavorite(Long userId, Long productId);

    boolean isFavorite(Long userId, Long productId);

    List<ProductResponse> getUserFavorites(Long userId);

    long countUserFavorites(Long userId);

    long countProductFavorites(Long productId);
}
