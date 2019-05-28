#******************************************************************************
#
# Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
# All rights reserved.
#
#******************************************************************************

#!/bin/bash

#Check these params value first :
P2PSERVER_PATH="/4EUtils/p2pserver"
STOP_COMMAND="stopPeer.sh"
START_COMMAND="startPeer.sh"
ERROR_LOG_PATH=$P2PSERVER_PATH"/logs"
P2P_PARENT_FOLDER="/4EUtils/"
P2P_PUBLIC_FOLDER="/mnt/public/Infrastructure/Linux_Unix/Current Version/R and Peer Software/Ubuntu_Peer/p2pserver/"
GIT_FOLDER_PATH="/git"
BACKUP_PARENT_PATH="/temp/scheduler_deployment_backup"
GITHUB_REPO_URL="https://4ecapkronos:2016gitcoDE@github.com/FourElementsCapital/Scheduler.git"

echo "*** SCHEDULER PEER DEPLOYMENT START ***"

set -e

if [ $# -lt 1 ]
  then
    echo "Error argument. Sample : ./deployPeer.sh 2.1.0"
    exit 1
fi

v_out=$1

if [ $# -gt 1 ]
  then
    key="$2"
    case $key in
      -f|--fresh)
        is_fresh="true"
        shift # past argument
        ;;
      *)
        echo "Error argument. use -f or --fresh"
		exit 1
      ;;
    esac
fi

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

if [ "$is_fresh" == "true" ]
then
  echo "*. Copy p2pfolder from public drive "
  mkdir -p $P2P_PARENT_FOLDER
  cp -R "$P2P_PUBLIC_FOLDER" $P2P_PARENT_FOLDER
fi

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

if [ -z $is_fresh ]
then
  echo "*. Create backup folder"
  if [ -d $BACKUP_PATH ]
  then
    echo "ERROR : backup folder '"$BACKUP_PATH"' is already exist ! Please rename or remove it first. Note that it contains the BACKUP COPY of previous deployment."
    exit 1
  else
    mkdir -p $BACKUP_PATH
  fi
fi

if [ -z $is_fresh ]
then
  echo "*. Backup current scheduler app"
  cp -R $TOMCAT_WEB_PATH"/bldb" $BACKUP_PATH
fi

if [ -z $is_fresh ]
then
  echo "*. Backup & clear tomcat log"
  mkdir $BACKUP_PATH/log_tomcat
  if [ "$(ls -A $TOMCAT_LOG_PATH)" ]
  then
    mv $TOMCAT_LOG_PATH/* $BACKUP_PATH/log_tomcat
  fi
fi

if [ -z $is_fresh ]
then
  echo "*. Backup & clear error log"
  mkdir $BACKUP_PATH/log_error
  if [ "$(ls -A $ERROR_LOG_PATH)" ]
  then
    mv $ERROR_LOG_PATH/* $BACKUP_PATH/log_error
  fi
fi

echo "*. Deploy new scheduler app"
if [ -z $is_fresh ]
then
  rm -R $TOMCAT_WEB_PATH"/bldb"
fi
cd $SCHEDULER_RELEASE_PATH
unzip -qq bldb.zip -d $TOMCAT_WEB_PATH

echo "*. Copy properties file"
cp -R $SCHEDULER_RELEASE_PATH"/properties/peer_unix/bldb" $TOMCAT_WEB_PATH

echo "*. Set access permission"
chmod -R 777 $TOMCAT_WEB_PATH"/bldb"

echo "*. Start Tomcat server"
cd $P2PSERVER_PATH
sh ./$START_COMMAND

echo "*** DEPLOYMENT SUCCESS ! ***"


