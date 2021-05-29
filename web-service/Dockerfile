# Maven build container
  
FROM maven:3.6.3-openjdk-11-slim AS maven_build

WORKDIR /tmp/

COPY pom.xml /tmp/
RUN mvn dependency:go-offline

COPY src /tmp/src/

COPY src/main/resources/application.properties.template /tmp/src/main/resoureces/application.properties

RUN mvn package

#pull base image

FROM adoptopenjdk/openjdk11:jdk-11.0.2.9-alpine-slim

COPY hooks /tmp/hooks/
COPY wait-for /tmp/

#maintainer
MAINTAINER ttran@pingidentity.com
EXPOSE 9191

CMD cd /tmp/ && ./hooks/start-service.sh

COPY --from=maven_build /tmp/target/magiclink-0.0.1-SNAPSHOT.war /tmp/app.war
