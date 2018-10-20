/*
Navicat MariaDB Data Transfer

Source Server         : Proj
Source Server Version : 100130
Source Host           : localhost:3306
Source Database       : instantmsg

Target Server Type    : MariaDB
Target Server Version : 100130
File Encoding         : 65001

Date: 2018-10-21 00:24:14
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `serial_num` int(255) NOT NULL AUTO_INCREMENT,
  `timestamp` varchar(255) NOT NULL,
  `type` int(5) NOT NULL,
  `content` varchar(2000) NOT NULL,
  PRIMARY KEY (`serial_num`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES ('1', '123', '0', '1');
INSERT INTO `notice` VALUES ('2', '123', '0', '2');
INSERT INTO `notice` VALUES ('3', '123', '0', '3');
INSERT INTO `notice` VALUES ('4', '123', '0', '4');
INSERT INTO `notice` VALUES ('5', '123', '0', '5');

-- ----------------------------
-- Table structure for offline_msg
-- ----------------------------
DROP TABLE IF EXISTS `offline_msg`;
CREATE TABLE `offline_msg` (
  `to` int(255) NOT NULL,
  `serial_num` int(255) NOT NULL,
  `from` int(255) NOT NULL,
  `timestamp` varchar(255) NOT NULL,
  `content` varchar(2000) NOT NULL,
  PRIMARY KEY (`to`,`serial_num`),
  KEY `from` (`from`),
  CONSTRAINT `offline_msg_ibfk_1` FOREIGN KEY (`from`) REFERENCES `user` (`user_id`),
  CONSTRAINT `offline_msg_ibfk_2` FOREIGN KEY (`to`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of offline_msg
-- ----------------------------

-- ----------------------------
-- Table structure for relationship
-- ----------------------------
DROP TABLE IF EXISTS `relationship`;
CREATE TABLE `relationship` (
  `from` int(255) NOT NULL,
  `to` int(255) NOT NULL,
  `relationship` int(5) NOT NULL,
  `judge` int(5) NOT NULL,
  KEY `from_id` (`from`),
  KEY `to_id` (`to`),
  CONSTRAINT `from_id` FOREIGN KEY (`from`) REFERENCES `user` (`user_id`),
  CONSTRAINT `to_id` FOREIGN KEY (`to`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of relationship
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` int(255) NOT NULL AUTO_INCREMENT,
  `credential` varchar(50) NOT NULL,
  `credential_type` int(5) NOT NULL,
  `hashed_password` varchar(255) NOT NULL,
  `group` int(5) NOT NULL,
  `nickname` varchar(255) NOT NULL,
  `sign` varchar(2000) NOT NULL DEFAULT '',
  `gender` int(2) NOT NULL DEFAULT '0',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100003 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'admin@system.com', '0', '0', '0', '0', '0', '0');
INSERT INTO `user` VALUES ('100000', 'fakeyw@163.com', '0', '111111', '0', 'fakeyw', '', '0');
