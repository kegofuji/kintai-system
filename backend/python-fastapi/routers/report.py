"""
レポート生成ルーター
Spring Boot → FastAPI (Port: 8081) にPDF生成要求
エンドポイント: POST /reports/pdf
"""

from fastapi import APIRouter, HTTPException, BackgroundTasks
from fastapi.responses import FileResponse
import os
import asyncio
from datetime import datetime, timedelta
import logging
from models.report_models import ReportRequest, ReportResponse
from services.pdf_service import PDFReportService

logger = logging.getLogger(__name__)

router = APIRouter()
pdf_service = PDFReportService()

@router.post("/pdf", response_model=ReportResponse)
async def generate_pdf_report(request: ReportRequest, background_tasks: BackgroundTasks):
    """
    PDFレポート生成エンドポイント
    Spring Boot: POST /api/reports/generate
    FastAPI: POST /reports/pdf
    """
    try:
        logger.info(f"PDF生成要求受信: 社員ID={request.employee_id}, 年月={request.year_month}")
        
        # PDFファイル生成
        pdf_filename = await pdf_service.generate_monthly_report(
            request.employee_id, 
            request.year_month
        )
        
        if not pdf_filename:
            raise HTTPException(
                status_code=500, 
                detail="PDFファイルの生成に失敗しました"
            )
        
        # 一時URL生成（24時間後に自動削除）
        pdf_url = f"/files/{pdf_filename}"
        expires_at = datetime.now() + timedelta(hours=24)
        
        # 24時間後の自動削除スケジューリング
        background_tasks.add_task(
            schedule_file_deletion, 
            pdf_filename, 
            expires_at
        )
        
        logger.info(f"PDF生成完了: {pdf_filename}")
        
        return ReportResponse(
            success=True,
            pdf_url=pdf_url,
            filename=pdf_filename,
            expires_at=expires_at.isoformat(),
            message="PDFレポートを生成しました"
        )
        
    except Exception as e:
        logger.error(f"PDF生成エラー: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"PDFレポート生成中にエラーが発生しました: {str(e)}"
        )

@router.get("/download/{filename}")
async def download_pdf(filename: str):
    """
    PDF ダウンロードエンドポイント
    生成されたPDFファイルのダウンロード
    """
    try:
        file_path = f"generated_pdfs/{filename}"
        
        if not os.path.exists(file_path):
            raise HTTPException(
                status_code=404, 
                detail="指定されたファイルが見つかりません"
            )
        
        # ファイルの有効期限チェック
        file_stat = os.stat(file_path)
        file_age = datetime.now() - datetime.fromtimestamp(file_stat.st_mtime)
        
        if file_age > timedelta(hours=24):
            # 期限切れファイルを削除
            os.remove(file_path)
            raise HTTPException(
                status_code=404, 
                detail="ファイルの有効期限が切れています"
            )
        
        return FileResponse(
            path=file_path,
            filename=filename,
            media_type='application/pdf'
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"ファイルダウンロードエラー: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail="ファイルダウンロード中にエラーが発生しました"
        )

@router.delete("/cleanup")
async def cleanup_expired_files():
    """
    期限切れファイルのクリーンアップ
    管理用エンドポイント
    """
    try:
        pdf_dir = "generated_pdfs"
        if not os.path.exists(pdf_dir):
            return {"message": "削除対象のディレクトリが存在しません"}
        
        deleted_count = 0
        current_time = datetime.now()
        
        for filename in os.listdir(pdf_dir):
            file_path = os.path.join(pdf_dir, filename)
            
            if os.path.isfile(file_path):
                file_stat = os.stat(file_path)
                file_age = current_time - datetime.fromtimestamp(file_stat.st_mtime)
                
                # 24時間以上経過したファイルを削除
                if file_age > timedelta(hours=24):
                    os.remove(file_path)
                    deleted_count += 1
                    logger.info(f"期限切れファイルを削除: {filename}")
        
        return {
            "message": f"{deleted_count}個のファイルを削除しました",
            "deleted_count": deleted_count
        }
        
    except Exception as e:
        logger.error(f"ファイルクリーンアップエラー: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail="ファイルクリーンアップ中にエラーが発生しました"
        )

async def schedule_file_deletion(filename: str, expires_at: datetime):
    """
    ファイル自動削除のスケジューリング
    24時間後に自動削除
    """
    try:
        # 削除時刻まで待機
        now = datetime.now()
        if expires_at > now:
            sleep_seconds = (expires_at - now).total_seconds()
            await asyncio.sleep(sleep_seconds)
        
        # ファイル削除実行
        file_path = f"generated_pdfs/{filename}"
        if os.path.exists(file_path):
            os.remove(file_path)
            logger.info(f"自動削除実行: {filename}")
        
    except Exception as e:
        logger.error(f"ファイル自動削除エラー: {str(e)}")

@router.get("/status")
async def get_service_status():
    """
    サービスステータス確認
    """
    try:
        pdf_dir = "generated_pdfs"
        file_count = 0
        total_size = 0
        
        if os.path.exists(pdf_dir):
            for filename in os.listdir(pdf_dir):
                file_path = os.path.join(pdf_dir, filename)
                if os.path.isfile(file_path):
                    file_count += 1
                    total_size += os.path.getsize(file_path)
        
        return {
            "status": "active",
            "generated_files_count": file_count,
            "total_size_mb": round(total_size / (1024 * 1024), 2),
            "service": "PDF Generation Service",
            "version": "1.0.0"
        }
        
    except Exception as e:
        logger.error(f"ステータス取得エラー: {str(e)}")
        return {
            "status": "error",
            "message": str(e)
        }