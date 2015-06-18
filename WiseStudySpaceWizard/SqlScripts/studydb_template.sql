-- WISE database schema template

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `invitee`
-- Description: holds the personal information of the invitees
--
DROP TABLE IF EXISTS `invitee`;
CREATE TABLE `invitee` (
  `id` int(6) NOT NULL auto_increment,
  `firstname` varchar(64) default NULL,
  `lastname` varchar(64) default NULL,
  `salutation` varchar(5) default NULL,
  `email` varchar(64) default NULL,
  `phone` varchar(12) default NULL,
  `irb_id` varchar(11) default NULL,
  `subj_type` char(1) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `survey_user_state`
-- Description: records the state and message sequence that a user is on.
DROP TABLE IF EXISTS `survey_user_state`;
CREATE TABLE `survey_user_state` (
  `invitee` int(6) NOT NULL,
  `survey` varchar(64) NOT NULL,
  `message_sequence` varchar(64) default NULL,
  `state` varchar(64) default NULL,
  `state_count` int(3) NOT NULL default '1',
  `entry_time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`invitee`, `survey`),
  KEY `invitee` (`invitee`),
  CONSTRAINT `survey_user_state_ibfk_1` FOREIGN KEY (`invitee`) REFERENCES `invitee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `pending`
-- Description: stores messages to be send to invitees at a later date
DROP TABLE IF EXISTS `pending`;
CREATE TABLE `pending` (
  `invitee` int(8) NOT NULL,
  `send_time` datetime default NULL,
  `message_sequence` varchar(32) default NULL,
  `message` varchar(32) default NULL,
  `survey` varchar(32) default NULL,
  `completed` char(1) default NULL,
  `completed_time` datetime default NULL,
  KEY `invitee` (`invitee`),
  CONSTRAINT `pending_ibfk_1` FOREIGN KEY (`invitee`) REFERENCES `invitee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `survey_message_use`
-- Description: records the messageId, and the type of message sent to the invitee. 
--              messageId links back to the survey and invitee.  
--
DROP TABLE IF EXISTS `survey_message_use`;
CREATE TABLE `survey_message_use` (
  `id` int(6) NOT NULL auto_increment,
  `invitee` int(6) default NULL,
  `survey` varchar(64) default NULL,
  `messageId` varchar(64) default NULL,
  `sent_date` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `message` varchar(64) default NULL,
  PRIMARY KEY  (`id`),
  KEY `invitee` (`invitee`),
  CONSTRAINT `survey_message_use_ibfk_1` FOREIGN KEY (`invitee`) REFERENCES `invitee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `welcome_hits`
--
DROP TABLE IF EXISTS `welcome_hits`;
CREATE TABLE `welcome_hits` (
  `invitee` int(6) default NULL,
  `viewdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `survey` varchar(64) NOT NULL default '',
  KEY `invitee` (`invitee`),
  CONSTRAINT `welcome_hits_ibfk_1` FOREIGN KEY (`invitee`) REFERENCES `invitee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `decline_hits`
-- Description: records a decline event when the invitee declines to take the survey.
--              The msg_id can be used to trace which message was declined.
--
DROP TABLE IF EXISTS `decline_hits`;
CREATE TABLE `decline_hits` (
  `msg_id` int(6) default NULL,
  `survey` varchar(64) default NULL,
  `viewdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  KEY `msg_id` (`msg_id`),
  CONSTRAINT `decline_hits_ibfk_1` FOREIGN KEY (`msg_id`) REFERENCES `survey_message_use` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `decline_reason`
-- Description: records the reason for declining provided by the invitee
--
DROP TABLE IF EXISTS `decline_reason`;
CREATE TABLE `decline_reason` (
  `invitee` int(6) default NULL,
  `reason` text,
  `declinedate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  KEY `invitee` (`invitee`),
  CONSTRAINT `decline_reason_ibfk_1` FOREIGN KEY (`invitee`) REFERENCES `invitee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `consent_response`
-- Description: records invitee consent before the survey is administered
--
DROP TABLE IF EXISTS `consent_response`;
CREATE TABLE `consent_response` (
  `invitee` int(6) default NULL,
  `answer` char(1) default NULL,
  `viewdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `survey` varchar(64) NOT NULL default '',
  KEY `invitee` (`invitee`),
  CONSTRAINT `consent_response_ibfk_1` FOREIGN KEY (`invitee`) REFERENCES `invitee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `survey_user_session`
-- Description: records data related to the survey session of a particular invitee
--
DROP TABLE IF EXISTS `survey_user_session`;
CREATE TABLE `survey_user_session` (
  `id` int(6) NOT NULL auto_increment,
  `endtime` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `starttime` timestamp NOT NULL default '0000-00-00 00:00:00',
  `from_message` int(6) NOT NULL default '0',
  `browser_info` text NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `from_message` (`from_message`),
  CONSTRAINT `survey_user_session_ibfk_1` FOREIGN KEY (`from_message`) REFERENCES `survey_message_use` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `page_view`
-- Description: records a page view event when an invitee views a page
--
DROP TABLE IF EXISTS `page_view`;
CREATE TABLE `page_view` (
  `invitee` int(8) NOT NULL default '0',
  `page` varchar(64) default NULL,
  `time` varchar(8) default NULL,
  `idle` varchar(8) default NULL,
  `hittime` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  KEY `invitee` (`invitee`),
  CONSTRAINT `page_view_ibfk_1` FOREIGN KEY (`invitee`) REFERENCES `invitee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `survey_user_page`
--
DROP TABLE IF EXISTS `survey_user_page`;
CREATE TABLE `survey_user_page` (
  `invitee` varchar(45) NOT NULL,
  `status` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`invitee`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `page_submit`
-- Description: records a page submit event when an invitee submits a page
--
DROP TABLE IF EXISTS `page_submit`;
CREATE TABLE `page_submit` (
  `invitee` int(6) default NULL,
  `survey` varchar(64) default NULL,
  `page` varchar(64) default NULL,
  `Created` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  KEY `invitee` (`invitee`),
  CONSTRAINT `page_submit_ibfk_1` FOREIGN KEY (`invitee`) REFERENCES `invitee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `update_trail`
--
DROP TABLE IF EXISTS `update_trail`;
CREATE TABLE `update_trail` (
  `invitee` int(6) default NULL,
  `survey` varchar(64) default NULL,
  `page` varchar(64) default NULL,
  `ColumnName` varchar(32) default NULL,
  `OldValue` varchar(255) default NULL,
  `Modified` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `CurrentValue` varchar(255) default NULL,
  KEY `invitee` (`invitee`),
  CONSTRAINT `update_trail_ibfk_1` FOREIGN KEY (`invitee`) REFERENCES `invitee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `interviewer`
-- Description: An interviewer is assigned to manage multiple invitees.
--
DROP TABLE IF EXISTS `interviewer`;
CREATE TABLE `interviewer` (
  `id` int(6) NOT NULL auto_increment,
  `username` varchar(64) default NULL,
  `firstname` varchar(64) default NULL,
  `lastname` varchar(64) default NULL,
  `salutation` varchar(5) default NULL,
  `email` varchar(64) default NULL,
  `submittime` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `interview_assignment`
-- Description: records the assignments of invitees to interviewers
--
DROP TABLE IF EXISTS `interview_assignment`;
CREATE TABLE `interview_assignment` (
  `id` int(6) NOT NULL auto_increment,
  `interviewer` int(6) default NULL,
  `invitee` int(6) default NULL,
  `survey` varchar(64) default NULL,
  `close_date` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `assign_date` timestamp NOT NULL default '0000-00-00 00:00:00',
  `pending` int(1) default '1',
  PRIMARY KEY  (`id`),
  KEY `interviewer` (`interviewer`,`invitee`,`survey`),
  CONSTRAINT `interview_assignment_ibfk_1` FOREIGN KEY (`interviewer`) REFERENCES `interviewer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `interview_session`
-- Description: records data related to the survey session of conducted by an interviewer
--
DROP TABLE IF EXISTS `interview_session`;
CREATE TABLE `interview_session` (
  `session_id` int(6) NOT NULL default '0',
  `assign_id` int(6) default NULL,
  PRIMARY KEY  (`session_id`),
  KEY `assign_id` (`assign_id`),
  CONSTRAINT `interview_session_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `survey_user_session` (`id`) ON DELETE CASCADE,
  CONSTRAINT `interview_session_ibfk_2` FOREIGN KEY (`assign_id`) REFERENCES `interview_assignment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `surveys`
--
DROP TABLE IF EXISTS `surveys`;
CREATE TABLE `surveys` (
  `internal_id` int(6) NOT NULL auto_increment,
  `id` varchar(64) default NULL,
  `filename` varchar(64) default NULL,
  `title` varchar(255) default NULL,
  `uploaded` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `status` char(1) default NULL,
  `archive_date` varchar(64) default NULL,
  `create_syntax` text,
  PRIMARY KEY  (`internal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `survey_health`
--
DROP TABLE IF EXISTS `survey_health`;
CREATE TABLE `survey_health` ( 
	`survey_name` varchar(32) NOT NULL, 
	`last_update_time` bigint(64) NOT NULL, 
	PRIMARY KEY (`survey_name`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;



--
-- Table structure for table `wisefiles`
--
DROP TABLE IF EXISTS `wisefiles`;
CREATE TABLE `wisefiles` (
  `file_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `filecontents` longblob NOT NULL,
  `upload_date` datetime NOT NULL,
  PRIMARY KEY (`file_id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=latin1;

--
-- Table structure for table `xmlfiles`
--
DROP TABLE IF EXISTS `xmlfiles`;
CREATE TABLE `xmlfiles` (
  `file_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `filecontents` longblob NOT NULL,
  `upload_date` datetime NOT NULL,
  PRIMARY KEY (`file_id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=latin1;


--
-- Table structure for table `data_integer`
--
DROP TABLE IF EXISTS `data_integer`;
CREATE TABLE `data_integer` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `survey` varchar(45) NOT NULL,
  `inviteeId` int(10) NOT NULL,
  `questionId` varchar(45) NOT NULL,
  `answer` int(11) NOT NULL,
  `level` int(2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  CONSTRAINT `data_integer_fk_1` FOREIGN KEY (`inviteeId`) REFERENCES `invitee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=latin1;

--
-- Table structure for table `data_text`
--
DROP TABLE IF EXISTS `data_text`;
CREATE TABLE `data_text` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `survey` varchar(45) NOT NULL,
  `inviteeId` int(10) NOT NULL,
  `questionId` varchar(100) NOT NULL,
  `answer` text NOT NULL,
  `level` int(2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  CONSTRAINT `data_text_fk_1` FOREIGN KEY (`inviteeId`) REFERENCES `invitee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `data_repeat_set_instance`
--
DROP TABLE IF EXISTS `data_repeat_set_instance`;
CREATE TABLE `data_repeat_set_instance` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `repeat_set_name` varchar(100) NOT NULL,
  `instance_pseudo_id` varchar(100) NOT NULL,
  `inviteeId` int(10) NOT NULL,
  `survey` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `data_rpt_ins_id_to_ques_id`
--
DROP TABLE IF EXISTS `data_rpt_ins_id_to_ques_id`;
CREATE TABLE `data_rpt_ins_id_to_ques_id` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `rpt_ins_id` varchar(100) NOT NULL,
  `ques_id` varchar(100) NOT NULL,
  `type` char(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;