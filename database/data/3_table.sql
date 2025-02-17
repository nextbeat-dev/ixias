USE `dummy`;

DROP TABLE IF EXISTS `aws_s3_file`;
CREATE TABLE `aws_s3_file` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `region` varchar(32) NOT NULL,
  `bucket` varchar(32) NOT NULL,
  `key` varchar(255) NOT NULL,
  `typedef` varchar(32) NOT NULL,
  `width` smallint unsigned DEFAULT NULL,
  `height` smallint unsigned DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;
