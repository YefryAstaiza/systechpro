# Build multi-etapa: compila el WAR con Maven y lo sirve con Tomcat, sin depender
# de tener target/systechpro.war ya generado localmente (Railway construye desde cero).

# ---- Etapa 1: compilar el WAR ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Etapa 2: runtime sobre Tomcat ----
FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/systechpro.war /usr/local/tomcat/webapps/systechpro.war
# La app vive en el context path /systechpro/ (no ROOT.war, porque core.js's apiBase
# hardcodea esa ruta) - esta pagina redirige quien entra al dominio pelado hacia ahi.
RUN mkdir -p /usr/local/tomcat/webapps/ROOT
COPY root-redirect.html /usr/local/tomcat/webapps/ROOT/index.html
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/docker-entrypoint.sh"]
