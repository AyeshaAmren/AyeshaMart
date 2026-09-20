# Ayesha Mart - cloud deployment (Render / any Docker host)
# Builds the WAR with Maven, then runs it on Apache Tomcat 11 (Jakarta EE 10).

# ---- Build stage -----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- Run stage -------------------------------------------------------------
FROM tomcat:11.0-jdk17-temurin
ENV AYESHA_MART_DATA_DIR=/opt/ayesha-mart-data
RUN mkdir -p /opt/ayesha-mart-data \
    && rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /workspace/target/ayesha-mart.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]