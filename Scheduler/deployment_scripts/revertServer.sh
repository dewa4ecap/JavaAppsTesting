#******************************************************************************
#
# Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
# All rights reserved.
#
#****************************************************************************** 

#!/bin/bash
echo "*** SCHEDULER REVERT START ***"

set -e

if [ $# -lt 1 ]
  then
    echo "Error argument. Sample : ./revertServer.sh 2.1.0"
    exit 1
fi

v_out=$1

P2PSERVER_PATH="/home/p2pserver"
START_COMMAND="start.sh"
STOP_COMMAND="stop.sh"
BACKUP_PATH="/temp/scheduler_deployment_backup/"$v_out

echo "*. Check backup path "$BACKUP_PATH
cd $BACKUP_PATH

echo "*. Check Tomcat version and directory"
TOMCAT_7="/tomcat7"
TOMCAT_6="/tomcat6"
VAR_LIB_PATH="/var/lib"
VAR_LOG_PATH="/var/log"
TOMCAT_PATH=$VAR_LIB_PATH$TOMCAT_7
TOMCAT_WEB_PATH=$TOMCAT_PATH"/webapps"
if [ ! -d $TOMCAT_PATH ]; then
  if [ -d $VAR_LIB_PATH$TOMCAT_6 ]; then
    TOMCAT_PATH=$VAR_LIB_PATH$TOMCAT_6
    TOMCAT_WEB_PATH=$TOMCAT_PATH"/webapps"
  else
    echo "Error : Neither tomcat 6 or 7 is installed !!"
    exit 1
  fi
fi
echo "Tomcat path : "$TOMCAT_PATH

echo "*. Stop Tomcat server"
cd $P2PSERVER_PATH
sh ./$STOP_COMMAND

echo "*. Remove Scheduler app"
rm -R $TOMCAT_WEB_PATH"/bldb"

echo "*. Copy files from backup"
cp -R $BACKUP_PATH"/bldb" $TOMCAT_WEB_PATH

echo "*. Set access permission"
chmod -R 777 $TOMCAT_WEB_PATH"/bldb"


echo "*** REVERT SUCCESS ! ***"
echo "*** Please start Scheduler Server manually ! ***"



