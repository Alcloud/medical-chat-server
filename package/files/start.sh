#!/bin/sh
echo "Migrate databases"
java -jar /tmp/TextSecureServer-1.88.jar accountdb migrate /config/sample1.yml
java -jar /tmp/TextSecureServer-1.88.jar messagedb migrate /config/sample1.yml
echo "Starting Signal Service"
java -jar /tmp/TextSecureServer-1.88.jar server /config/sample1.yml