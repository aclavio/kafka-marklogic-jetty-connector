package com.marklogic.kafka.connect.source.jetty;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProducerServerFactory {

    private static Logger logger = LoggerFactory.getLogger(ProducerServerFactory.class);

    public static void main(String[] args) throws Exception {
        Server server;

        if (args.length > 0) {
            server = ProducerServerFactory.createSecureServer(9090, 443, args[0], args[1], args[2], args[3], args[4], Boolean.parseBoolean(args[5]));
        } else {
            server = ProducerServerFactory.createServer(9090);
        }

        server.start();
        server.join();
    }

    private ProducerServerFactory() {}

    public static URI getWebrootUri() throws IllegalStateException, URISyntaxException {
        URL webRootLocation = ProducerServlet.class.getClassLoader().getResource("index.html");
        if (webRootLocation == null) {
            throw new IllegalStateException("unable to determine webroot url location");
        }

        URI webRootUri = URI.create(webRootLocation.toURI().toASCIIString().replaceFirst("/index.html$", "/"));
        logger.error("webRootUrl {}", webRootLocation);
        logger.error("webRootUri {}", webRootUri);

        return webRootUri;
    }

    public static HandlerList createServletHandler(boolean secure) throws Exception {

        HandlerList handlers = new HandlerList();
        String[] serverNames;

        if (secure) {
            serverNames = new String[]{"@secured"};
            handlers.addHandler(new SecuredRedirectHandler());
        } else {
            serverNames = new String[]{"@unsecured"};
        }

        // HealthCheck handler
        ContextHandler healthCheckHandler = new ContextHandler();
        healthCheckHandler.setContextPath("/timestamp/");
        healthCheckHandler.setHandler(new HealthCheckHandler());
        healthCheckHandler.setVirtualHosts(serverNames);
        handlers.addHandler(healthCheckHandler);

         // Main Servlet for publishing to Kafka
        ServletHolder holder = new ServletHolder(new ProducerServlet());
        ServletContextHandler servletContext = new ServletContextHandler();
        servletContext.addServlet(holder, "/*");
        servletContext.setContextPath("/topics/");
        servletContext.setVirtualHosts(serverNames);
        handlers.addHandler(servletContext);

        // Serve static resources
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        ContextHandler resourceContext = new ContextHandler();
        resourceContext.setContextPath("/");
        resourceContext.setWelcomeFiles(new String[]{"index.html"});
        resourceContext.setBaseResource(Resource.newResource(getWebrootUri()));
        resourceContext.setHandler(resourceHandler);
        resourceContext.setVirtualHosts(serverNames);
        handlers.addHandler(resourceContext);

        return handlers;
    }

    public static Server createServer(int port) throws Exception {
        Server server = new Server(port);
        // HTTP configuration
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(true);
        httpConfig.setSendDateHeader(false);
        // Create the HTTP Connector
        ServerConnector httpConnector = new ServerConnector(server,
                new HttpConnectionFactory(httpConfig));
        httpConnector.setName("unsecured");
        httpConnector.setPort(port);
        // Add connectors to the server
        server.setConnectors(new Connector[] { httpConnector });
        server.setHandler(createServletHandler(false));
        return server;
    }

    public static Server createSecureServer(int port,
                                      int securePort,
                                      String keystorePath,
                                      String keystorePassword,
                                      String keystoreManagerPassword,
                                      String truststorePath,
                                      String truststorePassword,
                                      boolean clientAuth) throws Exception {
        Server server = new Server();

        // HTTP configuration
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(securePort);
        httpConfig.setSendServerVersion(true);
        httpConfig.setSendDateHeader(false);

        // HTTPS configuration
        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

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
        //sslContextFactory.setIncludeCipherSuites("^TLS.*$");
        //sslContextFactory.setIncludeProtocols("TLSv1.3", "TLSv1.2", "TLSv1.1");
        //sslContextFactory.setExcludeCipherSuites("");
        //sslContextFactory.setExcludeProtocols("");

        logger.debug("getExcludeCipherSuites: {}", String.join(",", sslContextFactory.getExcludeCipherSuites()));
        logger.debug("getIncludeCipherSuites: {}", String.join(",", sslContextFactory.getIncludeCipherSuites()));
        logger.debug("getExcludeProtocols: {}", String.join(",", sslContextFactory.getExcludeProtocols()));
        logger.debug("getIncludeProtocols: {}", String.join(",", sslContextFactory.getIncludeProtocols()));

        // Create the HTTP Connector
        ServerConnector httpConnector = new ServerConnector(server,
                new HttpConnectionFactory(httpConfig));
        httpConnector.setName("unsecured");
        httpConnector.setPort(port);

        // Create the SSL HTTPS Connector
        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig));
        sslConnector.setName("secured");
        sslConnector.setPort(securePort);

        // Add connectors to the server
        server.setConnectors(new Connector[] { httpConnector, sslConnector });

        // Servlet Setup
        server.setHandler(createServletHandler(true));

        return server;
    }

    public static class HealthCheckHandler extends AbstractHandler {

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("text/plain");
            response.getWriter().println(new Date().toString());
            response.setStatus(200);
            baseRequest.setHandled(true);
        }
    }
}
