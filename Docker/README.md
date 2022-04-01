# Docker Commands

## Run Container

    docker run -it \
    --name=kafka-marklogic-jetty-connector \
    -p 28082:28082 \
    -p 9090:9090 \
    -e CONNECT_BOOTSTRAP_SERVERS=localhost:9092 \
    -e CONNECT_REST_PORT=28082 \
    -e CONNECT_GROUP_ID="marklogic-source" \
    -e CONNECT_CONFIG_STORAGE_TOPIC="marklogic-source-config" \
    -e CONNECT_OFFSET_STORAGE_TOPIC="marklogic-source-offsets" \
    -e CONNECT_STATUS_STORAGE_TOPIC="marklogic-source-status" \
    -e CONNECT_KEY_CONVERTER="org.apache.kafka.connect.storage.StringConverter" \
    -e CONNECT_VALUE_CONVERTER="org.apache.kafka.connect.storage.StringConverter" \
    -e CONNECT_KEY_SCHEMAS_ENABLE=false\
    -e CONNECT_VALUE_CONVERTER_SCHEMAS=false\
    -e CONNECT_REST_ADVERTISED_HOST_NAME="localhost" \
    -e CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR=1 \
    -e CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR=1 \
    -e CONNECT_STATUS_STORAGE_REPLICATION_FACTOR=1 \
    -e CONNECT_PLUGIN_PATH=/usr/share/java \
    -e JETTY_PORT=9090 \
    -e JETTY_SECURE=false \
    kafka-marklogic-jetty-connector:latest