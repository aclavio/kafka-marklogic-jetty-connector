#!/bin/sh

# provide environment variable defaults or error if required
if [ -z "$CONNECT_REST_PORT" ]; then
  CONNECT_REST_PORT="28082"
fi

if [ -z "$CONNECTOR_NAME" ]; then
  CONNECTOR_NAME="marklogic-jetty-connector"
fi

if [ -z "$JETTY_PORT" ]; then
  JETTY_PORT="9090"
fi

if [ -z "$JETTY_SECURE" ]; then
  JETTY_SECURE="false"
fi

if [ -z "$JETTY_SSL_CLIENT_AUTH" ]; then
  JETTY_SSL_CLIENT_AUTH="true"
fi

# create payload for initializing connector using environment variables
JSON_TEMPLATE='{ "connector.class": "com.marklogic.kafka.connect.source.jetty.MarkLogicSourceConnector", "tasks.max": "1", "jetty.port": "%s", "jetty.secure": "%s", "jetty.ssl.client.auth": "%s", "jetty.ssl.keystore.path": "", "jetty.ssl.keystore.password": "", "jetty.ssl.keystore.manager.password": "", "jetty.ssl.truststore.path": "", "jetty.ssl.truststore.password": ""}'
JSON_STRING=$( printf "$JSON_TEMPLATE" "$JETTY_PORT" "$JETTY_SECURE" "$JETTY_SSL_CLIENT_AUTH" )

echo "$JSON_STRING" > config.json

# start the confluent connect piece
CONNECT_RUN_COMMAND="/etc/confluent/docker/run"
echo "starting connect..."
nohup $CONNECT_RUN_COMMAND > connect.log 2>&1 &

# wait
echo "waiting for connect startup..."
sleep 60s

# issue curl command to configure connector
echo "configuring connector..."
curl -v --request PUT "http://localhost:$CONNECT_REST_PORT/connectors/$CONNECTOR_NAME/config" --header 'Content-Type: application/json' --data "$JSON_STRING" > config.log 2>&1

# tail connect log
tail -n 1000 -f connect.log