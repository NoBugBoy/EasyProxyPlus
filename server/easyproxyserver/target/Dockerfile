FROM openjdk:8
RUN ls
ENV WEB 18800
ENV PORT 9675
ADD easyproxyserver-1.0.jar app.jar
ENTRYPOINT [ "sh", "-c", "java -jar /app.jar -web $WEB -port $PORT" ]
