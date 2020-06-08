package com.marklogic.kafka.connect.source.jetty;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProducerServerFactoryTest {

    Logger logger = LoggerFactory.getLogger(ProducerServerFactoryTest.class);

    @Test
    public void testUnsecuredServer() {
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

    @Test
    public void testSecureServer() {
        Server server = ProducerServerFactory.createSecureServer(9090,
                "",
                "",
                "",
                "",
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
