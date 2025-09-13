from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from routers import report
import os
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(
    title="勤怠管理システム PDFレポートAPI",
    description="Spring Boot連携によるPDFレポート生成サービス",
    version="1.0.0"
)

# CORSミドルウェア設定
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "https://*.railway.app"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 静的ファイル配信（生成されたPDF用）
os.makedirs("static/reports", exist_ok=True)
app.mount("/static", StaticFiles(directory="static"), name="static")

# ルーター追加
app.include_router(report.router, prefix="/reports", tags=["reports"])

@app.get("/")
async def root():
    return {"message": "勤怠管理システム PDFレポートAPI", "port": 8081}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8081, reload=True)
