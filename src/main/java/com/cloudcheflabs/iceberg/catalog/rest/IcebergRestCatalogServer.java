package com.cloudcheflabs.iceberg.catalog.rest;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.cloudcheflabs.iceberg.catalog.rest.filter.RequestFilter;
import com.cloudcheflabs.iceberg.catalog.rest.util.StringUtils;
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
import org.checkerframework.checker.units.qual.C;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;

@Component
public class IcebergRestCatalogServer implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(IcebergRestCatalogServer.class);
    private static final String CATALOG_ENV_PREFIX = "CATALOG_";

    public static final String ENV_S3_ACCESS_KEY = "S3_ACCESS_KEY";
    public static final String ENV_S3_SECRET_KEY = "S3_SECRET_KEY";
    public static final String ENV_S3_ENDPOINT = "S3_ENDPOINT";

    private String s3AccessKey;
    private String s3SecretKey;
    private String s3Endpoint;

    @Override
    public void afterPropertiesSet() throws Exception {
        s3AccessKey = StringUtils.getEnv(ENV_S3_ACCESS_KEY);
        LOG.info("s3AccessKey: {}", s3AccessKey);

        s3SecretKey = StringUtils.getEnv(ENV_S3_SECRET_KEY);
        LOG.info("s3SecretKey: {}", s3SecretKey);

        s3Endpoint = StringUtils.getEnv(ENV_S3_ENDPOINT);
        LOG.info("s3Endpoint: {}", s3Endpoint);

        run();
    }

    public void run(){
        try {
            RESTCatalogAdapter adapter = new RESTCatalogAdapter(backendCatalog());
            RESTCatalogServlet servlet = new RESTCatalogServlet(adapter);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/");
            ServletHolder servletHolder = new ServletHolder(servlet);
            servletHolder.setInitParameter("javax.ws.rs.Application", "ServiceListPublic");
            context.addFilter(RequestFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
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

    private Catalog backendCatalog() throws IOException {
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
        Configuration configuration = new Configuration();
        if(warehouseLocation.startsWith("s3a")) {
            if(s3AccessKey == null) {
                throw new RuntimeException("Env. value of S3_ACCESS_KEY for S3 access key is null!");
            }
            configuration.set("fs.s3a.access.key", s3AccessKey);

            if(s3SecretKey == null) {
                throw new RuntimeException("Env. value of S3_SECRET_KEY for S3 secret key is null!");
            }
            configuration.set("fs.s3a.secret.key", s3SecretKey);

            if(s3Endpoint != null) {
                configuration.set("fs.s3a.endpoint", s3Endpoint);
            }
            configuration.set("fs.s3a.path.style.access", "true");
        }

        if (warehouseLocation == null) {
            File tmp = java.nio.file.Files.createTempDirectory("iceberg_warehouse").toFile();
            tmp.deleteOnExit();
            warehouseLocation = tmp.toPath().resolve("iceberg_data").toFile().getAbsolutePath();
            catalogProperties.put(CatalogProperties.WAREHOUSE_LOCATION, warehouseLocation);

            LOG.info("No warehouse location set.  Defaulting to temp location: {}", warehouseLocation);
        }

        LOG.info("Creating catalog with properties: {}", catalogProperties);
        return CatalogUtil.buildIcebergCatalog("rest_backend", catalogProperties, configuration);
    }
}