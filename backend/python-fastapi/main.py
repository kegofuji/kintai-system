"""
FastAPI メインアプリケーション
PDFレポート生成専用サービス
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
import os
import uvicorn
from routers import report
import logging

# ログ設定
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# FastAPIアプリケーション作成
app = FastAPI(
    title="勤怠管理システム PDFレポート生成API",
    description="勤怠管理システム用のPDFレポート生成サービス",
    version="1.0.0",
    docs_url="/docs" if os.getenv("ENVIRONMENT") != "production" else None,
    redoc_url="/redoc" if os.getenv("ENVIRONMENT") != "production" else None
)

# CORS設定（Spring Boot連携用）
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:8080",  # Spring Boot開発環境
        "https://*.railway.app",   # Railway本番環境
        os.getenv("SPRING_BOOT_URL", "http://localhost:8080")
    ],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)

# 静的ファイル配信（生成されたPDFファイル用）
os.makedirs("generated_pdfs", exist_ok=True)
app.mount("/files", StaticFiles(directory="generated_pdfs"), name="files")

# ルーター登録
app.include_router(report.router, prefix="/reports", tags=["reports"])

# ヘルスチェックエンドポイント
@app.get("/health")
async def health_check():
    """ヘルスチェック"""
    return {
        "status": "healthy",
        "service": "pdf-generation-service",
        "version": "1.0.0"
    }

# ルートエンドポイント
@app.get("/")
async def root():
    """ルートパス"""
    return {
        "message": "勤怠管理システム PDFレポート生成API",
        "docs_url": "/docs",
        "health_url": "/health"
    }

# スタートアップイベント
@app.on_event("startup")
async def startup_event():
    """アプリケーション起動時の処理"""
    logger.info("PDFレポート生成サービス開始")
    
    # 必要なディレクトリの作成
    os.makedirs("generated_pdfs", exist_ok=True)
    os.makedirs("templates", exist_ok=True)
    
    logger.info("必要なディレクトリを作成しました")

# シャットダウンイベント
@app.on_event("shutdown")
async def shutdown_event():
    """アプリケーション終了時の処理"""
    logger.info("PDFレポート生成サービス終了")

# エラーハンドラー
@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    """一般例外ハンドラー"""
    logger.error(f"予期しないエラーが発生しました: {str(exc)}")
    raise HTTPException(
        status_code=500,
        detail="システムエラーが発生しました"
    )

# メイン実行部
if __name__ == "__main__":
    port = int(os.getenv("PORT", 8081))
    host = os.getenv("HOST", "0.0.0.0")
    
    uvicorn.run(
        "main:app",
        host=host,
        port=port,
        reload=os.getenv("ENVIRONMENT") == "development",
        log_level="info"
    )