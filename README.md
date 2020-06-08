# kafka-marklogic-jetty-connector

This is a connector for publishing to Kafka queues from MarkLogic.  It utilizes an embedded Jetty Servlet to listen for REST messages sent by MarkLogic using the included server-side-javascript module.

## Requirements
* MarkLogic 9+

## Quick Start

#### To try this out locally:

1. Configure kafkaHome in gradle-local.properties - e.g. kafkaHome=/Users/myusername/tools/kafka_2.11-2.1.0
1. Run "./gradlew deploy" to build a jar and copy it and the below property files into the appropriate Kafka directories

#### To try this out on a remote Kafka server
1. Run "./gradlew jar" to build the jar.
1. Copy the jar to the <kafkaHome>/libs on the remote server.
1. Copy the two properties (config/marklogic-connect-(distributed|standalone).properties config/marklogic-jetty-source.properties) to <kafkaHome>/config on the remote server.

See https://kafka.apache.org/quickstart for instructions on starting up Zookeeper and Kafka.

To start the Kafka connector in standalone mode (from the Kafka home directory):

    bin/connect-standalone.sh config/marklogic-connect-standalone.properties config/marklogic-jetty-source.properties

Check that Jetty has bound to it's configured port.

You can also start the connector in distributed mode:

    bin/connect-distributed.sh config/marklogic-connect-distributed.properties config/marklogic-jetty-source.properties

To send messages to Kafka via the included MarkLogic Module:
    
     'use strict';
     
     const KafkaClient = require('/kafka-client.sjs');
     let client = new KafkaClient('localhost', 9090);
     client.post('mytopic', {
       hello: "World!"
     }, {
        credentialId: xdmp.credentialId('myCredentialName')
     });

## Configuring the connector

#### Connector-specific properties are defined in config/marklogic-connect-standalone.properties
| Property | Default Value | Description |
|:-------- |:--------------|:------------|
| bootstrap.servers              | 9092                                             | This points to the Kafka server and port                                                                                                                                      |
| key.converter                  | org.apache.kafka.connect.storage.StringConverter | This controls the format of the data that will be written to Kafka for source connectors or read from Kafka for sink connectors.                                              |
| value.converter                | org.apache.kafka.connect.storage.StringConverter | This controls the format of the data that will be written to Kafka for source connectors or read from Kafka for sink connectors.                                              |
| key.converter.schemas.enable   | false                                            | Control the use of schemas for keys                                                                                                                                           |
| value.converter.schemas.enable | false                                            | Control the use of schemas for values                                                                                                                                         |
| offset.storage.file.filename   | /tmp/connect.offsets                             | The file to store connector offsets in. By storing offsets on disk, a standalone process can be stopped and started on a single node and resume where it previously left off. |
| offset.flush.interval.ms       | 10000                                            | Interval at which to try committing offsets for tasks.                                                                                                                        |

#### MarkLogic and Jetty specific properties are defined in config/marklogic-jetty-source.properties
| Property | Default Value | Description |
|:-------- |:--------------|:------------|
| name | marklogic-source | The name of the connector |
| connector.class | com.marklogic.kafka.connect.source.jetty.MarkLogicSourceConnector | The FQ name of the connector class |
| tasks.max | 1 | The maximum number of concurrent tasks |
| jetty.port |  | Jetty Server port |
| jetty.secure | false | Use secure jetty server? |
| jetty.ssl.keystore.path |  | Path the the keystore used by secure Jetty |
| jetty.ssl.keystore.password |  | keystore password |
| jetty.ssl.keystore.manager.password |  | keystore manager password |
| jetty.ssl.truststore.path |  | Path the the truststore used by secure Jetty |
| jetty.ssl.truststore.password |  | truststore password |
| jetty.ssl.client.auth | false | require client authentication |
