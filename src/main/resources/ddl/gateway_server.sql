CREATE TABLE `gateway_server` (
  `server_name` varchar(100) NOT NULL,
  `uri` varchar(200) NOT NULL,
  `metadata` varchar(500) DEFAULT NULL,
  `desc` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`server_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci