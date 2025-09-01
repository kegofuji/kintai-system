from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from routers import report
import uvicorn

app = FastAPI(
    title="勤怠管理システム Report API",
    description="勤怠管理システムのレポート生成APIサービス",
    version="1.0.0"
)

# CORSミドルウェアの設定
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],  # フロントエンドのオリジン
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ルーターの登録
app.include_router(report.router, prefix="/api/reports", tags=["reports"])

@app.get("/")
async def root():
    return {"message": "勤怠管理システム Report API"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
