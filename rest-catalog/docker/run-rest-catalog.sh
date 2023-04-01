#!/bin/bash

set -eux;

# run chango data-api spring boot application.
java \
-cp *.jar \
-Xmx4G \
-Dloader.path=/opt/chango-data-api/ \
-Dspring.config.location=file:///opt/chango-data-api/conf/application.yml \
org.springframework.boot.loader.PropertiesLauncher