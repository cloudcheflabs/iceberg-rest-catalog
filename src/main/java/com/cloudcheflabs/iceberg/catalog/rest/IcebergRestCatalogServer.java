package com.cloudcheflabs.iceberg.catalog.rest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.SessionCatalog;
import org.apache.iceberg.jdbc.JdbcCatalog;
import org.apache.iceberg.rest.RESTCatalog;
import org.apache.iceberg.rest.RESTCatalogAdapter;
import org.apache.iceberg.rest.RESTCatalogServlet;
import org.apache.iceberg.util.PropertyUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcebergRestCatalogServer {
    private static final Logger LOG = LoggerFactory.getLogger(IcebergRestCatalogServer.class);
    private static final String CATALOG_ENV_PREFIX = "CATALOG_";

    public IcebergRestCatalogServer() {
    }

    public void run(){
        try {
            RESTCatalogAdapter adapter = new RESTCatalogAdapter(backendCatalog());
            SessionCatalog.SessionContext sessionContext =
                    new SessionCatalog.SessionContext(
                            UUID.randomUUID().toString(),
                            "user",
                            ImmutableMap.of("credential", "user:secret"),
                            ImmutableMap.of());

            RESTCatalog catalog = new RESTCatalog(sessionContext, (config) -> adapter);
            catalog.initialize(
                    "prod",
                    ImmutableMap.of(
                            CatalogProperties.URI,
                            "ignored",
                            "credential",
                            "catalog:secret",
                            "token",
                            "bearer-token"));

            RESTCatalogServlet servlet = new RESTCatalogServlet(adapter);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/");
            ServletHolder servletHolder = new ServletHolder(servlet);
            servletHolder.setInitParameter("javax.ws.rs.Application", "ServiceListPublic");
            context.addServlet(servletHolder, "/*");
            context.setVirtualHosts(null);
            context.setGzipHandler(new GzipHandler());

            Server httpServer =
                    new Server(PropertyUtil.propertyAsInt(System.getenv(), "REST_PORT", 8181));
            httpServer.setHandler(context);

            httpServer.start();
            LOG.info("REST Catalog Server started...");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Catalog backendCatalog() throws IOException {
        // Translate environment variable to catalog properties
        Map<String, String> catalogProperties =
                System.getenv().entrySet().stream()
                        .filter(e -> e.getKey().startsWith(CATALOG_ENV_PREFIX))
                        .collect(
                                Collectors.toMap(
                                        e ->
                                                e.getKey()
                                                        .replaceFirst(CATALOG_ENV_PREFIX, "")
                                                        .replaceAll("__", "-")
                                                        .replaceAll("_", ".")
                                                        .toLowerCase(Locale.ROOT),
                                        Map.Entry::getValue,
                                        (m1, m2) -> {
                                            throw new IllegalArgumentException("Duplicate key: " + m1);
                                        },
                                        HashMap::new));

        // Fallback to a JDBCCatalog impl if one is not set
        catalogProperties.putIfAbsent(
                CatalogProperties.CATALOG_IMPL, "org.apache.iceberg.jdbc.JdbcCatalog");

        catalogProperties.putIfAbsent(
                CatalogProperties.URI, "jdbc:sqlite:file:/tmp/iceberg_rest_mode=memory");

        catalogProperties.putIfAbsent(
                JdbcCatalog.PROPERTY_PREFIX + "username", "user");

        catalogProperties.putIfAbsent(
                JdbcCatalog.PROPERTY_PREFIX + "password", "password");

        // Configure a default location if one is not specified
        String warehouseLocation = catalogProperties.get(CatalogProperties.WAREHOUSE_LOCATION);

        if (warehouseLocation == null) {
            File tmp = java.nio.file.Files.createTempDirectory("iceberg_warehouse").toFile();
            tmp.deleteOnExit();
            warehouseLocation = tmp.toPath().resolve("iceberg_data").toFile().getAbsolutePath();
            catalogProperties.put(CatalogProperties.WAREHOUSE_LOCATION, warehouseLocation);

            LOG.info("No warehouse location set.  Defaulting to temp location: {}", warehouseLocation);
        }

        LOG.info("Creating catalog with properties: {}", catalogProperties);
        return CatalogUtil.buildIcebergCatalog("rest_backend", catalogProperties, new Configuration());
    }
}