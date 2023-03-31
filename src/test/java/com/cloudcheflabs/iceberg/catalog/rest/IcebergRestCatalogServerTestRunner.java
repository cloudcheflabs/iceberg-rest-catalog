package com.cloudcheflabs.iceberg.catalog.rest;

import org.junit.Test;

public class IcebergRestCatalogServerTestRunner {

    @Test
    public void runServer() throws Exception {

        IcebergRestCatalogServer icebergRestCatalogServer = new IcebergRestCatalogServer();
        icebergRestCatalogServer.run();

        Thread.sleep(Long.MAX_VALUE);
    }

}
