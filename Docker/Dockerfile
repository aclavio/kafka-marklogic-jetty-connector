FROM confluentinc/cp-kafka-connect-base

COPY kafka-marklogic-jetty-connector.jar /etc/kafka-connect/jars/kafka-marklogic-jetty-connector.jar
COPY start-connector.sh /start-connector.sh

ENTRYPOINT ./start-connector.sh
