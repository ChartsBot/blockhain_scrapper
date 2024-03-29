FROM --platform=linux/amd64 fomoinc/java-base
ARG JAR_LIBS
ARG JAR_FILE
ARG VERSION
ARG BUILD
ARG SERVICE_NAME

LABEL "com.fomotbot.service"="${SERVICE_NAME}"
LABEL "com.fomobot.version"="${VERSION}"
LABEL "com.fomobot.build"="${BUILD}"

EXPOSE 8081
EXPOSE 3306
EXPOSE 9010
EXPOSE 9020
EXPOSE 4321

ENV _JAVA_OPTIONS "-Xms128m -Xmx512m -Djava.awt.headless=true"

ENTRYPOINT [ \
  "/bin/bash", \
  "-c", \
  "exec /usr/bin/java \
   -XX:MaxRAM=$(( $(cat /sys/fs/cgroup/memory/memory.limit_in_bytes) - 254*1024*1024 )) \
   -Djava.awt.headless=true \
   -Djava.security.egd=file:/dev/./urandom \
   -Djava.rmi.server.hostname=localhost \
   -Dcom.sun.management.jmxremote \
   -Dcom.sun.management.jmxremote.port=9010 \
   -Dcom.sun.management.jmxremote.rmi.port=9010 \
   -Dcom.sun.management.jmxremote.local.only=false \
   -Dcom.sun.management.jmxremote.authenticate=false \
   -Dcom.sun.management.jmxremote.ssl=false \
   -Dconfig.resource=application-docker.conf \
   -Dlogback.configurationFile=logback-docker.xml \
   -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9020 \
   -jar /usr/share/service/main.jar" \
]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
COPY ${JAR_LIBS} /usr/share/service/lib
# Add the service itself
COPY ${JAR_FILE} /usr/share/service/main.jar

HEALTHCHECK --interval=10s --timeout=10s --start-period=30s \
    CMD curl --fail http://0.0.0.0:5001/api/v1/health/alive
