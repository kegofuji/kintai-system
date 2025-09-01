# ベースイメージの選択
FROM openjdk:17-slim as java-builder

# 作業ディレクトリの設定
WORKDIR /app/java

# ソースコードのコピー
COPY backend/java-springboot/pom.xml .
COPY backend/java-springboot/src ./src

# Mavenでビルド
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package

# Pythonイメージの準備
FROM python:3.9-slim as python-builder

# 作業ディレクトリの設定
WORKDIR /app/python

# 必要なファイルのコピー
COPY backend/python-fastapi/requirements.txt .
COPY backend/python-fastapi/. .

# 依存関係のインストール
RUN pip install --no-cache-dir -r requirements.txt

# 最終イメージの作成
FROM openjdk:17-slim

# 必要なパッケージのインストール
RUN apt-get update && \
    apt-get install -y python3 python3-pip nginx && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Java アプリケーションのコピー
WORKDIR /app/java
COPY --from=java-builder /app/java/target/*.jar ./app.jar

# Python アプリケーションのコピー
WORKDIR /app/python
COPY --from=python-builder /app/python .
COPY --from=python-builder /usr/local/lib/python3.9/site-packages /usr/local/lib/python3.9/site-packages

# フロントエンドのコピーとNginxの設定
COPY frontend/public_ui /usr/share/nginx/html
COPY infra/nginx.conf /etc/nginx/nginx.conf

# ポートの公開
EXPOSE 80 8080 8000

# 起動スクリプト
COPY infra/start.sh /start.sh
RUN chmod +x /start.sh

# アプリケーションの起動
CMD ["/start.sh"]
