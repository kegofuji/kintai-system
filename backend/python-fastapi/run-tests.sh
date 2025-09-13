#!/bin/bash

# FastAPI テスト実行スクリプト
echo "=== FastAPI テスト実行 ==="

# 仮想環境の確認
if [ ! -d "venv" ]; then
    echo "仮想環境を作成しています..."
    python3 -m venv venv
fi

# 仮想環境の有効化
source venv/bin/activate

# 依存関係のインストール
echo "依存関係をインストールしています..."
pip install -r requirements.txt
pip install -r requirements-test.txt

# テスト実行
echo "テストを実行しています..."
pytest tests/ -v --tb=short

echo "=== テスト完了 ==="
