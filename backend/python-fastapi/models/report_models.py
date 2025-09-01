from typing import Optional
from pydantic import BaseModel
from datetime import date

class ReportRequest(BaseModel):
    employee_id: int
    start_date: date
    end_date: date
    report_type: str = "attendance"  # "attendance" or "leave"
    format: str = "pdf"  # "pdf" or "excel"
    include_summary: bool = True
    include_details: bool = True

class ReportStatus(BaseModel):
    task_id: str
    status: str  # "processing", "completed", "failed"
    message: Optional[str] = None
    download_url: Optional[str] = None
    created_at: date
    completed_at: Optional[date] = None

class ReportMetadata(BaseModel):
    report_id: str
    employee_id: int
    report_type: str
    start_date: date
    end_date: date
    generated_at: date
    file_path: str
    file_size: int
    format: str
