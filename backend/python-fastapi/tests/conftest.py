import pytest
import asyncio
import os
import tempfile
from unittest.mock import Mock

# テスト用の環境変数設定
@pytest.fixture(autouse=True)
def setup_test_env():
    """テスト用の環境変数を設定"""
    os.environ["SPRING_BOOT_URL"] = "http://localhost:8080"
    os.environ["ENVIRONMENT"] = "test"
    yield
    # テスト後のクリーンアップ
    if "SPRING_BOOT_URL" in os.environ:
        del os.environ["SPRING_BOOT_URL"]
    if "ENVIRONMENT" in os.environ:
        del os.environ["ENVIRONMENT"]

@pytest.fixture
def event_loop():
    """非同期テスト用のイベントループ"""
    loop = asyncio.new_event_loop()
    yield loop
    loop.close()

@pytest.fixture
def temp_directory():
    """テスト用の一時ディレクトリ"""
    with tempfile.TemporaryDirectory() as temp_dir:
        yield temp_dir

@pytest.fixture
def mock_requests():
    """requests ライブラリのモック"""
    with pytest.Mock() as mock:
        yield mock
