#!/bin/bash

set -eux;

# run rest catalog spring boot application.
java \
-cp *.jar \
-Xmx4G \
-Dloader.path=/opt/rest-catalog/ \
-Dspring.config.location=file:///opt/rest-catalog/conf/application.yml \
org.springframework.boot.loader.PropertiesLauncher