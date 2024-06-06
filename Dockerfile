FROM maven:3.5 AS build

WORKDIR /app

COPY . .
RUN mvn clean install


FROM tomcat:7
COPY --from=build /app/target/scimproxycompliance.war /usr/local/tomcat/webapps/ROOT.war


# USAGE:
# docker build -t scim-test .
# docker run --rm -p "80:8080" scim-test
