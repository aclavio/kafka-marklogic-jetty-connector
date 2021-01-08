package com.marklogic.kafka.connect.source.jetty;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ProducerServerFactory {

    private static Logger logger = LoggerFactory.getLogger(ProducerServerFactory.class);

    public static void main(String[] args) throws Exception {
        Server server;

        if (args.length > 0) {
            server = ProducerServerFactory.createSecureServer(9090, args[0], args[1], args[2], args[3], args[4], Boolean.parseBoolean(args[5]));
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

    public static Handler createServletHandler() throws Exception {
         // Main Servlet for publishing to Kafka
        ServletHolder holder = new ServletHolder(new ProducerServlet());
        ServletContextHandler servletContext = new ServletContextHandler();
        servletContext.addServlet(holder, "/*");
        servletContext.setContextPath("/topics/");

        // Serve static resources
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        ContextHandler resourceContext = new ContextHandler();
        resourceContext.setContextPath("/");
        resourceContext.setWelcomeFiles(new String[]{"index.html"});
        resourceContext.setBaseResource(Resource.newResource(getWebrootUri()));
        resourceContext.setHandler(resourceHandler);

        ContextHandlerCollection contexts = new ContextHandlerCollection(
                servletContext,
                resourceContext
        );

        return contexts;
    }

    public static Server createServer(int port) throws Exception {
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
                                      boolean clientAuth) throws Exception {
        Server server = new Server();

        // HTTP configuration
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(port);
        httpConfig.setSendServerVersion(true);
        httpConfig.setSendDateHeader(false);
        httpConfig.addCustomizer(new SecureRequestCustomizer());

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
