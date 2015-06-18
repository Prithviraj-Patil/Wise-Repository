CREATE DATABASE `study_space_parameters`;

DROP TABLE IF EXISTS `study_space_parameters`.`parameters`;

CREATE TABLE `study_space_parameters`.`parameters` (
  `study_id` int(11) NOT NULL AUTO_INCREMENT,
  `studySpaceName` varchar(45) NOT NULL,
  `server_url` TEXT NOT NULL,
  `serverApp` varchar(45) NOT NULL,
  `sharedFiles_linkName` varchar(45) NOT NULL,
  `dirName` varchar(45) NOT NULL,
  `dbuser` varchar(45) NOT NULL,
  `dbpass` varchar(45) NOT NULL,
  `dbname` varchar(45) NOT NULL,
  `proj_title` TEXT NOT NULL,
  `db_crypt_key` varchar(45) NOT NULL,
  `emailSendingTime` int(2) NOT NULL,
  PRIMARY KEY (`study_id`,`studySpaceName`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

