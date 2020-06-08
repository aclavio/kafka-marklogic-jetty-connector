package com.marklogic.kafka.connect.source.jetty;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ProducerServerFactory {

    private static Logger logger = LoggerFactory.getLogger(ProducerServerFactory.class);

    private ProducerServerFactory() {}

    public static ServletHandler createServletHandler() {
        ServletHandler handler = new ServletHandler();
        // Main Servlet for publishing to Kafka
        ServletHolder holder = new ServletHolder(new ProducerServlet());
        handler.addServletWithMapping(holder, "/topics/*");
        // Redirect Servlet for root requests
        ServletHolder redirectHandler = new ServletHolder();
        redirectHandler.setServlet(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.sendRedirect("/topics/");
            }
        });
        handler.addServletWithMapping(redirectHandler, "/*");
        return handler;
    }

    public static Server createServer(int port) {
        Server server = new Server(port);
        server.setHandler(createServletHandler());
        return server;
    }

    public static Server createSecureServer(int port,
                                      String keystorePath,
                                      String keystorePassword,
                                      String keystoreManagerPassword,
                                      String truststorePath,
                                      String truststorePassword,
                                      boolean clientAuth) {
        Server server = new Server();

        // HTTP configuration
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(port);
        httpConfig.setSendServerVersion(true);
        httpConfig.setSendDateHeader(false);

        // Configure SSL, KeyStore, TrustStore, Ciphers
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystorePath);
        sslContextFactory.setKeyStorePassword(keystorePassword);
        sslContextFactory.setKeyManagerPassword(keystoreManagerPassword);
        sslContextFactory.setTrustStorePath(truststorePath);
        sslContextFactory.setTrustStorePassword(truststorePassword);

        // force client authentication
        sslContextFactory.setNeedClientAuth(clientAuth);

        // acceptable protocols and ciphers
        sslContextFactory.setIncludeCipherSuites("^TLS.*$");
        sslContextFactory.setIncludeProtocols("TLSv1.3", "TLSv1.2", "TLSv1.1");

        logger.debug("getExcludeCipherSuites: {}", String.join(",", sslContextFactory.getExcludeCipherSuites()));
        logger.debug("getIncludeCipherSuites: {}", String.join(",", sslContextFactory.getIncludeCipherSuites()));
        logger.debug("getExcludeProtocols: {}", String.join(",", sslContextFactory.getExcludeProtocols()));
        logger.debug("getIncludeProtocols: {}", String.join(",", sslContextFactory.getIncludeProtocols()));

        // SSL HTTP Configuration
        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        // Add the SSL HTTP Connector
        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig));
        sslConnector.setPort(port);
        server.addConnector(sslConnector);

        // Servlet Setup
        server.setHandler(createServletHandler());

        return server;
    }
}
