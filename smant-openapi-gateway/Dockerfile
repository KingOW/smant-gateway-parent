FROM openjdk:21
MAINTAINER Smant
ENV LANG en_US.UTF-8
ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime
#COPY   lib/    /app/lib
#COPY   config/bootstrap.yml /app/config/bootstrap.yml
COPY   target/smant-openapi-gateway.jar  /app/app.jar
#CMD java -Dloader.path=/app/lib -Dspring.cloud.bootstrap.location=/app/config/bootstrap.yml -Dspring.cloud.nacos.discovery.register-enabled=false -jar /app/erentals-gateway.jarz
#FROM nginx:stable-alpine
#CMD ["nginx","-g","daemon off;"z
