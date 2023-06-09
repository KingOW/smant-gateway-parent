FROM openjdk:17-jdk
MAINTAINER Smant
ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime
COPY   lib/    /app/lib
#COPY   config/bootstrap.yml /app/config/bootstrap.yml

COPY   @{project_run_jar}@  /app/@{project_run_jar}@
#CMD echo "启动....."
#CMD ["nginx","-g","daemon off;"]
#CMD java -Dloader.path=/app/lib -Dspring.cloud.bootstrap.location=/app/config/bootstrap.yml -Dspring.cloud.nacos.discovery.register-enabled=false -jar /app/erentals-gateway.jarz
#FROM nginx:stable-alpine
#CMD ["nginx","-g","daemon off;"z