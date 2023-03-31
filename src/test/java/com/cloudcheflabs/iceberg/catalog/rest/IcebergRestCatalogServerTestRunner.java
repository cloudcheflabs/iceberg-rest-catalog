package com.cloudcheflabs.iceberg.catalog.rest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

public class IcebergRestCatalogServerTestRunner {

    @Before
    public void init() throws Exception {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(IcebergRestCatalogApplication.class, Arrays.asList("").toArray(new String[0]));
        applicationContext.registerShutdownHook();
    }

    @Test
    public void runServer() throws Exception {

        IcebergRestCatalogServer icebergRestCatalogServer = new IcebergRestCatalogServer();
        icebergRestCatalogServer.run();

        Thread.sleep(Long.MAX_VALUE);
    }

}
