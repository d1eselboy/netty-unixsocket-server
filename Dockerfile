FROM anapsix/alpine-java
MAINTAINER d1eselboy
COPY build/libs/netty-unixsocket-server-1.0-SNAPSHOT.jar /home/netty-unixsocket-server-1.0-SNAPSHOT.jar
CMD ["java","-jar","/home/netty-unixsocket-server-1.0-SNAPSHOT.jar"]
VOLUME /tmp


