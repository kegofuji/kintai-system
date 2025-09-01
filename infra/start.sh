#!/bin/bash

# Nginxの起動
nginx

# Java アプリケーションの起動
java -jar /app/java/app.jar &

# Python アプリケーションの起動
cd /app/python && uvicorn main:app --host 0.0.0.0 --port 8000 &

# すべてのプロセスが終了するまで待機
wait
