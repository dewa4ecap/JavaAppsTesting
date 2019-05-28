FROM ubuntu:14.04
MAINTAINER Gamaliel Surya, gama@kronosinvestments.com

#Install required packages
RUN apt-get update
RUN apt-get install -y libxml2 libxml2-dev gfortran curl libcurl3 libcurl3-dev libgfortran3 libpaper-utils cifs-utils
RUN apt-get install -y ess tcl tcl-dev tk
RUN apt-get install -y libmyodbc freetds-* unixodbc unixodbc-dev libcairo2 libcairo2-dev tdsodbc
RUN apt-get install -y pdftk netcdf-* liblzma-dev lzma lzma-dev
RUN apt-get install -y gcc libtool libreadline-dev g++ xorg-dev mesa-common-dev libcgal-dev
RUN apt-get install -y texlive latex2html imagemagick graphviz htop

#Install Java
RUN apt-get update
RUN apt-get install -y openjdk-7-jre
RUN apt-get install -y openjdk-7-jdk

#Install JCS
RUN apt-get install -y libcommons-collections3-java libcommons-dbcp-java libcommons-logging-java libcommons-parent-java libcommons-pool-java

#Install Tomcat
RUN apt-get install -y tomcat7
RUN echo "TOMCAT7_USER=root" > /etc/default/tomcat7
RUN echo "TOMCAT7_GROUP=root" >> /etc/default/tomcat7
RUN echo "JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/" >> /etc/default/tomcat7
RUN echo 'JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Xmx512m -XX:MaxPermSize=256m -XX:+UseConcMarkSweepGC"' >> /etc/default/tomcat7
RUN echo "R_HOME=/usr/lib/R" >> /etc/default/tomcat7

#Install R 
RUN apt-get purge -y r-base r-base-dev r-base-html r-base-core
RUN apt-get autoremove -y
RUN rm -Rf /usr/lib/R/*
RUN echo "deb https://cloud.r-project.org/bin/linux/ubuntu trusty/" >> /etc/apt/sources.list
RUN gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys E084DAB9
RUN gpg -a --export E084DAB9 | apt-key add -
RUN apt-get install -y apt-transport-https
RUN apt-get update
RUN apt-get install -y r-base-core=3.2.2-1trusty0
RUN apt-get install -y r-base-dev=3.2.2-1trusty0
RUN rm /usr/lib/R/etc/Rprofile.site

# Install Rserve
RUN R -e "install.packages('Rserve', lib=file.path(R.home(),'library'),repos='http://cran.r-project.org/')" --no-save

# Install rJava
RUN apt-get install -y r-cran-rjava

# Setting Environment
ENV CATALINA_BASE /var/lib/tomcat7
ENV CATALINA_HOME /usr/share/tomcat7
ENV PATH $CATALINA_HOME/bin:$PATH
ENV JRE_HOME /usr/lib/jvm/java-7-openjdk-amd64/jre
ENV PATH $JRE_HOME/bin:$PATH
ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64/
ENV PATH $JAVA_HOME:$PATH
ENV R_HOME /usr/lib/R
ENV R_SHARE_DIR /usr/lib/R/share
ENV R_DOC_HOME /usr/local/lib64/R/doc
ENV LD_LIBRARY_PATH /usr/local/lib:/usr/local/lib64:/usr/lib:/usr/lib64:/lib$
ENV JAVA_CPPFLAGS -I/usr/lib/jvm/java-7-openjdk-amd64/include
ENV JAVA_LIBS -L/usr/lib/jvm/java-7-openjdk-amd64/lib
RUN R CMD javareconf -e

#Make required directories
RUN mkdir -p /home/p2pserver/logs/ && chmod -R 775 /home/p2pserver/logs/
RUN mkdir -p /usr/share/tomcat7/common/classes && chmod -R 775 /usr/share/tomcat7/common/classes
RUN mkdir -p /usr/share/tomcat7/server/classes && chmod -R 775 /usr/share/tomcat7/server/classes
RUN mkdir -p /usr/share/tomcat7/shared/classes && chmod -R 775 /usr/share/tomcat7/shared/classes
RUN mkdir -p  /var/lib/tomcat7/temp && chmod -R 775 /var/lib/tomcat7/temp

#Open required ports 
EXPOSE 8080 1818 6311

#Copy peer.war from local machine to image
ADD peer.war $CATALINA_BASE/webapps/
RUN chmod -R 775 $CATALINA_BASE/webapps/

#Run Rserve service and Tomcat
WORKDIR $CATALINA_HOME
CMD R CMD Rserve --no-save && bin/catalina.sh run