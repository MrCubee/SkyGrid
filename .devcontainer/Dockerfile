FROM alpine AS buildtools
RUN apk add --no-cache bash git openjdk25 && mkdir -p /opt/spigot-builder /build
ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar /opt/spigot-builder/BuildTools.jar
WORKDIR /build
RUN java -jar /opt/spigot-builder/BuildTools.jar --rev 26.2

FROM alpine
RUN apk add --no-cache bash git openjdk25
COPY --from=buildtools /root/.m2 /root/.m2
