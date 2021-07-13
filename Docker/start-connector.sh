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

if [ -z "$JETTY_SECURE_PORT" ]; then
  JETTY_SECURE_PORT="443"
fi

if [ -z "$JETTY_SSL_CLIENT_AUTH" ]; then
  JETTY_SSL_CLIENT_AUTH="true"
fi

# create payload for initializing connector using environment variables
JSON_TEMPLATE=`cat config-template.json`
JSON_STRING=$( printf "$JSON_TEMPLATE" "$JETTY_PORT" "$JETTY_SECURE" "$JETTY_SECURE_PORT" "$JETTY_SSL_CLIENT_AUTH" "$JETTY_SSL_KEYSTORE_PATH" "$JETTY_SSL_KEYSTORE_PASSWORD" "$JETTY_SSL_KEYSTORE_MANAGER_PASSWORD" "$JETTY_SSL_TRUSTSTORE_PATH" "$JETTY_SSL_TRUSTSTORE_PASSWORD" )

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