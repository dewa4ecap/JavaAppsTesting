#******************************************************************************
#
# Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
# All rights reserved.
#
#****************************************************************************** 

#!/bin/bash

#Check these params value first :
P2PSERVER_PATH="/home/p2pserver"
STOP_COMMAND="stop.sh"
START_COMMAND="start.sh"
ERROR_LOG_PATH=$P2PSERVER_PATH"/logs"
SVN_PATH=$P2PSERVER_PATH"/4svntemp"
SVN_R_PATH=$P2PSERVER_PATH"/r_function_svntemp"
SVN_IEXEC_PATH=$P2PSERVER_PATH"/iexec_svn"
QUEUE_FILES=$P2PSERVER_PATH"/queue/queue1"
GIT_FOLDER_PATH="/git"
BACKUP_PARENT_PATH="/temp/scheduler_deployment_backup"
GITHUB_REPO_URL="https://4ecapkronos:2016gitcoDE@github.com/FourElementsCapital/Scheduler.git"

echo "*** SCHEDULER SERVER DEPLOYMENT START ***"

set -e

if [ $# -lt 1 ]
  then
    echo "Error argument. Sample : ./deployServer.sh 2.1.0"
    exit 1
fi

v_out=$1

SCHEDULER_REPO_PATH=$GIT_FOLDER_PATH"/Scheduler"
SCHEDULER_RELEASE_PATH=$SCHEDULER_REPO_PATH"/release/"$v_out
BACKUP_PATH=$BACKUP_PARENT_PATH"/"$v_out

#echo "*. Check Scheduler local repository whether it's exist or not"
#if [ -d $SCHEDULER_REPO_PATH ]; then
#  echo "Scheduler repo IS EXIST. PULLING from Github.."
#  cd $SCHEDULER_REPO_PATH
#  git pull $GITHUB_REPO_URL
#else
#  echo "Scheduler repo IS NOT EXIST. CLONING from Github.."
#  mkdir -p $GIT_FOLDER_PATH
#  cd $GIT_FOLDER_PATH
#  git clone $GITHUB_REPO_URL
#fi
#chmod -R 777 $GIT_FOLDER_PATH

echo "*. Check Scheduler release path "$SCHEDULER_RELEASE_PATH
cd $SCHEDULER_RELEASE_PATH

echo "*. Check Tomcat version and directory"
TOMCAT_7="/tomcat7"
TOMCAT_6="/tomcat6"
VAR_LIB_PATH="/var/lib"
VAR_LOG_PATH="/var/log"
TOMCAT_PATH=$VAR_LIB_PATH$TOMCAT_7
TOMCAT_WEB_PATH=$TOMCAT_PATH"/webapps"
TOMCAT_LOG_PATH=$VAR_LOG_PATH$TOMCAT_7
if [ ! -d $TOMCAT_PATH ]; then
  if [ -d $VAR_LIB_PATH$TOMCAT_6 ]; then
    TOMCAT_PATH=$VAR_LIB_PATH$TOMCAT_6
    TOMCAT_WEB_PATH=$TOMCAT_PATH"/webapps"
    TOMCAT_LOG_PATH=$VAR_LOG_PATH$TOMCAT_6
  else
    echo "Error : Neither tomcat 6 or 7 is installed !!"
    exit 1
  fi
fi
echo "Tomcat path : "$TOMCAT_PATH

echo "*. Stop Tomcat server"
cd $P2PSERVER_PATH
sh ./$STOP_COMMAND

echo "*. Create backup folder"
if [ -d $BACKUP_PATH ]
then
  echo "ERROR : backup folder '"$BACKUP_PATH"' is already exist ! Please rename or remove it first. Note that it contains the BACKUP COPY of previous deployment."
  exit 1
else
  mkdir -p $BACKUP_PATH
fi

echo "*. Backup current scheduler app"
cp -R $TOMCAT_WEB_PATH"/bldb" $BACKUP_PATH

echo "*. Backup SVN files"
cp -R $SVN_PATH $BACKUP_PATH/svn
cp -R $SVN_R_PATH $BACKUP_PATH/svn_r
cp -R $SVN_IEXEC_PATH $BACKUP_PATH/svn_iexec

echo "*. Backup & clear tomcat log"
mkdir $BACKUP_PATH/log_tomcat
if [ "$(ls -A $TOMCAT_LOG_PATH)" ]
then
  mv $TOMCAT_LOG_PATH/* $BACKUP_PATH/log_tomcat
fi

echo "*. Backup & clear error log"
mkdir $BACKUP_PATH/log_error
if [ "$(ls -A $ERROR_LOG_PATH)" ]
then
  mv $ERROR_LOG_PATH/* $BACKUP_PATH/log_error
fi

echo "*. Retain superuser.pwd"
cp $BACKUP_PATH/log_error/superuser.pwd $ERROR_LOG_PATH
chmod 777 $ERROR_LOG_PATH"/superuser.pwd"

echo "*. Backup & clear queue files"
mkdir $BACKUP_PATH/queue
if [ "$(ls $QUEUE_FILES.* 2> /dev/null)" ]
then
  mv $QUEUE_FILES.* $BACKUP_PATH/queue
fi

echo "*. Deploy new scheduler app"
rm -R $TOMCAT_WEB_PATH"/bldb"
cd $SCHEDULER_RELEASE_PATH
unzip -qq bldb.zip -d $TOMCAT_WEB_PATH

echo "*. Copy properties file"
cp -R $SCHEDULER_RELEASE_PATH"/properties/server_unix/bldb" $TOMCAT_WEB_PATH

echo "*. Set access permission"
chmod -R 777 $TOMCAT_WEB_PATH"/bldb"

echo "*** DEPLOYMENT SUCCESS ! ***"
echo "*** Please start Scheduler Server manually ! ***"



