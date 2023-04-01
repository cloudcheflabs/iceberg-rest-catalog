FROM centos:7

ENTRYPOINT []

USER root

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN yum install -y tzdata openssl curl ca-certificates fontconfig gzip tar \
    && yum update -y; yum clean all


# install jdk.

RUN set -eux; \
    yum install java-11-openjdk java-11-openjdk-devel -y;


# install rest catalog.
ENV REST_CATALOG_HOME /opt/rest-catalog
ENV REST_CATALOG_USER rest

RUN useradd -ms /bin/bash -d ${REST_CATALOG_HOME} ${REST_CATALOG_USER}


# add rest catalog jar.
ARG REST_CATALOG_JAR
ADD ${REST_CATALOG_JAR} ${REST_CATALOG_HOME}/


# make conf directory.
RUN mkdir -p ${REST_CATALOG_HOME}/conf

# add rest catalog run shell.
ADD run-rest-catalog.sh ${REST_CATALOG_HOME}/


# add permissions.
RUN chmod +x ${REST_CATALOG_HOME}/*.sh
RUN chown ${REST_CATALOG_USER}: -R ${REST_CATALOG_HOME}

# change work directory.
USER ${REST_CATALOG_USER}
WORKDIR ${REST_CATALOG_HOME}
