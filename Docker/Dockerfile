FROM confluentinc/cp-kafka-connect-base

COPY kafka-marklogic-jetty-connector.jar /etc/kafka-connect/jars/kafka-marklogic-jetty-connector.jar
COPY start-connector.sh /start-connector.sh
COPY config-template.json /config-template.json
COPY cacerts /cacerts

ENV CONNECT_REST_PORT=28082
ENV CONNECTOR_NAME=marklogic-jetty-connector
ENV JETTY_PORT=9090
ENV JETTY_SECURE=false
ENV JETTY_SECURE_PORT=443
ENV JETTY_SSL_CLIENT_AUTH=true
ENV JETTY_SSL_KEYSTORE_PATH=cacerts
ENV JETTY_SSL_KEYSTORE_PASSWORD=changeit
ENV JETTY_SSL_KEYSTORE_MANAGER_PASSWORD=changeit
ENV JETTY_SSL_TRUSTSTORE_PATH=cacerts
ENV JETTY_SSL_TRUSTSTORE_PASSWORD=changeit

RUN chmod -R a+x /start-connector.sh

EXPOSE 28082
EXPOSE 9090
EXPOSE 443

ENTRYPOINT ./start-connector.sh
