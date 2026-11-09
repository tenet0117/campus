package com.example.campussystem.service;

import com.example.campussystem.dto.UserLoginRequest;
import com.example.campussystem.dto.UserLoginResponse;
import com.example.campussystem.dto.UserProfileResponse;
import com.example.campussystem.dto.UserRegisterRequest;
import com.example.campussystem.dto.UserRegisterResponse;
import com.example.campussystem.dto.UserUpdateRequest;
import com.example.campussystem.entity.User;

import java.util.Map;
import java.util.Optional;

public interface UserService {

    UserLoginResponse login(UserLoginRequest request);

    UserRegisterResponse register(UserRegisterRequest request);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean isUsernameExists(String username);

    boolean isEmailExists(String email);

    boolean isPhoneExists(String phone);

    UserProfileResponse getUserProfile(Long userId);

    UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request);

    Map<String, Object> getUserTradeStats(Long userId);
}
