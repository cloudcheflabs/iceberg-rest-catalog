package com.cloudcheflabs.iceberg.catalog.rest;

import com.cloudcheflabs.iceberg.catalog.rest.util.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;

import java.util.Arrays;

public class RunSparkWithIcebergRestCatalog {

    @Test
    public void runWithRestCatalog() throws Exception
    {
        String s3AccessKey = System.getProperty("s3AccessKey");
        String s3SecretKey = System.getProperty("s3SecretKey");
        String s3Endpoint = System.getProperty("s3Endpoint");
        String restUrl = System.getProperty("restUrl");
        String warehouse = System.getProperty("warehouse");
        String token = System.getProperty("token");

        SparkConf sparkConf = new SparkConf().setAppName("Run Spark with Iceberg REST Catalog");
        sparkConf.setMaster("local[2]");

        // iceberg catalog from hive metastore.
        sparkConf.set("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions");
        sparkConf.set("spark.sql.catalog.rest", "org.apache.iceberg.spark.SparkCatalog");
        sparkConf.set("spark.sql.catalog.rest.catalog-impl", "org.apache.iceberg.rest.RESTCatalog");
        sparkConf.set("spark.sql.catalog.rest.io-impl", "org.apache.iceberg.aws.s3.S3FileIO");
        sparkConf.set("spark.sql.catalog.rest.uri", restUrl);
        sparkConf.set("spark.sql.catalog.rest.warehouse", warehouse);
        sparkConf.set("spark.sql.catalog.rest.token", token);
        sparkConf.set("spark.sql.defaultCatalog", "rest");

        SparkSession spark = SparkSession
                .builder()
                .config(sparkConf)
                .enableHiveSupport()
                .getOrCreate();


        Configuration hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.s3a.endpoint", s3Endpoint);
        hadoopConfiguration.set("fs.s3a.access.key", s3AccessKey);
        hadoopConfiguration.set("fs.s3a.secret.key", s3SecretKey);
        hadoopConfiguration.set("fs.s3a.path.style.access", "true");
        hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        hadoopConfiguration.set("fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider");

//        // get table schema created by trino.
//        StructType schema = spark.table("rest.iceberg_db.test_iceberg").schema();
//
//        // read json.
//        String json = StringUtils.fileToString("data/test.json", true);
//        String lines[] = json.split("\\r?\\n");
//        Dataset<Row> df = spark.read().json(new JavaSparkContext(spark.sparkContext()).parallelize(Arrays.asList(lines)));
//
//        df.show(10);
//
//        // write to iceberg table.
//        Dataset<Row> newDf = spark.createDataFrame(df.javaRDD(), schema);
//        newDf.writeTo("rest.iceberg_db.test_iceberg").append();


        // show data in table.
        spark.table("rest.rest_db.ctas_again13").show(20);
    }
}