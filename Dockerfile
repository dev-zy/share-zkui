FROM openjdk:8-jdk-alpine

MAINTAINER Kervin521 <zy5211314521@outlook.com>

RUN \
    mkdir -p /opt/deploy

COPY target/zkui-2.1.0.tar.gz /opt/deploy

RUN \
    tar -xzvf /opt/deploy/zkui-*.tar.gz -C /opt/deploy && \
    /bin/rm -rf /opt/deploy/zkui-*.tar.gz && \
    mkdir -p /opt/deploy/zkui/logs  /opt/deploy/zkui/data && \
    chmod +x /opt/deploy/zkui/bin/*.sh  && \
    true

WORKDIR /opt/deploy/zkui
ENTRYPOINT ["java", "-jar", "zkui-2.1.0.jar" ]
