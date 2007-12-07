@echo off

rem MSDOS batch script to run espam

set params=
:start
if "%1" == "" goto stop
set params=%params% %1
shift
goto start
:stop
rem echo "params is: %params%"

"%JAVAHOME%\bin\java" -classpath "%ESPAM%\lib\compaan.jar;%ESPAM%\src" espam.main.Main %params%

