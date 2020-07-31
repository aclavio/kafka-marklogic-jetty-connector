# Alpine Linux with OpenJDK JRE
FROM openjdk:8

# extract the kafka directory
ADD kafka_2.12-2.2.1.tgz /

# set the working directory
ENV KAFKA_DIR=/kafka_2.12-2.2.1

# copy local resources into the directory
COPY start-connector.sh ${KAFKA_DIR}/start-connector.sh
COPY kafka-marklogic-jetty-connector.jar ${KAFKA_DIR}/libs/kafka-marklogic-jetty-connector.jar
COPY marklogic-connect-standalone.properties ${KAFKA_DIR}/config/marklogic-connect-standalone.properties
COPY marklogic-jetty-source.properties ${KAFKA_DIR}/config/marklogic-jetty-source.properties

RUN chmod ag+x ${KAFKA_DIR}/start-connector.sh

# port mappings
EXPOSE 9090

WORKDIR ${KAFKA_DIR}
# runs application
CMD ["./start-connector.sh"]

