/*
 Navicat Premium Dump SQL

 Source Server         : 本地数据库
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : campus_trading

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 17/09/2026 15:56:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admins
-- ----------------------------
DROP TABLE IF EXISTS `admins`;
CREATE TABLE `admins`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理员用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ADMIN' COMMENT '角色',
  `status` int NULL DEFAULT 1 COMMENT '状态: 1正常 0禁用',
  `login_count` int NULL DEFAULT 0 COMMENT '登录次数',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admins
-- ----------------------------
INSERT INTO `admins` VALUES (1, 'admin', '$2b$10$hc1RLLpxJn8h7QuE3Zh8De9tsVI6xjI5t9E4Cisjq9SU98kLhjer2', '系统管理员', 'admin@campus.com', '13800000001', NULL, 'ADMIN', 1, 17, '2026-09-17 15:46:52', '0:0:0:0:0:0:0:1', '2026-09-17 11:55:20', '2026-09-17 11:55:20');
INSERT INTO `admins` VALUES (2, 'superadmin', '$2b$10$hc1RLLpxJn8h7QuE3Zh8De9tsVI6xjI5t9E4Cisjq9SU98kLhjer2', '超级管理员', 'super@campus.com', '13800000002', NULL, 'ADMIN', 1, 5, '2026-06-02 18:00:00', '127.0.0.1', '2026-09-17 11:55:20', '2026-09-17 11:55:20');

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父分类ID, 0为顶级',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` int NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '电子产品', 0, 1, 1, '2026-09-17 11:55:20', '2026-09-17 11:55:20');
INSERT INTO `categories` VALUES (2, '图书教材', 0, 2, 1, '2026-09-17 11:55:20', '2026-09-17 11:55:20');
INSERT INTO `categories` VALUES (3, '生活用品', 0, 3, 1, '2026-09-17 11:55:20', '2026-09-17 11:55:20');
INSERT INTO `categories` VALUES (4, '运动户外', 0, 4, 1, '2026-09-17 11:55:20', '2026-09-17 11:55:20');
INSERT INTO `categories` VALUES (5, '服饰鞋包', 0, 5, 1, '2026-09-17 11:55:20', '2026-09-17 11:55:20');
INSERT INTO `categories` VALUES (6, '数码配件', 0, 6, 1, '2026-09-17 11:55:20', '2026-09-17 11:55:20');
INSERT INTO `categories` VALUES (7, '乐器', 0, 7, 1, '2026-09-17 11:55:20', '2026-09-17 11:55:20');
INSERT INTO `categories` VALUES (8, '美妆护肤', 0, 8, 1, '2026-09-17 11:55:20', '2026-09-17 11:55:20');
INSERT INTO `categories` VALUES (9, '食品饮料', 0, 9, 1, '2026-09-17 11:55:20', '2026-09-17 11:55:20');
INSERT INTO `categories` VALUES (10, '其他', 0, 10, 1, '2026-09-17 11:55:20', '2026-09-17 11:55:20');

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_product`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorites
-- ----------------------------
INSERT INTO `favorites` VALUES (1, 1, 2, '2026-05-20 10:30:00');
INSERT INTO `favorites` VALUES (2, 1, 4, '2026-05-21 11:00:00');
INSERT INTO `favorites` VALUES (3, 1, 19, '2026-05-22 15:00:00');
INSERT INTO `favorites` VALUES (4, 2, 1, '2026-05-18 09:00:00');
INSERT INTO `favorites` VALUES (5, 2, 13, '2026-05-19 14:00:00');
INSERT INTO `favorites` VALUES (6, 2, 37, '2026-05-25 08:00:00');
INSERT INTO `favorites` VALUES (7, 3, 5, '2026-05-12 08:00:00');
INSERT INTO `favorites` VALUES (8, 3, 6, '2026-05-13 10:00:00');
INSERT INTO `favorites` VALUES (9, 3, 25, '2026-05-15 16:00:00');
INSERT INTO `favorites` VALUES (10, 4, 3, '2026-05-26 12:00:00');
INSERT INTO `favorites` VALUES (11, 4, 14, '2026-05-28 09:00:00');
INSERT INTO `favorites` VALUES (12, 4, 40, '2026-05-30 11:00:00');
INSERT INTO `favorites` VALUES (13, 5, 8, '2026-05-10 10:00:00');
INSERT INTO `favorites` VALUES (14, 5, 17, '2026-05-22 14:00:00');
INSERT INTO `favorites` VALUES (15, 5, 29, '2026-05-28 09:00:00');
INSERT INTO `favorites` VALUES (16, 6, 1, '2026-05-21 08:00:00');
INSERT INTO `favorites` VALUES (17, 6, 10, '2026-05-27 13:00:00');
INSERT INTO `favorites` VALUES (18, 6, 22, '2026-05-25 16:00:00');
INSERT INTO `favorites` VALUES (19, 7, 13, '2026-05-20 07:00:00');
INSERT INTO `favorites` VALUES (20, 7, 25, '2026-05-22 11:00:00');
INSERT INTO `favorites` VALUES (21, 8, 3, '2026-05-30 15:00:00');
INSERT INTO `favorites` VALUES (22, 8, 37, '2026-05-28 18:00:00');
INSERT INTO `favorites` VALUES (23, 9, 40, '2026-05-16 10:00:00');
INSERT INTO `favorites` VALUES (24, 9, 19, '2026-05-18 14:00:00');
INSERT INTO `favorites` VALUES (25, 10, 2, '2026-05-22 09:00:00');
INSERT INTO `favorites` VALUES (26, 10, 21, '2026-05-25 07:00:00');
INSERT INTO `favorites` VALUES (27, 11, 4, '2026-05-24 16:00:00');
INSERT INTO `favorites` VALUES (28, 11, 36, '2026-06-01 08:00:00');
INSERT INTO `favorites` VALUES (29, 12, 14, '2026-05-25 12:00:00');
INSERT INTO `favorites` VALUES (30, 12, 23, '2026-05-27 15:00:00');
INSERT INTO `favorites` VALUES (31, 1, 33, '2026-09-17 15:43:46');

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_user_id` bigint NOT NULL COMMENT '发送者ID',
  `to_user_id` bigint NOT NULL COMMENT '接收者ID',
  `product_id` bigint NULL DEFAULT NULL COMMENT '关联商品ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `type` int NULL DEFAULT 1 COMMENT '消息类型: 1文本 2图片',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'TEXT' COMMENT '消息类别: TEXT/IMAGE/VIDEO',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话ID',
  `sender_nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称(快照)',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片URL',
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频URL',
  `video_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频标题',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `is_read` int NULL DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_from_user`(`from_user_id` ASC) USING BTREE,
  INDEX `idx_to_user`(`to_user_id` ASC) USING BTREE,
  INDEX `idx_product`(`product_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_session`(`session_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (1, 1, 2, 1, '你好，iPad还在吗？', 1, 'TEXT', 'session_1_2', '小明', NULL, NULL, NULL, '2026-05-20 12:00:00', 1, NULL, '2026-05-20 12:00:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (2, 2, 1, 1, '在的，有什么问题吗？', 1, 'TEXT', 'session_1_2', '小红', NULL, NULL, NULL, '2026-05-20 12:05:00', 1, NULL, '2026-05-20 12:05:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (3, 1, 2, 1, '能再便宜一点吗？', 1, 'TEXT', 'session_1_2', '小明', NULL, NULL, NULL, '2026-05-20 12:08:00', 1, '2026-09-17 13:38:36', '2026-05-20 12:08:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (4, 2, 1, 1, '最低3100，送保护壳', 1, 'TEXT', 'session_1_2', '小红', NULL, NULL, NULL, '2026-05-20 12:10:00', 1, NULL, '2026-05-20 12:10:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (5, 3, 1, 2, '请问笔记本还在吗', 1, 'TEXT', 'session_3_1', '小刚', NULL, NULL, NULL, '2026-05-21 09:00:00', 1, '2026-09-17 13:36:09', '2026-05-21 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (6, 1, 3, 2, '在的，可以来宿舍看机', 1, 'TEXT', 'session_3_1', '小明', NULL, NULL, NULL, '2026-05-21 09:30:00', 1, NULL, '2026-05-21 09:30:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (7, 3, 1, 2, '好，今天下午2点方便吗', 1, 'TEXT', 'session_3_1', '小刚', NULL, NULL, NULL, '2026-05-21 09:35:00', 1, '2026-09-17 13:36:07', '2026-05-21 09:35:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (8, 1, 3, 2, '可以，到时联系', 1, 'TEXT', 'session_3_1', '小明', NULL, NULL, NULL, '2026-05-21 09:40:00', 1, NULL, '2026-05-21 09:40:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (9, 5, 6, 6, '书还在吗？我想要那两本计算机书', 1, 'TEXT', 'session_5_6', '小张', NULL, NULL, NULL, '2026-05-15 14:00:00', 0, NULL, '2026-05-15 14:00:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (10, 6, 5, 6, '在的，笔记很详细', 1, 'TEXT', 'session_5_6', '小王', NULL, NULL, NULL, '2026-05-15 14:30:00', 0, NULL, '2026-05-15 14:30:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (11, 8, 13, 13, '自行车几成新？', 1, 'TEXT', 'session_8_13', '小刘', NULL, NULL, NULL, '2026-05-18 10:00:00', 1, NULL, '2026-05-18 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (12, 13, 8, 13, '八成新，一切功能正常', 1, 'TEXT', 'session_8_13', '小吴', NULL, NULL, NULL, '2026-05-18 10:15:00', 1, NULL, '2026-05-18 10:15:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (13, 15, 25, 25, '吉他换的什么弦？', 1, 'TEXT', 'session_15_25', '小黄', NULL, NULL, NULL, '2026-05-17 16:00:00', 0, NULL, '2026-05-17 16:00:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (14, 12, 19, 19, '请问包包还在吗？什么颜色的', 1, 'TEXT', 'session_12_19', '小吴', NULL, NULL, NULL, '2026-05-20 09:00:00', 1, NULL, '2026-05-20 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (15, 19, 12, 19, '在的，棕色真皮，可以看实物', 1, 'TEXT', 'session_12_19', '小徐', NULL, NULL, NULL, '2026-05-20 09:15:00', 1, NULL, '2026-05-20 09:15:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (16, 12, 19, 19, '明天中午方便吗', 1, 'TEXT', 'session_12_19', '小吴', NULL, NULL, NULL, '2026-05-20 09:20:00', 1, NULL, '2026-05-20 09:20:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (17, 17, 37, 37, '塞尔达好玩吗？通关多久？', 1, 'TEXT', 'session_17_37', '小林', NULL, NULL, NULL, '2026-05-22 11:00:00', 0, NULL, '2026-05-22 11:00:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (18, 4, 40, 40, '音响可以蓝牙连接电脑吗', 1, 'TEXT', 'session_4_40', '小李', NULL, NULL, NULL, '2026-05-16 12:00:00', 1, NULL, '2026-05-16 12:00:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (19, 20, 4, 40, '可以的，电脑手机都支持', 1, 'TEXT', 'session_4_40', '测试用户', NULL, NULL, NULL, '2026-05-16 12:15:00', 1, NULL, '2026-05-16 12:15:00', '2026-09-17 11:55:20');
INSERT INTO `messages` VALUES (20, 10, 14, 14, '鞋子码数偏大还是偏小', 1, 'TEXT', 'session_10_14', '小孙', NULL, NULL, NULL, '2026-05-24 09:00:00', 0, NULL, '2026-05-24 09:00:00', '2026-09-17 11:55:20');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '交易金额',
  `status` int NULL DEFAULT 1 COMMENT '状态: 1待确认 2进行中 3已完成 4已取消',
  `buyer_confirm` int NULL DEFAULT 0 COMMENT '买家确认: 0未确认 1已确认',
  `seller_confirm` int NULL DEFAULT 0 COMMENT '卖家确认: 0未确认 1已确认',
  `trade_type` int NULL DEFAULT 1 COMMENT '交易方式: 1校内面交 2快递配送',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货人/联系人',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `campus` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '校区',
  `building` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼栋/宿舍',
  `address_detail` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'ORD20260501001', 3, 1, 1, 3299.00, 3, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-28 15:00:00', '2026-05-22 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (2, 'ORD20260501002', 4, 2, 2, 4999.00, 3, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, '当面交易', NULL, NULL, '2026-05-27 16:00:00', '2026-05-20 14:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (3, 'ORD20260501003', 5, 6, 6, 45.00, 3, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-29 10:00:00', '2026-05-18 12:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (4, 'ORD20260501004', 7, 10, 9, 69.00, 3, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, '台灯很好', NULL, NULL, '2026-06-01 09:00:00', '2026-05-29 07:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (5, 'ORD20260501005', 8, 13, 13, 450.00, 3, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-25 14:00:00', '2026-05-18 08:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (6, 'ORD20260501006', 9, 17, 17, 350.00, 3, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, '很喜欢', NULL, NULL, '2026-05-30 11:00:00', '2026-05-23 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (7, 'ORD20260501007', 10, 25, 25, 450.00, 3, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-20 17:00:00', '2026-05-15 15:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (8, 'ORD20260501008', 11, 5, 5, 88.00, 3, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, '书已收到', NULL, NULL, '2026-05-22 08:00:00', '2026-05-13 11:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (9, 'ORD20260502001', 12, 21, 21, 149.00, 2, 0, 0, 1, NULL, NULL, NULL, NULL, NULL, '微信已沟通', NULL, NULL, NULL, '2026-06-01 08:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (10, 'ORD20260502002', 14, 29, 29, 180.00, 2, 0, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-01 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (11, 'ORD20260502003', 15, 37, 37, 199.00, 2, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, '已付,待发货', NULL, NULL, NULL, '2026-06-02 15:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (12, 'ORD20260502004', 16, 4, 4, 1399.00, 2, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-02 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (13, 'ORD20260502005', 6, 14, 14, 199.00, 2, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-03 12:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (14, 'ORD20260503001', 18, 19, 19, 680.00, 1, 0, 0, 1, NULL, NULL, NULL, NULL, NULL, '请问什么时候方便交易', NULL, NULL, NULL, '2026-06-03 14:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (15, 'ORD20260503002', 2, 40, 40, 199.00, 1, 0, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-03 15:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (16, 'ORD20260503003', 3, 12, 12, 79.00, 1, 0, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-03 16:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (17, 'ORD20260504001', 1, 7, 7, 60.00, 4, 0, 0, 1, NULL, NULL, NULL, NULL, NULL, '不想要了', '买家取消', '2026-05-26 18:00:00', NULL, '2026-05-25 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (18, 'ORD20260504002', 5, 22, 22, 99.00, 4, 0, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '卖家已卖出', '2026-05-28 10:00:00', NULL, '2026-05-27 14:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (19, 'ORD20260504003', 9, 33, 33, 65.00, 4, 0, 0, 1, NULL, NULL, NULL, NULL, NULL, '找到更便宜的', '买家取消', '2026-06-02 20:00:00', NULL, '2026-06-01 08:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (20, 'ORD20260504004', 13, 16, 16, 280.00, 4, 0, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '物品已损坏', '2026-05-30 12:00:00', NULL, '2026-05-29 11:00:00', '2026-09-17 11:55:20');
INSERT INTO `orders` VALUES (21, 'ORD202609171337094328', 1, 2, 2, 4999.00, 3, 1, 1, 1, '邓少', '13811110001', '东校区', '6号32', '111', '222', NULL, NULL, '2026-09-17 15:46:45', '2026-09-17 13:37:09', '2026-09-17 13:37:09');
INSERT INTO `orders` VALUES (22, 'ORD202609171544085918', 1, 33, 33, 65.00, 4, 0, 0, 1, '邓少', '13811110001', '主校区', '6楼3层501', '门口', '下午', '不想要了', '2026-09-17 15:44:32', NULL, '2026-09-17 15:44:08', '2026-09-17 15:44:08');

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商品描述',
  `price` decimal(10, 2) NOT NULL COMMENT '售价',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片(JSON数组)',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `status` int NULL DEFAULT 1 COMMENT '状态: 1在售 2已预订 3已售出 0已下架',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌',
  `model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '型号',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '颜色',
  `size` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '尺寸',
  `purchase_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '购买时间',
  `condition` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '成色: 全新/九成新/八成新等',
  `purchase_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '购买地点',
  `trade_notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '交易说明',
  `is_negotiable` bit(1) NULL DEFAULT b'0' COMMENT '是否可议价',
  `include_invoice` bit(1) NULL DEFAULT b'0' COMMENT '是否包含发票',
  `include_warranty` bit(1) NULL DEFAULT b'0' COMMENT '是否包含保修',
  `tags` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签,逗号分隔',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_price`(`price` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  FULLTEXT INDEX `ft_title_desc`(`title`, `description`)
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of products
-- ----------------------------
INSERT INTO `products` VALUES (1, 1, 1, '九成新 iPad Air 5 64G 星空色', '2024年购入，主要用于看网课和记笔记，屏幕完美无划痕，电池健康度95%，配件齐全含原装充电器和保护壳。因换Pro所以出。', 3299.00, 4399.00, '[\"/demo-images/ipad_air5_01.jpg\",\"/demo-images/ipad_air5_02.jpg\"]', 290, 0, 'Apple', 'iPad Air 5', NULL, NULL, '2024年3月', '九成新', NULL, NULL, b'0', b'0', b'0', '平板,苹果,iPad', '2026-05-20 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (2, 2, 1, '戴尔 XPS 15 笔记本 i7 16G 512G', '2023年款，办公学习利器，15.6寸4K触摸屏，i7-13700H处理器，16G内存，512G固态，独显RTX3050。外观轻微使用痕迹，功能一切正常。', 4999.00, 8999.00, '[\"/demo-images/xps15_01.jpg\",\"/demo-images/xps15_02.jpg\",\"/demo-images/xps15_03.jpg\"]', 517, 3, 'Dell', 'XPS 15 9530', NULL, NULL, '2023年9月', '八成新', NULL, NULL, b'0', b'0', b'0', '笔记本,戴尔,XPS,高性能', '2026-05-18 14:00:00', '2026-09-17 13:37:09');
INSERT INTO `products` VALUES (3, 3, 1, '索尼 WH-1000XM5 头戴式降噪耳机 黑色', '刚买三个月，几乎全新，头戴舒适降噪效果顶级，续航30小时。配件箱说全，因入了XM6故出。', 1799.00, 2499.00, '[\"/demo-images/sony_xm5_01.jpg\"]', 198, 2, 'Sony', 'WH-1000XM5', NULL, NULL, '2026年3月', '九五新', NULL, NULL, b'0', b'0', b'0', '耳机,索尼,降噪,头戴', '2026-05-25 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (4, 4, 1, '全新未拆封 AirPods Pro 2', '年会奖品，全新未拆封，正品国行，Type-C接口版本，支持主动降噪和通透模式。', 1399.00, 1899.00, '[\"/demo-images/airpods_pro2_01.jpg\"]', 366, 1, 'Apple', 'AirPods Pro 2', NULL, NULL, '2026年1月', '全新', NULL, NULL, b'0', b'0', b'0', '耳机,苹果,AirPods,降噪', '2026-05-22 16:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (5, 5, 2, '考研数学全套（张宇+李永乐+汤家凤）', '2025考研已上岸，出全套数学资料。张宇高数18讲+线代9讲+概率9讲，李永乐线代辅导讲义，汤家凤1800题。部分有笔记但很整洁。', 88.00, 256.00, '[\"/demo-images/math_books_01.jpg\"]', 89, 1, NULL, NULL, NULL, NULL, '2024年6月', '七成新', NULL, NULL, b'0', b'0', b'0', '考研,数学,教材,二手书', '2026-05-10 08:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (6, 6, 2, '《算法导论》第三版 + 《剑指Offer》', '计算机专业必读书籍，算法导论英文原版影印，剑指Offer正版。轻微笔记不影响阅读。', 45.00, 128.00, '[\"/demo-images/cs_books_01.jpg\"]', 121, 1, NULL, NULL, NULL, NULL, '2023年9月', '八成新', NULL, NULL, b'0', b'0', b'0', '计算机,算法,面试,编程', '2026-05-12 11:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (7, 7, 2, '新概念英语1-4册全套（含练习册）', '经典英语教材，1-4册课本+练习册+答案，有部分铅笔笔记可擦除。适合自学英语。', 60.00, 198.00, '[\"/demo-images/nce_books_01.jpg\"]', 68, 1, NULL, NULL, NULL, NULL, '2024年3月', '七成新', NULL, NULL, b'0', b'0', b'0', '英语,教材,新概念,自学', '2026-05-15 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (8, 8, 2, '计算机考研408全套资料（王道四本）', '2025考研408上岸，出王道四本（数据结构+计组+操作系统+计网）+历年真题，笔记详细。', 99.00, 268.00, '[\"/demo-images/408_books_01.jpg\"]', 156, 3, NULL, NULL, NULL, NULL, '2024年5月', '六成新', NULL, NULL, b'0', b'0', b'0', '考研,计算机,408,王道', '2026-05-08 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (9, 9, 3, '小米米家台灯1S 白色', '可调节亮度和色温，支持米家APP控制，护眼阅读模式。用了一年，一切正常。', 69.00, 149.00, '[\"/demo-images/lamp_01.jpg\"]', 45, 1, 'Xiaomi', '米家台灯1S', NULL, NULL, '2025年3月', '八成新', NULL, NULL, b'0', b'0', b'0', '台灯,小米,护眼,智能', '2026-05-28 07:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (10, 10, 3, '美的电热水壶 1.7L 不锈钢', '大容量烧水壶，304不锈钢内胆，自动断电。用了半年外观完好。', 35.00, 89.00, '[\"/demo-images/kettle_01.jpg\"]', 32, 1, 'Midea', 'MK-TM1502', NULL, NULL, '2025年12月', '八成新', NULL, NULL, b'0', b'0', b'0', '水壶,电器,厨房,美的', '2026-05-26 12:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (11, 11, 3, '折叠收纳箱 66L 三个装', '搬家闲置，三个大号折叠收纳箱，带盖，加厚PP材质，几乎全新。', 40.00, 89.00, '[\"/demo-images/storage_01.jpg\"]', 28, 1, NULL, NULL, NULL, NULL, '2026年2月', '九成新', NULL, NULL, b'0', b'0', b'0', '收纳,整理,搬家,储物', '2026-05-30 14:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (12, 12, 3, '罗技 K380 蓝牙键盘 粉色', '小巧便携，支持3台设备一键切换，适合iPad和手机使用。电池仓完好，送一对新电池。', 79.00, 199.00, '[\"/demo-images/k380_01.jpg\"]', 93, 1, 'Logitech', 'K380', NULL, NULL, '2025年10月', '九成新', NULL, NULL, b'0', b'0', b'0', '键盘,蓝牙,罗技,办公', '2026-05-20 15:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (13, 13, 4, '迪卡侬 山地自行车 ST100 26寸', '入门级山地车，21速，前后V刹，车架无损伤。送车锁和打气筒。因换公路车闲置。', 450.00, 899.00, '[\"/demo-images/bike_01.jpg\",\"/demo-images/bike_02.jpg\"]', 178, 1, 'Decathlon', 'ST100', NULL, NULL, '2025年5月', '八成新', NULL, NULL, b'0', b'0', b'0', '自行车,山地车,迪卡侬,运动', '2026-05-16 08:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (14, 14, 4, '李宁 篮球鞋 闪电6 42码', '穿了不到十次，鞋底磨损轻微，包裹性好，缓震效果一流。码数买小了故出。', 199.00, 499.00, '[\"/demo-images/shoes_01.jpg\"]', 64, 1, 'Li-Ning', '闪电6', NULL, NULL, '2026年1月', '九成新', NULL, NULL, b'0', b'0', b'0', '篮球鞋,李宁,运动,42码', '2026-05-22 17:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (15, 15, 4, 'Keep 瑜伽垫 加厚10mm 紫色', '买来只用过两次，几乎全新，防滑加厚款，尺寸183*80cm，送收纳绑带。', 49.00, 129.00, '[\"/demo-images/yogamat_01.jpg\"]', 35, 1, 'Keep', '加厚防滑款', NULL, NULL, '2026年4月', '九五新', NULL, NULL, b'0', b'0', b'0', '瑜伽垫,Keep,健身,运动', '2026-05-29 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (16, 16, 4, '探路者 户外双人帐篷 3-4季通用', '用过一次露营，双层防雨，带防潮垫。搭建简易，收纳方便。', 280.00, 599.00, '[\"/demo-images/tent_01.jpg\"]', 82, 1, 'Toread', '双人帐篷', NULL, NULL, '2025年10月', '九成新', NULL, NULL, b'0', b'0', b'0', '帐篷,露营,户外,探路者', '2026-05-14 13:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (17, 17, 5, 'Nike Air Force 1 经典白 41码', '穿了两个月，鞋面轻微折痕已拍出，其他完好。百搭小白鞋，正品保证。', 350.00, 799.00, '[\"/demo-images/af1_01.jpg\",\"/demo-images/af1_02.jpg\"]', 211, 1, 'Nike', 'Air Force 1', NULL, NULL, '2026年2月', '八成新', NULL, NULL, b'0', b'0', b'0', '耐克,AF1,小白鞋,经典', '2026-05-21 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (18, 18, 5, '优衣库 轻薄羽绒服 男款 L码 黑色', '穿了一个冬天，保暖效果好，重量轻便。干洗过一次，干净整洁。', 159.00, 499.00, '[\"/demo-images/down_jacket_01.jpg\"]', 56, 1, 'UNIQLO', '轻薄羽绒服', NULL, NULL, '2025年11月', '八成新', NULL, NULL, b'0', b'0', b'0', '羽绒服,优衣库,男装,冬季', '2026-05-19 11:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (19, 19, 5, 'Coach 双肩包 棕色真皮', '生日礼物，背了约半年，真皮材质很有质感。容量大可以放15寸笔记本。因换新包出。', 680.00, 2200.00, '[\"/demo-images/coach_bag_01.jpg\",\"/demo-images/coach_bag_02.jpg\"]', 426, 1, 'Coach', '双肩包', NULL, NULL, '2025年9月', '八成新', NULL, NULL, b'0', b'0', b'0', '包,Coach,双肩包,真皮', '2026-05-17 14:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (20, 20, 5, 'Converse 经典款帆布鞋 高帮 39码', '穿了不到五次，几乎全新，经典黑色高帮，百搭款。买大了半码所以出。', 120.00, 369.00, '[\"/demo-images/converse_01.jpg\"]', 78, 1, 'Converse', 'Chuck Taylor', NULL, NULL, '2026年3月', '九五新', NULL, NULL, b'0', b'0', b'0', '匡威,帆布鞋,经典,高帮', '2026-05-25 16:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (21, 21, 6, 'Anker 充电宝 20000mAh 65W', '支持笔记本和手机快充，容量大可以充MacBook一次。带电量显示，使用约一年。', 149.00, 299.00, '[\"/demo-images/powerbank_01.jpg\"]', 167, 1, 'Anker', 'PowerCore 20000', NULL, NULL, '2025年6月', '八成新', NULL, NULL, b'0', b'0', b'0', '充电宝,Anker,快充,大容量', '2026-05-23 08:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (22, 22, 6, '绿联 10合1 Type-C扩展坞', '支持HDMI 4K输出、USB3.0*3、SD/TF读卡、千兆网口、PD充电，MacBook和iPad适用。', 99.00, 199.00, '[\"/demo-images/hub_01.jpg\"]', 134, 1, 'UGREEN', '10合1扩展坞', NULL, NULL, '2025年8月', '九成新', NULL, NULL, b'0', b'0', b'0', '扩展坞,绿联,Type-C,办公', '2026-05-24 12:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (23, 23, 6, 'Sandisk 移动固态硬盘 1TB', '读取速度1050MB/s，小巧便携，附带Type-C和Type-A两根线。仅用不到100小时。', 350.00, 599.00, '[\"/demo-images/ssd_01.jpg\"]', 89, 1, 'SanDisk', 'E61 1TB', NULL, NULL, '2025年12月', '九成新', NULL, NULL, b'0', b'0', b'0', '硬盘,移动硬盘,1TB,SSD', '2026-05-20 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (24, 24, 6, '手机三脚架 + 补光灯 套装', '拍视频和直播套装，三脚架高度可调(60-180cm)，环形补光灯三色温可调。用过两次。', 55.00, 129.00, '[\"/demo-images/tripod_01.jpg\"]', 42, 1, NULL, NULL, NULL, NULL, '2026年3月', '九成新', NULL, NULL, b'0', b'0', b'0', '三脚架,补光灯,直播,摄影', '2026-05-31 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (25, 25, 7, '雅马哈 F310 民谣吉他 41寸 原木色', '大学自学用，练了一年多，音色温暖手感不错。琴身有轻微使用痕迹，已换新弦。送琴包和调音器。', 450.00, 899.00, '[\"/demo-images/guitar_01.jpg\",\"/demo-images/guitar_02.jpg\"]', 210, 1, 'YAMAHA', 'F310', NULL, NULL, '2024年9月', '八成新', NULL, NULL, b'0', b'0', b'0', '吉他,雅马哈,民谣,乐器', '2026-05-13 15:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (26, 26, 7, '卡西欧 CT-S200 电子琴 61键', '入门电子琴，400种音色，77种节奏，支持耳机输出不扰民。带谱架和电源适配器。', 380.00, 699.00, '[\"/demo-images/keyboard_01.jpg\"]', 73, 1, 'CASIO', 'CT-S200', NULL, NULL, '2025年7月', '九成新', NULL, NULL, b'0', b'0', b'0', '电子琴,卡西欧,乐器,音乐', '2026-05-18 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (27, 27, 7, 'K歌麦克风 全民K歌专用 蓝牙版', '蓝牙连接手机，自带混响效果，Type-C充电，续航约6小时。几乎全新。', 55.00, 139.00, '[\"/demo-images/mic_01.jpg\"]', 38, 1, NULL, NULL, NULL, NULL, '2026年4月', '九五新', NULL, NULL, b'0', b'0', b'0', '麦克风,K歌,蓝牙,娱乐', '2026-05-30 11:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (28, 28, 7, '架子鼓练习哑鼓垫 + 鼓棒', '静音练习鼓垫，带支架，不扰民。配两副5A鼓棒。练习基本功的神器。', 80.00, 199.00, '[\"/demo-images/drum_01.jpg\"]', 29, 1, NULL, NULL, NULL, NULL, '2025年8月', '八成新', NULL, NULL, b'0', b'0', b'0', '架子鼓,练习,打击乐,静音', '2026-05-27 14:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (29, 29, 8, '兰蔻 粉水 400ml 全新未开封', '朋友从免税店带回，家里囤货太多用不完。全新未开封，正品保证。', 180.00, 320.00, '[\"/demo-images/lancome_01.jpg\"]', 145, 1, 'Lancome', '粉水 400ml', NULL, NULL, '2026年3月', '全新', NULL, NULL, b'0', b'0', b'0', '兰蔻,粉水,护肤,保湿', '2026-05-26 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (30, 30, 8, 'MAC 口红 Rubby Woo 经典红', '仅手臂试色一次，颜色不适合自己。正品，经典蓝调正红，雾面质地。', 88.00, 180.00, '[\"/demo-images/mac_lip_01.jpg\"]', 96, 1, 'MAC', 'Ruby Woo', NULL, NULL, '2026年4月', '九五新', NULL, NULL, b'0', b'0', b'0', '口红,MAC,彩妆,红色', '2026-05-28 15:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (31, 31, 8, 'SK-II 神仙水 230ml 余量约80%', '去年双十一购入，用了两个月不适合肤质，余量约80%。瓶身完好，正品。', 380.00, 690.00, '[\"/demo-images/sk2_01.jpg\"]', 267, 2, 'SK-II', '神仙水 230ml', NULL, NULL, '2025年11月', '八成新', NULL, NULL, b'0', b'0', b'0', 'SK-II,神仙水,精华,护肤', '2026-05-15 13:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (32, 32, 8, '完美日记 眼影盘 小猫盘', '用过三四次，颜色不太适合。9色盘，粉质细腻显色。几乎全新。', 35.00, 89.00, '[\"/demo-images/eyeshad_01.jpg\"]', 52, 1, 'PERFECT DIARY', '小猫盘', NULL, NULL, '2026年2月', '八成新', NULL, NULL, b'0', b'0', b'0', '眼影,完美日记,彩妆,国货', '2026-05-24 16:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (33, 33, 9, '瑞士莲 黑巧克力 混合装 500g 未开封', '朋友从瑞士带回的伴手礼，家里巧克力太多。全新未开封，保质期到2027年。', 65.00, 128.00, '[\"/demo-images/choco_01.jpg\"]', 43, 1, 'Lindt', '黑巧克力混合装', NULL, NULL, '2026年5月', '全新', NULL, NULL, b'0', b'0', b'0', '巧克力,瑞士莲,零食,进口', '2026-05-31 08:00:00', '2026-09-17 15:44:08');
INSERT INTO `products` VALUES (34, 34, 9, '星巴克 咖啡豆 哥伦比亚 中度烘焙 1kg', '公司发的福利，家里不喝咖啡。全新未开封，保质期到2027年3月。', 99.00, 198.00, '[\"/demo-images/coffee_01.jpg\"]', 33, 1, 'Starbucks', '哥伦比亚咖啡豆', NULL, NULL, '2026年4月', '全新', NULL, NULL, b'0', b'0', b'0', '咖啡,星巴克,咖啡豆,哥伦比亚', '2026-05-29 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (35, 35, 9, '三只松鼠 坚果大礼包 2kg 未拆', '过年收到的年货，一直没拆。包含夏威夷果、碧根果、巴旦木等多种坚果。保质期到2026年12月。', 55.00, 129.00, '[\"/demo-images/nuts_01.jpg\"]', 27, 1, 'Squirrel', '坚果大礼包', NULL, NULL, '2026年1月', '全新', NULL, NULL, b'0', b'0', b'0', '坚果,零食,三只松鼠,年货', '2026-05-27 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (36, 36, 9, 'Whittard 英式红茶 经典款 80包', '英国带回来的经典红茶，家里茶叶太多。全新未开封，保质期到2027年。', 75.00, 159.00, '[\"/demo-images/tea_01.jpg\"]', 19, 1, 'Whittard', 'English Breakfast', NULL, NULL, '2026年3月', '全新', NULL, NULL, b'0', b'0', b'0', '红茶,英式,茶叶,进口', '2026-05-30 14:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (37, 37, 10, '正版 Switch 游戏 塞尔达传说王国之泪', '通关后闲置，卡带完好，带原盒。游戏时长约120小时，自用一手。', 199.00, 369.00, '[\"/demo-images/zelda_01.jpg\"]', 342, 1, 'Nintendo', '王国之泪', NULL, NULL, '2025年5月', '九成新', NULL, NULL, b'0', b'0', b'0', 'Switch,塞尔达,游戏,NS', '2026-05-21 08:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (38, 38, 10, 'Funko Pop 漫威 钢铁侠 限定款', '收藏品，开盒检视后回盒保存，品相完美。限量版编号1382。', 220.00, 399.00, '[\"/demo-images/funko_01.jpg\"]', 187, 1, 'Funko Pop', '钢铁侠限定', NULL, NULL, '2025年11月', '全新仅拆', NULL, NULL, b'0', b'0', b'0', '手办,Funko,漫威,钢铁侠', '2026-05-19 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (39, 39, 10, '12寸手绘数字油画 风景系列 三幅', '买了想做装饰，一直没时间。三幅全新未拆，含颜料和画笔。', 45.00, 99.00, '[\"/demo-images/paint_01.jpg\"]', 23, 1, NULL, NULL, NULL, NULL, '2026年2月', '全新', NULL, NULL, b'0', b'0', b'0', '油画,手工,装饰,DYI', '2026-05-25 12:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (40, 40, 10, '猫王复古蓝牙音箱 胡桃木色', '音质出色，颜值担当，支持蓝牙5.0和FM收音。包装盒在，几乎全新。', 199.00, 399.00, '[\"/demo-images/speaker_01.jpg\",\"/demo-images/speaker_02.jpg\"]', 313, 1, '猫王', '小王子OTR', NULL, NULL, '2025年10月', '九五新', NULL, NULL, b'0', b'0', b'0', '音箱,蓝牙,复古,猫王', '2026-05-14 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `products` VALUES (41, 1, 10, '九九成新棉被', '某宝购买，没用几次 全新', 120.00, 200.00, '[\"products/720c92ef1c4e4fbd8c0b4205b02d2735.png\"]', 2, 1, NULL, NULL, NULL, NULL, NULL, '九五新', NULL, NULL, b'0', b'0', b'0', NULL, '2026-09-17 15:45:27', '2026-09-17 15:45:27');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` int NULL DEFAULT 1 COMMENT '状态: 1正常 0禁用',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'WebSocket会话ID',
  `online` bit(1) NULL DEFAULT b'0' COMMENT '是否在线',
  `last_active_time` datetime(6) NULL DEFAULT NULL COMMENT '最后活跃时间',
  `banned` bit(1) NULL DEFAULT b'0' COMMENT '是否被封禁',
  `ban_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封禁原因',
  `ban_expire_time` datetime(6) NULL DEFAULT NULL COMMENT '封禁到期时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'dengshao', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'dengshao@qq.com', '邓少', '13811110001', 'avatars/avatar01.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-01 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (2, 'xiaohong', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaohong@qq.com', '小红', '13811110002', 'avatars/avatar02.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-02 11:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (3, 'xiaogang', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaogang@qq.com', '小刚', '13811110003', 'avatars/avatar03.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-03 09:30:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (4, 'xiaoli', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaoli@qq.com', '小李', '13811110004', 'avatars/avatar04.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-04 14:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (5, 'xiaozhang', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaozhang@qq.com', '小张', '13811110005', 'avatars/avatar05.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-05 16:20:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (6, 'xiaowang', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaowang@qq.com', '小王', '13811110006', 'avatars/avatar01.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-06 08:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (7, 'xiaochen', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaochen@qq.com', '小陈', '13811110007', 'avatars/avatar02.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-07 12:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (8, 'xiaoliu', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaoliu@qq.com', '小刘', '13811110008', 'avatars/avatar03.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-08 10:30:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (9, 'xiaozhao', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaozhao@qq.com', '小赵', '13811110009', 'avatars/avatar04.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-09 15:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (10, 'xiaosun', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaosun@qq.com', '小孙', '13811110010', 'avatars/avatar05.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-10 09:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (11, 'xiaozhou', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaozhou@qq.com', '小周', '13811110011', 'avatars/avatar01.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-11 11:30:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (12, 'xiaowu', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaowu@qq.com', '小吴', '13811110012', 'avatars/avatar02.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-12 14:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (13, 'xiaoxu', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaoxu@qq.com', '小徐', '13811110013', 'avatars/avatar03.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-13 16:30:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (14, 'xiaoma', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaoma@qq.com', '小马', '13811110014', 'avatars/avatar04.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-14 10:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (15, 'xiaohuang', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaohuang@qq.com', '小黄', '13811110015', 'avatars/avatar05.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-15 13:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (16, 'xiaoyang', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaoyang@qq.com', '小杨', '13811110016', 'avatars/avatar01.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-16 15:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (17, 'xiaolin', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaolin@qq.com', '小林', '13811110017', 'avatars/avatar02.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-17 08:30:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (18, 'xiaohu', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaohu@qq.com', '小胡', '13811110018', 'avatars/avatar03.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-05-18 17:00:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (19, 'xiaozheng', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'xiaozheng@qq.com', '小郑', '13811110019', 'avatars/avatar04.png', 1, 'USER', NULL, b'0', NULL, b'0', '违规发布虚假信息', NULL, '2026-05-19 09:30:00', '2026-09-17 11:55:20');
INSERT INTO `users` VALUES (20, 'testuser', '$2b$10$qULJj6ELVXMXNIcfvNkoWeD4L8XXGg6Sy5tplpCffkJ/lJJpwFnj6', 'test@campus.com', '测试用户', '13800000000', 'avatars/default-avatar.png', 1, 'USER', NULL, b'0', NULL, b'0', NULL, NULL, '2026-06-01 00:00:00', '2026-09-17 11:55:20');

SET FOREIGN_KEY_CHECKS = 1;
