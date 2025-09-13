#!/bin/bash

# Spring Boot テスト実行スクリプト
echo "=== Spring Boot テスト実行 ==="

# テスト用プロファイルでテスト実行
mvn test -Dspring.profiles.active=test

echo "=== テスト完了 ==="
