CREATE TABLE IF NOT EXISTS rest.iceberg_db.test_iceberg (
    baseproperties STRUCT<eventtype: string,
                       ts: long,
                       uid: string,
                       version: string>,
    itemid string,
    price long,
    quantity long
)
USING Iceberg
PARTITIONED BY (itemid)