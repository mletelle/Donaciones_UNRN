CREATE DATABASE  IF NOT EXISTS `seminario_2025_1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `seminario_2025_1`;
-- MySQL dump 10.13  Distrib 8.0.36, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: seminario_2025_1
-- ------------------------------------------------------
-- Server version	8.0.43-0ubuntu0.24.04.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bienes`
--

DROP TABLE IF EXISTS `bienes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bienes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_pedido_donacion` int NOT NULL,
  `categoria` int NOT NULL,
  `cantidad` int NOT NULL,
  `tipo` int NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  `estado_inventario` varchar(20) DEFAULT 'PENDIENTE',
  `id_orden_entrega` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_bien_pedido_idx` (`id_pedido_donacion`),
  KEY `fk_bienes_orden_entrega` (`id_orden_entrega`),
  CONSTRAINT `fk_bien_pedido` FOREIGN KEY (`id_pedido_donacion`) REFERENCES `pedidos_donacion` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_bienes_orden_entrega` FOREIGN KEY (`id_orden_entrega`) REFERENCES `ordenes_entrega` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bienes`
--

LOCK TABLES `bienes` WRITE;
/*!40000 ALTER TABLE `bienes` DISABLE KEYS */;
INSERT INTO `bienes` VALUES (17,13,1,1,1,'Remera',NULL,'EN_STOCK',NULL),(18,13,1,2,2,'Pantalon',NULL,'EN_STOCK',NULL),(19,14,2,1,1,'Alacena',NULL,'PENDIENTE',NULL),(20,14,5,4,1,'Clavos',NULL,'PENDIENTE',NULL),(21,15,3,12,1,'Latas','2025-11-16','PENDIENTE',NULL),(22,16,6,1,1,'Jugete',NULL,'PENDIENTE',NULL),(23,16,9,2,1,'Herramienta',NULL,'PENDIENTE',NULL),(24,17,1,1,1,'Remera',NULL,'PENDIENTE',NULL),(25,18,5,1,1,'Torno',NULL,'PENDIENTE',NULL),(26,19,1,12,1,'Medias',NULL,'PENDIENTE',NULL),(27,19,3,6,1,'Cerveza','2025-12-16','PENDIENTE',NULL),(28,20,1,24,1,'Medias',NULL,'EN_STOCK',NULL),(29,20,3,16,1,'Latas','2025-12-20','EN_STOCK',NULL);
/*!40000 ALTER TABLE `bienes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ordenes_entrega`
--

DROP TABLE IF EXISTS `ordenes_entrega`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ordenes_entrega` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha_generacion` datetime NOT NULL,
  `fecha_entrega` datetime DEFAULT NULL,
  `estado` varchar(20) NOT NULL,
  `usuario_beneficiario` varchar(45) NOT NULL,
  `usuario_voluntario` varchar(45) DEFAULT NULL,
  `patente_vehiculo` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_oe_beneficiario_idx` (`usuario_beneficiario`),
  KEY `fk_oe_voluntario_idx` (`usuario_voluntario`),
  KEY `fk_oe_vehiculo_idx` (`patente_vehiculo`),
  CONSTRAINT `fk_oe_beneficiario` FOREIGN KEY (`usuario_beneficiario`) REFERENCES `usuarios` (`usuario`) ON UPDATE CASCADE,
  CONSTRAINT `fk_oe_vehiculo` FOREIGN KEY (`patente_vehiculo`) REFERENCES `vehiculos` (`patente`) ON UPDATE CASCADE,
  CONSTRAINT `fk_oe_voluntario` FOREIGN KEY (`usuario_voluntario`) REFERENCES `usuarios` (`usuario`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordenes_entrega`
--

LOCK TABLES `ordenes_entrega` WRITE;
/*!40000 ALTER TABLE `ordenes_entrega` DISABLE KEYS */;
/*!40000 ALTER TABLE `ordenes_entrega` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ordenes_retiro`
--

DROP TABLE IF EXISTS `ordenes_retiro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ordenes_retiro` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha_generacion` datetime NOT NULL,
  `estado` varchar(20) NOT NULL,
  `usuario_voluntario` varchar(45) DEFAULT NULL,
  `patente_vehiculo` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_orden_usuario_idx` (`usuario_voluntario`),
  KEY `fk_orden_vehiculo_idx` (`patente_vehiculo`),
  CONSTRAINT `fk_orden_usuario` FOREIGN KEY (`usuario_voluntario`) REFERENCES `usuarios` (`usuario`) ON UPDATE CASCADE,
  CONSTRAINT `fk_orden_vehiculo` FOREIGN KEY (`patente_vehiculo`) REFERENCES `vehiculos` (`patente`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordenes_retiro`
--

LOCK TABLES `ordenes_retiro` WRITE;
/*!40000 ALTER TABLE `ordenes_retiro` DISABLE KEYS */;
INSERT INTO `ordenes_retiro` VALUES (11,'2025-11-16 20:45:47','PENDIENTE','bgoro','AE 123 CD'),(12,'2025-11-16 20:45:53','COMPLETADA','bgoro','AE 123 CD'),(13,'2025-11-18 19:12:51','COMPLETADA','bgoro','AA 789 GH');
/*!40000 ALTER TABLE `ordenes_retiro` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidos_donacion`
--

DROP TABLE IF EXISTS `pedidos_donacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidos_donacion` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL,
  `tipo_vehiculo` varchar(20) NOT NULL,
  `usuario_donante` varchar(45) NOT NULL,
  `estado` varchar(20) NOT NULL,
  `id_orden_retiro` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_pedido_usuario_idx` (`usuario_donante`),
  KEY `fk_pedido_orden_idx` (`id_orden_retiro`),
  CONSTRAINT `fk_pedido_orden` FOREIGN KEY (`id_orden_retiro`) REFERENCES `ordenes_retiro` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_pedido_usuario` FOREIGN KEY (`usuario_donante`) REFERENCES `usuarios` (`usuario`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidos_donacion`
--

LOCK TABLES `pedidos_donacion` WRITE;
/*!40000 ALTER TABLE `pedidos_donacion` DISABLE KEYS */;
INSERT INTO `pedidos_donacion` VALUES (13,'2025-11-16 00:00:00','AUTO','lperise','COMPLETADO',12),(14,'2025-11-16 00:00:00','CAMIONETA','rargel','PENDIENTE',11),(15,'2025-11-16 00:00:00','AUTO','lperise','PENDIENTE',11),(16,'2025-11-18 00:00:00','AUTO','rargel','PENDIENTE',NULL),(17,'2025-11-18 00:00:00','AUTO','lperise','PENDIENTE',NULL),(18,'2025-11-18 00:00:00','CAMION','lperise','PENDIENTE',NULL),(19,'2025-11-18 00:00:00','AUTO','rargel','PENDIENTE',NULL),(20,'2025-11-18 00:00:00','AUTO','lperise','COMPLETADO',13);
/*!40000 ALTER TABLE `pedidos_donacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `codigo` tinyint(1) NOT NULL,
  `nombre` varchar(45) DEFAULT NULL,
  `activo` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN',1),(2,'VOLUNTARIO',1),(3,'DONANTE',1),(4,'BENEFICIARIO',1);
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `usuario` varchar(45) NOT NULL,
  `rol` tinyint(1) NOT NULL,
  `contrasena` varchar(45) NOT NULL,
  `nombre` varchar(45) NOT NULL,
  `apellido` varchar(45) NOT NULL,
  `correo` varchar(45) NOT NULL,
  `activo` tinyint(1) NOT NULL,
  `dni` varchar(15) NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `necesidad` varchar(255) DEFAULT NULL,
  `personas_cargo` int DEFAULT '0',
  `prioridad` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`usuario`),
  UNIQUE KEY `dni_UNIQUE` (`dni`),
  KEY `fk_usuarios_1_idx` (`rol`),
  CONSTRAINT `fk_usuarios_1` FOREIGN KEY (`rol`) REFERENCES `roles` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES ('admin',1,'1234','Admin','Sistema','admin@unrn.edu.ar',1,'11111111',NULL,NULL,0,NULL),('bgoro',2,'pass','Bruno','Goro','bgoro@unrn.edu.ar',1,'22222222',NULL,NULL,0,NULL),('ihoncharuk',4,'1234','Ian','Honcharuk','ianh@email.com',1,'44455566','Av. Siempre Viva 742','Alimentos no perecederos y leche',4,'ALTA'),('ldifabio',2,'pass','Lucas','Difabio','ldifabio@unrn.edu.ar',1,'44444444',NULL,NULL,0,NULL),('lperise',3,'pass','Lautaro','Perise','lperise@unrn.edu.ar',1,'66666666','Avenida Siempre Viva 742',NULL,0,NULL),('mcamba',2,'pass','Mauro','Camba','mcamba@unrn.edu.ar',0,'33333333',NULL,NULL,0,NULL),('nbravo',4,'1234','Naim','Bravo','nbravo@email.com',1,'11122233','Calle Falsa 123','Ropa de invierno y calzado talle 40',2,'MEDIA'),('rargel',3,'pass','Ramiro','Argel','rargel@unrn.edu.ar',1,'55555555','Calle Falsa 123',NULL,0,NULL);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehiculos`
--

DROP TABLE IF EXISTS `vehiculos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehiculos` (
  `patente` varchar(10) NOT NULL,
  `tipoVeh` varchar(45) NOT NULL,
  `capacidad` int NOT NULL,
  `estado` varchar(45) NOT NULL,
  PRIMARY KEY (`patente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehiculos`
--

LOCK TABLES `vehiculos` WRITE;
/*!40000 ALTER TABLE `vehiculos` DISABLE KEYS */;
INSERT INTO `vehiculos` VALUES ('AA 789 GH','Camion',4000,'Disponible'),('AD 456 EF','Camioneta',1500,'Disponible'),('AE 123 CD','Auto',500,'Disponible');
/*!40000 ALTER TABLE `vehiculos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visitas`
--

DROP TABLE IF EXISTS `visitas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visitas` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_orden_retiro` int NOT NULL,
  `id_pedido_donacion` int NOT NULL,
  `fecha_visita` datetime NOT NULL,
  `resultado` varchar(45) NOT NULL,
  `observacion` text,
  PRIMARY KEY (`id`),
  KEY `fk_visita_orden_idx` (`id_orden_retiro`),
  KEY `fk_visita_pedido_idx` (`id_pedido_donacion`),
  CONSTRAINT `fk_visita_orden` FOREIGN KEY (`id_orden_retiro`) REFERENCES `ordenes_retiro` (`id`),
  CONSTRAINT `fk_visita_pedido` FOREIGN KEY (`id_pedido_donacion`) REFERENCES `pedidos_donacion` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visitas`
--

LOCK TABLES `visitas` WRITE;
/*!40000 ALTER TABLE `visitas` DISABLE KEYS */;
INSERT INTO `visitas` VALUES (19,12,13,'2025-11-16 20:46:56','Recoleccion Exitosa','Retiro exitoso '),(20,13,20,'2025-11-18 19:13:14','Recoleccion Exitosa','Correccion logica: Retiro exitoso simulado');
/*!40000 ALTER TABLE `visitas` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-09 18:06:12
