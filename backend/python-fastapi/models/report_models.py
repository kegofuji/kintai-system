from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime

class ReportRequest(BaseModel):
    employee_id: int = Field(..., description="社員ID")
    year_month: str = Field(..., pattern=r"^\d{4}-\d{2}$", description="対象年月（YYYY-MM形式）")
    report_type: str = Field(default="monthly", description="レポート種別")

class ReportResponse(BaseModel):
    success: bool = True
    report_url: str = Field(..., description="PDFダウンロードURL")
    expires: str = Field(..., description="有効期限（24時間後）")

class EmployeeData(BaseModel):
    employee_id: int
    employee_name: str
    employee_code: str

class AttendanceData(BaseModel):
    attendance_date: str
    clock_in_time: Optional[str]
    clock_out_time: Optional[str]
    late_minutes: int
    early_leave_minutes: int
    overtime_minutes: int
    night_shift_minutes: int
    attendance_status: str
    submission_status: str

class AttendanceSummary(BaseModel):
    total_working_minutes: int
    total_overtime_minutes: int
    total_night_shift_minutes: int
    total_late_minutes: int
    total_early_leave_minutes: int
    paid_leave_days: int
    absent_days: int

class ReportData(BaseModel):
    employee: EmployeeData
    year_month: str
    attendance_list: list[AttendanceData]
    summary: AttendanceSummary
