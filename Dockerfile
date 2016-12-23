FROM anapsix/alpine-java
MAINTAINER ermolaev.v.a
COPY build/libs/unixsocket-logwriter-1.0-SNAPSHOT.jar /home/unixsocket-logwriter-1.0-SNAPSHOT.jar
CMD ["java","-jar","/home/unixsocket-logwriter-1.0-SNAPSHOT.jar"]


