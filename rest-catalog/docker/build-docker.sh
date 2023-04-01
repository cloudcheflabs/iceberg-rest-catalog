#!/bin/bash


set -e -x

export CHANGO_DATA_API_JAR=chango-data-api-1.0.0-SNAPSHOT.jar;
export DATA_API_IMAGE=cloudcheflabs/chango-data-api:v1.0.0


for i in "$@"
do
case $i in
    --jar=*)
    CHANGO_DATA_API_JAR="${i#*=}"
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

echo "CHANGO_DATA_API_JAR = ${CHANGO_DATA_API_JAR}"
echo "DATA_API_IMAGE = ${DATA_API_IMAGE}"


# move to build directory.
cd ../


# build all.
mvn -e -DskipTests=true clean install;


# add fat jar to docker directory.
cp target/$CHANGO_DATA_API_JAR docker;



set +e -x

## remove docker image.
export IMAGE_NAME_ALIAS=chango-data-api
docker rmi -f $(docker images -a | grep ${IMAGE_NAME_ALIAS} | awk '{print $3}')

set -e -x

## build.
docker build \
--build-arg CHANGO_DATA_API_JAR=${CHANGO_DATA_API_JAR} \
-t ${DATA_API_IMAGE} \
./docker;


## remove fat jar from docker directory.
rm -rf docker/$CHANGO_DATA_API_JAR;

# push docker image.
docker push ${DATA_API_IMAGE};
