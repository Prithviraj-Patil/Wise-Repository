@echo off
rem -------------------------------------------------------------------------
rem Script to run working version of WISE
rem -------------------------------------------------------------------------

rem <-- START OF CONFIGURATION -->

rem WISE_HOME should be pointed at the directory containing the properties file for WISE
set WISE_HOME="C:\WiseInstance\Properties"
set WISE_SOURCE_HOME=C:\SecureWise
set POST_DEPLOY_URL=local.dev:8080

rem <-- END OF CONFIGURATION -->

set WISE_SSW_HOME=%WISE_HOME%
cd %WISE_SOURCE_HOME%

call ant -buildfile build.xml deploy_to_local

timeout /t 10
call start chrome "%POST_DEPLOY_URL%/WISE/admin" "%POST_DEPLOY_URL%/WiseStudySpaceWizard"