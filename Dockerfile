# ---- Stage 1: Build the application ----
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Copy the entire repo
COPY . .

# Install Gradle 8.10
RUN apt update && apt install -y wget unzip \
    && wget https://services.gradle.org/distributions/gradle-8.10-bin.zip -O gradle.zip \
    && unzip gradle.zip -d /opt/ \
    && ln -s /opt/gradle-8.10/bin/gradle /usr/bin/gradle \
    && rm gradle.zip

# Run Gradle build
RUN gradle --console=plain --info distTar


# ---- Stage 2: Create the final image ----
FROM eclipse-temurin:17-jre-jammy

ARG TARGET_DIR=/opt/app
ARG SOURCE_DIR=/app/build/distributions

WORKDIR $TARGET_DIR

# Copy the built application from the builder stage
COPY --from=builder $SOURCE_DIR/*.tar application.tar

RUN tar -xf application.tar -C $TARGET_DIR && rm application.tar

ARG DOCKER_USER=app
RUN groupadd -r $DOCKER_USER && useradd -rg $DOCKER_USER $DOCKER_USER
USER $DOCKER_USER

EXPOSE 8080/tcp
EXPOSE 8085/tcp

CMD [ "/opt/app/application/bin/application" ]