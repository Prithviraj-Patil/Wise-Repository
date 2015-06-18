CREATE USER 'demouser'@'localhost' IDENTIFIED BY 'demopass';
GRANT ALL PRIVILEGES ON demo.* TO 'demouser'@'localhost';

INSERT INTO `study_space_parameters`.`parameters`
(`studySpaceName`,
`server_url`,
`serverApp`,
`sharedFiles_linkName`,
`dirName`,
`dbuser`,
`dbpass`,
`dbname`,
`proj_title`,
`db_crypt_key`,
`emailSendingTime`)
VALUES
("demo",
"http://localhost:8080",
"WISE",
"survey",
"demo",
"demouser",
"demopass",
"demo",
"Demo WISE Environment",
"demodemodemodemo",
"0");