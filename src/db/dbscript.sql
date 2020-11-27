CREATE DATABASE JDBC_POS_DEP5;

USE JDBC_POS_DEP5;

DROP TABLE IF EXISTS `Customer`;
CREATE TABLE `Customer` (
  `id` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `address` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO `Customer` VALUES ('C001','Kamal','Galle'),('C002','Nimal','Matara'),('C003','Anil','Panadura'),('C004','Sunil','Kaluthara'),('C005','Wimal','Kandy'),('C010','Suranga','Suranga');

CREATE TABLE `Item` (
  `code` varchar(10) NOT NULL,
  `description` varchar(30) DEFAULT NULL,
  `unitPrice` decimal(6,2) DEFAULT NULL,
  `qtyOnHand` int DEFAULT NULL,
  PRIMARY KEY (`code`)
);

INSERT INTO `Item` VALUES ('I001','Dhal',125.00,100),('I002','Rathu Kakulu',65.00,100),('I003','Sugar',110.00,70),('I004','Anchor',375.00,120),('I005','Dhal',125.00,100),('I006','Soap',35.00,200),('I007','Biscuit',45.00,70),('I008','Sudu Kakulu',62.00,100),('I009','Chilie Powder',250.00,42),('I010','Sauce',145.00,50);

CREATE TABLE `Order` (
  `id` varchar(10) NOT NULL,
  `date` date DEFAULT NULL,
  `customerId` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `customerId` (`customerId`),
  CONSTRAINT `Order_ibfk_1` FOREIGN KEY (`customerId`) REFERENCES `Customer` (`id`)
);


INSERT INTO `Order` VALUES ('OD001','2013-02-12','C001'),('OD002','2013-02-11','C002');

CREATE TABLE `OrderDetail` (
  `orderId` varchar(10) NOT NULL,
  `itemCode` varchar(10) NOT NULL,
  `qty` int DEFAULT NULL,
  `unitPrice` decimal(6,2) DEFAULT NULL,
  PRIMARY KEY (`orderId`,`itemCode`),
  KEY `itemCode` (`itemCode`),
  CONSTRAINT `OrderDetail_ibfk_1` FOREIGN KEY (`orderId`) REFERENCES `Order` (`id`),
  CONSTRAINT `OrderDetail_ibfk_2` FOREIGN KEY (`itemCode`) REFERENCES `Item` (`code`)
);

INSERT INTO `OrderDetail` VALUES ('OD001','I001',2,125.00),('OD001','I002',5,65.00),('OD001','I003',1,110.00),('OD001','I007',2,45.00),('OD001','I010',1,145.00),('OD002','I003',2,110.00),('OD002','I008',3,62.00),('OD002','I010',2,145.00);
