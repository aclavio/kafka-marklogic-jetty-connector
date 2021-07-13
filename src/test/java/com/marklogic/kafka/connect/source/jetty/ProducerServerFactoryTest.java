package com.marklogic.kafka.connect.source.jetty;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProducerServerFactoryTest {

    Logger logger = LoggerFactory.getLogger(ProducerServerFactoryTest.class);

    @Test
    public void testUnsecuredServer() throws Exception {
        Server server = ProducerServerFactory.createServer(9090);
        boolean success = false;

        try {
            server.start();
            Thread.sleep(5000);
            server.stop();
            success = true;
        } catch (Exception ex) {
            logger.info(ex.getMessage());
            ex.printStackTrace();
            assertTrue(false, "Jetty server exception");
            success = false;
        }

        assertTrue(success, "Jetty server error");
    }

    //@Test
    // TODO provide a test keystore
    public void testSecureServer() throws Exception {
        Server server = ProducerServerFactory.createSecureServer(9999,
                443,
                System.getProperty("javax.net.ssl.keyStore"),
                "",
                "",
                System.getProperty("javax.net.ssl.trustStore"),
                "",
                true);
        boolean success = false;

        try {
            server.start();
            Thread.sleep(5000);
            server.stop();
            success = true;
        } catch (Exception ex) {
            logger.info(ex.getMessage());
            ex.printStackTrace();
            assertTrue(false, "Jetty server exception");
            success = false;
        }

        assertTrue(success, "Jetty server error");
    }
}
