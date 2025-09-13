FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Maven Wrapper追加
COPY .mvn ./.mvn
COPY mvnw .
RUN chmod +x ./mvnw

# 依存関係ダウンロードとビルド
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 必要なパッケージインストール
RUN apk --no-cache add curl

# アプリケーションJARコピー
COPY --from=builder /app/target/kintai-backend-*.jar app.jar

# ユーザー追加（セキュリティ向上）
RUN addgroup -g 1000 appgroup && \
    adduser -D -s /bin/sh -u 1000 -G appgroup appuser
USER appuser

# ヘルスチェック追加
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# JVMオプション設定（Railway環境用）
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:MaxMetaspaceSize=256m"

CMD ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar app.jar"]
