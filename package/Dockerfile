FROM openjdk:8-jre-alpine

MAINTAINER Fraunhofer FOKUS "ehealth@fokus.fraunhofer.de"

# java app associated files
ADD ./files/TextSecureServer-1.88.jar /tmp/TextSecureServer-1.88.jar
RUN chmod +x /tmp/TextSecureServer-1.88.jar

# start script associated files
ADD ./files/start.sh /tmp/start.sh
RUN chmod +x /tmp/start.sh

RUN mkdir config
ADD ./files/sample1.yml /config/sample1.yml
RUN chmod +x /config/sample1.yml

# Run REST Client
CMD ["sh", "/tmp/start.sh"]
