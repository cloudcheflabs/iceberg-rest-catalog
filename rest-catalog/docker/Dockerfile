FROM centos:7

ENTRYPOINT []

USER root

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN yum install -y tzdata openssl curl ca-certificates fontconfig gzip tar \
    && yum update -y; yum clean all


# install jdk.

RUN set -eux; \
    yum install java-11-openjdk java-11-openjdk-devel -y;


# install chango data api.
ENV CHANGO_DATA_API_HOME /opt/chango-data-api
ENV CHANGO_DATA_API_USER chango

RUN useradd -ms /bin/bash -d ${CHANGO_DATA_API_HOME} ${CHANGO_DATA_API_USER}


# add chango data-api jar.
ARG CHANGO_DATA_API_JAR
ADD ${CHANGO_DATA_API_JAR} ${CHANGO_DATA_API_HOME}/


# make conf directory.
RUN mkdir -p ${CHANGO_DATA_API_HOME}/conf

# add chango run shell.
ADD run-chango-data-api.sh ${CHANGO_DATA_API_HOME}/


# add permissions.
RUN chmod +x ${CHANGO_DATA_API_HOME}/*.sh
RUN chown ${CHANGO_DATA_API_USER}: -R ${CHANGO_DATA_API_HOME}

# change work directory.
USER ${CHANGO_DATA_API_USER}
WORKDIR ${CHANGO_DATA_API_HOME}
