CREATE TABLE rest.iceberg_db.test_iceberg (
    baseproperties STRUCT<eventtype: string,
                       ts: bigint,
                       uid: string,
                       version: string>,
    itemid string,
    price bigint,
    quantity bigint
)
USING iceberg
PARTITIONED BY (itemid)