FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle --no-daemon build -x test --stacktrace || true
COPY . .
RUN gradle --no-daemon bootJar --stacktrace -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y python3 python3-pip && rm -rf /var/lib/apt/lists/*
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt


COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/scripts ./scripts
ENTRYPOINT ["java", "-jar", "build/libs/app.jar"]