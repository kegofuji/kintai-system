FROM python:3.11-slim

WORKDIR /app

# 必要なシステムパッケージインストール（WeasyPrint用）
RUN apt-get update && apt-get install -y \
    libpango-1.0-0 \
    libpangoft2-1.0-0 \
    libgdk-pixbuf-2.0-0 \
    libffi-dev \
    libcairo2 \
    libcairo-gobject2 \
    shared-mime-info \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 依存関係ファイルコピーとインストール
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# アプリケーションコードコピー
COPY . .

# static ディレクトリ作成
RUN mkdir -p static/reports

# 非rootユーザー作成
RUN adduser --disabled-password --gecos '' --uid 1000 appuser && \
    chown -R appuser:appuser /app
USER appuser

# ヘルスチェック
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD curl -f http://localhost:8081/docs || exit 1

EXPOSE 8081

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8081", "--workers", "1"]
