package com.marklogic.kafka.connect.source.jetty;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ProducerServerFactory {

    private static Logger logger = LoggerFactory.getLogger(ProducerServerFactory.class);

    private ProducerServerFactory() {}

    public static Server createServer(int port) {
        Server server = new Server(port);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
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
        httpConfig.setSecurePort(8443);
        httpConfig.setSendServerVersion(true);
        httpConfig.setSendDateHeader(false);

        // Add the HTTP Connector
        //ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        //http.setPort(port);
        //server.addConnector(http);

        // Configure SSL, KeyStore, TrustStore, Ciphers
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystorePath);
        sslContextFactory.setKeyStorePassword(keystorePassword);
        sslContextFactory.setKeyManagerPassword(keystoreManagerPassword);
        sslContextFactory.setTrustStorePath(truststorePath);
        sslContextFactory.setTrustStorePassword(truststorePassword);
        // force client authentication
        sslContextFactory.setNeedClientAuth(clientAuth);

        logger.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        logger.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        logger.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        logger.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        logger.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

//        sslContextFactory.setIncludeCipherSuites("^.*_(MD5|SHA|SHA1)$,^TLS_RSA_.*$,^SSL_.*$,^.*_NULL_.*$,^.*_anon_.*$");
//        sslContextFactory.setExcludeCipherSuites("");
//        sslContextFactory.setIncludeProtocols("SSL", "SSLv2", "SSLv2Hello", "SSLv3");
//        sslContextFactory.setExcludeProtocols("");

        sslContextFactory.setIncludeProtocols("TLSv1.2", "TLSv1.1", "TLSv1");

        //sslContextFactory.setExcludeCipherSuites("^.*_(MD5)$");
        //sslContextFactory.setExcludeProtocols("SSL,SSLv2,SSLv2Hello,SSLv3");

        logger.info("getExcludeCipherSuites: {}", String.join(",", sslContextFactory.getExcludeCipherSuites()));
        logger.info("getIncludeCipherSuites: {}", String.join(",", sslContextFactory.getIncludeCipherSuites()));

        logger.info("getExcludeProtocols: {}", String.join(",", sslContextFactory.getExcludeProtocols()));
        logger.info("getIncludeProtocols: {}", String.join(",", sslContextFactory.getIncludeProtocols()));

        // SSL HTTP Configuration
        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        // Add the SSL HTTP Connector
        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(httpsConfig));
        sslConnector.setPort(8443);
        server.addConnector(sslConnector);

        // Servlet Setup
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
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

        return server;
    }
}
