#! /bin/tcsh -f
# automate creating the database for a new study space 

echo -n "Input the database server name used for the new study space [or q to quit]: "
set server_name = $<
if($server_name == "q") then
  exit 1
endif

echo -n "Input the database name used for the new study space [or q to quit]: "
set dbase_name = $<
if($dbase_name == "q") then
  exit 1
endif
echo -n "Input the user name for the database ($dbase_name) [or q]: "
set user_db = $<
if($user_db == "q") then
  exit 1
endif
echo -n "Input the user ($user_db)'s password [or q]: "
set passwd_db = $<
if($passwd_db == "q") then
  exit 1
endif
echo -n "Input the root ($user_db)'s password [or q]: "
set root_pwd_db = $<
if($root_pwd_db == "q") then
  exit 1
endif

echo "A new database [$dbase_name] will be created on [$server_name] mysql server"
echo "... with login as user [$user_db], password [$passwd_db]"
echo "... with database tables read from the file studydb_template.sql in this directory"
echo -n "Start creating new database? [y/n] " 
set direct_run = $<
if($direct_run != "y") then
  exit 1
endif

#set the mysql command path
#set path = ($path /usr/local/mysql/bin)
mysql -h $server_name -u root -p$root_pwd_db << EOF
drop user '$user_db'@'$server_name';
drop database if exists $dbase_name;
create database $dbase_name;
create user '$user_db'@'$server_name' identified by '$passwd_db';
grant all on *.* to '$user_db'@'$server_name';
#grant all privileges on $dbase_name.* to $user_db@sage.arc2.ucla.edu identified by '$passwd_db' with grant option;
#grant all privileges on $dbase_name.* to $user_db@sagedb.arc2.ucla.edu identified by '$passwd_db' with grant option;
#grant all privileges on $dbase_name.* to $user_db@cme.arc2.ucla.edu identified by '$passwd_db' with grant option;
#grant all privileges on $dbase_name.* to $user_db@lofts.arc2.ucla.edu identified by '$passwd_db' with grant option;
#grant all privileges on $dbase_name.* to $user_db@auth.arc2.ucla.edu identified by '$passwd_db' with grant option;
#set password for $user_db@$server_name = old_password('$passwd_db');
#set password for $user_db@sage.arc2.ucla.edu = old_password('$passwd_db');
#set password for $user_db@sagedb.arc2.ucla.edu = old_password('$passwd_db');
#set password for $user_db@cme.arc2.ucla.edu = old_password('$passwd_db');
#set password for $user_db@lofts.arc2.ucla.edu= old_password('$passwd_db');
#set password for $user_db@auth.arc2.ucla.edu = old_password('$passwd_db');
flush privileges;
#exit
#EOF

#mysqladmin -h $server_name -u root -p$root_pwd_db reload
mysql -h $server_name -u $user_db -p$passwd_db -D $dbase_name < studydb_template.sql

echo "Finished creating new database"
