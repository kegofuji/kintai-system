"""
レポート生成用データモデル
"""

from pydantic import BaseModel, Field, validator
from datetime import datetime
from typing import Optional
import re

class ReportRequest(BaseModel):
    """
    PDFレポート生成要求モデル
    Spring Boot → FastAPI 連携用
    """
    employee_id: int = Field(..., gt=0, description="社員ID")
    year_month: str = Field(..., description="対象年月（YYYY-MM形式）")
    report_type: str = Field(default="monthly", description="レポート種別")
    
    @validator('year_month')
    def validate_year_month(cls, v):
        """年月形式バリデーション（YYYY-MM）"""
        if not re.match(r'^\d{4}-\d{2}, v):
            raise ValueError('年月はYYYY-MM形式で入力してください')
        
        try:
            year, month = map(int, v.split('-'))
            if not (2020 <= year <= 2030):
                raise ValueError('年は2020-2030の範囲で入力してください')
            if not (1 <= month <= 12):
                raise ValueError('月は01-12の範囲で入力してください')
        except ValueError as e:
            raise ValueError(f'年月の値が不正です: {str(e)}')
        
        return v
    
    @validator('report_type')
    def validate_report_type(cls, v):
        """レポート種別バリデーション"""
        allowed_types = ['monthly', 'daily', 'custom']
        if v not in allowed_types:
            raise ValueError(f'レポート種別は{allowed_types}のいずれかを選択してください')
        return v

    class Config:
        schema_extra = {
            "example": {
                "employee_id": 1,
                "year_month": "2025-08",
                "report_type": "monthly"
            }
        }

class ReportResponse(BaseModel):
    """
    PDFレポート生成応答モデル
    FastAPI → Spring Boot 連携用
    """
    success: bool = Field(..., description="処理成功フラグ")
    pdf_url: Optional[str] = Field(None, description="PDF一時URL")
    filename: Optional[str] = Field(None, description="生成されたファイル名")
    expires_at: Optional[str] = Field(None, description="有効期限（ISO形式）")
    message: str = Field(..., description="処理結果メッセージ")
    error_code: Optional[str] = Field(None, description="エラーコード")
    
    class Config:
        schema_extra = {
            "example": {
                "success": True,
                "pdf_url": "/files/attendance_report_1_202508_20250826_123456.pdf",
                "filename": "attendance_report_1_202508_20250826_123456.pdf",
                "expires_at": "2025-08-27T23:59:59",
                "message": "PDFレポートを生成しました"
            }
        }

class AttendanceData(BaseModel):
    """勤怠データモデル（Spring Bootとの連携用）"""
    attendance_date: str = Field(..., description="勤務日")
    clock_in_time: Optional[str] = Field(None, description="出勤時刻")
    clock_out_time: Optional[str] = Field(None, description="退勤時刻")
    late_minutes: int = Field(default=0, description="遅刻時間（分）")
    early_leave_minutes: int = Field(default=0, description="早退時間（分）")
    overtime_minutes: int = Field(default=0, description="残業時間（分）")
    night_shift_minutes: int = Field(default=0, description="深夜時間（分）")
    attendance_status: str = Field(default="normal", description="勤怠ステータス")
    submission_status: str = Field(default="未提出", description="申請状況")
    attendance_fixed_flag: bool = Field(default=False, description="確定フラグ")

class EmployeeInfo(BaseModel):
    """社員情報モデル"""
    employee_id: int = Field(..., description="社員ID")
    employee_name: str = Field(..., description="社員名")
    employee_code: str = Field(..., description="社員コード")

class AttendanceSummary(BaseModel):
    """勤怠集計情報モデル（集計項目）"""
    total_working_minutes: int = Field(default=0, description="実働合計時間（分）")
    total_overtime_minutes: int = Field(default=0, description="残業合計時間（分）")
    total_night_shift_minutes: int = Field(default=0, description="深夜合計時間（分）")
    total_late_minutes: int = Field(default=0, description="遅刻合計時間（分）")
    total_early_leave_minutes: int = Field(default=0, description="早退合計時間（分）")
    paid_leave_days: int = Field(default=0, description="有給取得日数")
    absent_days: int = Field(default=0, description="欠勤日数")

class ReportData(BaseModel):
    """完全なレポートデータモデル"""
    employee: EmployeeInfo = Field(..., description="社員情報")
    period: dict = Field(..., description="対象期間")
    attendance_list: list[AttendanceData] = Field(..., description="勤怠明細")
    summary: AttendanceSummary = Field(..., description="集計情報")

class ErrorResponse(BaseModel):
    """エラーレスポンスモデル"""
    success: bool = Field(default=False, description="処理成功フラグ")
    error_code: str = Field(..., description="エラーコード")
    message: str = Field(..., description="エラーメッセージ")
    details: Optional[dict] = Field(None, description="詳細情報")
    
    class Config:
        schema_extra = {
            "example": {
                "success": False,
                "error_code": "VALIDATION_ERROR",
                "message": "入力パラメータが正しくありません",
                "details": {
                    "field": "year_month",
                    "error": "年月形式が正しくありません"
                }
            }
        }