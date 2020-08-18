FROM confluentinc/cp-kafka-connect-base

COPY kafka-marklogic-jetty-connector.jar /etc/kafka-connect/jars/kafka-marklogic-jetty-connector.jar
COPY start-connector.sh /start-connector.sh

RUN chmod -R a+x /start-connector.sh

EXPOSE 28082
EXPOSE 9090

ENTRYPOINT ./start-connector.sh
