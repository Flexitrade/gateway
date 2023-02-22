FROM eclipse-temurin:17.0.6_10-jre
ENV APP_FILE gateway-*.jar
ENV APP_HOME /app
EXPOSE 8088
COPY target/$APP_FILE $APP_HOME/gateway.jar
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar gateway.jar"]