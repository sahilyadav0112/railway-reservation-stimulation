-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: railway_reservation
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `booking`
--

DROP TABLE IF EXISTS `booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking` (
  `b_id` int NOT NULL,
  `pnr` varchar(50) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `train_id` int DEFAULT NULL,
  `journey_date` date DEFAULT NULL,
  `class_id` int DEFAULT NULL,
  `total_fare` decimal(10,2) DEFAULT NULL,
  `Booking_date` datetime DEFAULT NULL,
  `booking_status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`b_id`),
  UNIQUE KEY `pnr` (`pnr`),
  KEY `user_id` (`user_id`),
  KEY `train_id` (`train_id`),
  KEY `class_id` (`class_id`),
  CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`),
  CONSTRAINT `booking_ibfk_2` FOREIGN KEY (`train_id`) REFERENCES `trains` (`t_id`),
  CONSTRAINT `booking_ibfk_3` FOREIGN KEY (`class_id`) REFERENCES `train_classes` (`c_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking`
--

LOCK TABLES `booking` WRITE;
/*!40000 ALTER TABLE `booking` DISABLE KEYS */;
INSERT INTO `booking` VALUES (1,'8423622558',3,1,'2026-09-25',3,2100.00,'2026-10-03 14:01:48','CONFIRMED');
/*!40000 ALTER TABLE `booking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking_passengers`
--

DROP TABLE IF EXISTS `booking_passengers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking_passengers` (
  `booking_id` int NOT NULL,
  `p_id` int NOT NULL,
  `seat_id` int DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`booking_id`,`p_id`),
  KEY `p_id` (`p_id`),
  KEY `seat_id` (`seat_id`),
  CONSTRAINT `booking_passengers_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`b_id`),
  CONSTRAINT `booking_passengers_ibfk_2` FOREIGN KEY (`p_id`) REFERENCES `passenger` (`p_id`),
  CONSTRAINT `booking_passengers_ibfk_3` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`seat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking_passengers`
--

LOCK TABLES `booking_passengers` WRITE;
/*!40000 ALTER TABLE `booking_passengers` DISABLE KEYS */;
INSERT INTO `booking_passengers` VALUES (1,1,1,'CONFIRMED'),(1,2,2,'CONFIRMED'),(1,3,3,'CONFIRMED');
/*!40000 ALTER TABLE `booking_passengers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coaches`
--

DROP TABLE IF EXISTS `coaches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coaches` (
  `coach_id` int NOT NULL,
  `t_id` int DEFAULT NULL,
  `coach_number` varchar(10) DEFAULT NULL,
  `class_id` int DEFAULT NULL,
  `total_seats` int DEFAULT NULL,
  PRIMARY KEY (`coach_id`),
  KEY `t_id` (`t_id`),
  KEY `class_id` (`class_id`),
  CONSTRAINT `coaches_ibfk_1` FOREIGN KEY (`t_id`) REFERENCES `trains` (`t_id`),
  CONSTRAINT `coaches_ibfk_2` FOREIGN KEY (`class_id`) REFERENCES `train_classes` (`c_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coaches`
--

LOCK TABLES `coaches` WRITE;
/*!40000 ALTER TABLE `coaches` DISABLE KEYS */;
INSERT INTO `coaches` VALUES (1,1,'B1',3,72),(2,1,'B2',3,72),(3,1,'A1',2,48),(4,1,'H1',1,24);
/*!40000 ALTER TABLE `coaches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `passenger`
--

DROP TABLE IF EXISTS `passenger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `passenger` (
  `p_id` int NOT NULL,
  `user_id` int DEFAULT NULL,
  `p_name` varchar(100) NOT NULL,
  `p_age` int DEFAULT NULL,
  `p_gender` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`p_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `passenger_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `passenger`
--

LOCK TABLES `passenger` WRITE;
/*!40000 ALTER TABLE `passenger` DISABLE KEYS */;
INSERT INTO `passenger` VALUES (1,NULL,'Abhishek',20,'Male'),(2,3,'Saini Yadav',19,'Female'),(3,NULL,'Rahi',54,'Female');
/*!40000 ALTER TABLE `passenger` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `payment_id` int NOT NULL,
  `b_id` int DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `payment_method` varchar(50) NOT NULL,
  `payment_status` varchar(20) NOT NULL,
  `payment_date` datetime DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `b_id` (`b_id`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`b_id`) REFERENCES `booking` (`b_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1,1,2100.00,'UPI','SUCCESS','2026-10-03 14:09:31');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seat`
--

DROP TABLE IF EXISTS `seat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seat` (
  `seat_id` int NOT NULL,
  `coach_id` int DEFAULT NULL,
  `seat_number` varchar(10) DEFAULT NULL,
  `seat_type` varchar(25) DEFAULT NULL,
  `status` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`seat_id`),
  KEY `coach_id` (`coach_id`),
  CONSTRAINT `seat_ibfk_1` FOREIGN KEY (`coach_id`) REFERENCES `coaches` (`coach_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seat`
--

LOCK TABLES `seat` WRITE;
/*!40000 ALTER TABLE `seat` DISABLE KEYS */;
INSERT INTO `seat` VALUES (1,1,'1','Lower','BOOKED'),(2,1,'2','Upper','BOOKED'),(3,1,'3','Lower','BOOKED'),(4,1,'4','Upper','AVAILABLE'),(5,1,'5','Side Lower','AVAILABLE'),(6,1,'6','Side Upper','AVAILABLE'),(7,1,'7','Lower','AVAILABLE'),(8,1,'8','Upper','AVAILABLE'),(9,1,'9','Lower','AVAILABLE'),(10,1,'10','Upper','AVAILABLE');
/*!40000 ALTER TABLE `seat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stations`
--

DROP TABLE IF EXISTS `stations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stations` (
  `station_id` int NOT NULL,
  `station_code` varchar(50) NOT NULL,
  `station_name` varchar(100) NOT NULL,
  `City` varchar(100) DEFAULT NULL,
  `State` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`station_id`),
  UNIQUE KEY `station_code` (`station_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stations`
--

LOCK TABLES `stations` WRITE;
/*!40000 ALTER TABLE `stations` DISABLE KEYS */;
INSERT INTO `stations` VALUES (1,'CSMT','Chhatrapati Shivaji Maharaj Terminus','Mumbai','Maharashtra'),(2,'ST','Surat Railway Station','Surat','Gujarat'),(3,'BRC','Vadodara Junction','Vadodara','Gujarat'),(4,'NDLS','New Delhi Railway Station','Lajpat Nagar','Delhi'),(5,'BN','Bandra','Mumbai','Maharashtra');
/*!40000 ALTER TABLE `stations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `train_classes`
--

DROP TABLE IF EXISTS `train_classes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `train_classes` (
  `c_id` int NOT NULL,
  `c_no` varchar(15) DEFAULT NULL,
  `c_name` varchar(100) DEFAULT NULL,
  `base_fare` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`c_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `train_classes`
--

LOCK TABLES `train_classes` WRITE;
/*!40000 ALTER TABLE `train_classes` DISABLE KEYS */;
INSERT INTO `train_classes` VALUES (1,'1A','AC First Class',4500.00),(2,'2A','AC 2 Tier',3200.00),(3,'3A','AC 3 Tier',2100.00),(4,'SL','Sleeper Class',900.50);
/*!40000 ALTER TABLE `train_classes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `train_stations`
--

DROP TABLE IF EXISTS `train_stations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `train_stations` (
  `t_id` int NOT NULL,
  `station_id` int NOT NULL,
  `sequence_no` int NOT NULL,
  `arrival_t` time DEFAULT NULL,
  `departure_t` time DEFAULT NULL,
  PRIMARY KEY (`t_id`,`station_id`),
  KEY `station_id` (`station_id`),
  CONSTRAINT `train_stations_ibfk_1` FOREIGN KEY (`t_id`) REFERENCES `trains` (`t_id`),
  CONSTRAINT `train_stations_ibfk_2` FOREIGN KEY (`station_id`) REFERENCES `stations` (`station_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `train_stations`
--

LOCK TABLES `train_stations` WRITE;
/*!40000 ALTER TABLE `train_stations` DISABLE KEYS */;
INSERT INTO `train_stations` VALUES (1,1,1,NULL,'16:35:00'),(1,2,2,'19:30:00','19:35:00'),(1,3,3,'21:00:00','21:05:00'),(1,4,4,'08:15:00',NULL);
/*!40000 ALTER TABLE `train_stations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trains`
--

DROP TABLE IF EXISTS `trains`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trains` (
  `t_id` int NOT NULL,
  `t_name` varchar(50) NOT NULL,
  `t_no` varchar(10) NOT NULL,
  `t_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`t_id`),
  UNIQUE KEY `t_no` (`t_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trains`
--

LOCK TABLES `trains` WRITE;
/*!40000 ALTER TABLE `trains` DISABLE KEYS */;
INSERT INTO `trains` VALUES (1,'Mumbai Rajdhani','12951','Rajdhani Express'),(2,'August Kranti Rajdhani','12953','Rajdhani Express'),(3,'Mumbai Central Delhi Express','12903','Superfast Express'),(4,'Vandhe Bharat','15113','Superfast Express'),(5,'Kamayani Express','11156','Express');
/*!40000 ALTER TABLE `trains` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `u_id` int NOT NULL AUTO_INCREMENT,
  `u_name` varchar(100) NOT NULL,
  `u_phone` varchar(25) DEFAULT NULL,
  `u_email` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`u_id`),
  UNIQUE KEY `u_email` (`u_email`),
  UNIQUE KEY `password` (`password`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Sahil','9876543210','sahil@gmail.com','sahil123','2026-09-29 20:37:51'),(2,'Ritika','7977458441','ritika@gmail.com','ritikas','2026-09-29 20:38:55'),(3,'Saini Yadav','9967023924','sainiy@gmail.com','456saini','2026-09-29 20:40:12');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-03 14:28:35
