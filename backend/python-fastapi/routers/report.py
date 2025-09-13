from fastapi import APIRouter, HTTPException, BackgroundTasks
from fastapi.responses import FileResponse
from models.report_models import ReportRequest, ReportResponse
from services.pdf_service import PDFReportService
import os

router = APIRouter()
pdf_service = PDFReportService()

@router.post("/pdf", response_model=ReportResponse)
async def generate_pdf_report(request: ReportRequest, background_tasks: BackgroundTasks):
    """
    POST /reports/pdf - PDF生成エンドポイント（設計書仕様準拠）
    Spring Boot (Port: 8080) → FastAPI (Port: 8081) にPDF生成要求
    """
    try:
        result = await pdf_service.generate_monthly_report(
            request.employee_id, 
            request.year_month
        )
        
        return ReportResponse(
            success=result["success"],
            report_url=result["report_url"],
            expires=result["expires"]
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/download/{filename}")
async def download_pdf(filename: str):
    """
    GET /reports/download/{filename} - PDFダウンロードエンドポイント
    """
    file_path = f"static/reports/{filename}"
    
    if not os.path.exists(file_path):
        raise HTTPException(status_code=404, detail="PDFファイルが見つかりません")
    
    return FileResponse(
        path=file_path,
        media_type="application/pdf",
        filename=filename,
        headers={"Content-Disposition": f"attachment; filename={filename}"}
    )