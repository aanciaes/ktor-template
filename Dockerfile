# Stage 1 - assemble the ktor application by generating a fat jar
FROM gradle:6.6.1-jdk11 as jar

# Set the work dir
WORKDIR /home/gradle/project
COPY ./ /home/gradle/project
RUN gradle clean shadowJar

# Stage 2 - run the application
FROM openjdk:11-jre-slim-buster

ARG APP_NAME="ktor-template"
ENV JAR_NAME=${APP_NAME}

ARG APP_VERSION="0.0.1"
ENV JAR_VERSION=${APP_VERSION}

# Setup jattach so that we have some of the tooling from the JDK in the JRE
COPY ./docker-resources/jattach /usr/local/bin/jattach
RUN chmod +x /usr/local/bin/jattach

# Create user
RUN groupadd --gid 1000 app \
    && useradd --uid 1000 --gid app --shell /bin/bash --create-home app

# Set the workdir
WORKDIR /home/app
COPY --from=jar /home/gradle/project/build/libs/${JAR_NAME}-${JAR_VERSION}-all.jar app.jar

# Make sure everything is owned by the new user
RUN chown -cR app:app /home/app

# Provide some default jvm options, but these should be overriden
ENV JAVA_OPTS="-server -XX:+UseStringDeduplication"

# Entrypoint
USER app
RUN  echo "${JAVA_OPTS}"
CMD java $JAVA_OPTS -jar app.jar