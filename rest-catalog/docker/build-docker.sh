#!/bin/bash


set -e -x

export REST_CATALOG_JAR=rest-catalog-1.1.0.jar;
export DATA_API_IMAGE=cloudcheflabs/rest-catalog:v1.1.0


for i in "$@"
do
case $i in
    --jar=*)
    REST_CATALOG_JAR="${i#*=}"
    shift
    ;;
    --image=*)
    DATA_API_IMAGE="${i#*=}"
    shift
    ;;
    *)
          # unknown option
    ;;
esac
done

echo "REST_CATALOG_JAR = ${REST_CATALOG_JAR}"
echo "DATA_API_IMAGE = ${DATA_API_IMAGE}"


# move to build directory.
cd ../


# build all.
mvn -e -DskipTests=true clean install;


# add fat jar to docker directory.
cp target/$REST_CATALOG_JAR docker;



set +e -x

## remove docker image.
export IMAGE_NAME_ALIAS=rest-catalog
docker rmi -f $(docker images -a | grep ${IMAGE_NAME_ALIAS} | awk '{print $3}')

set -e -x

## build.
docker build \
--build-arg REST_CATALOG_JAR=${REST_CATALOG_JAR} \
-t ${DATA_API_IMAGE} \
./docker;


## remove fat jar from docker directory.
rm -rf docker/$REST_CATALOG_JAR;

# push docker image.
docker push ${DATA_API_IMAGE};
