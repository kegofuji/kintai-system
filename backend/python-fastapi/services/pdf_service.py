import requests
import os
import uuid
from datetime import datetime, timedelta
from jinja2 import Environment, FileSystemLoader
from weasyprint import HTML, CSS
from typing import Optional
import asyncio
import logging

logger = logging.getLogger(__name__)

class PDFReportService:
    
    def __init__(self):
        self.spring_boot_base_url = os.getenv("SPRING_BOOT_URL", "http://localhost:8080")
        self.pdf_storage_path = "static/reports"
        self.template_env = Environment(loader=FileSystemLoader("templates"))
        
        # PDFディレクトリ作成
        os.makedirs(self.pdf_storage_path, exist_ok=True)
    
    async def generate_monthly_report(self, employee_id: int, year_month: str) -> dict:
        """
        月次PDFレポート生成（設計書の連携フロー準拠）
        1. Spring Boot APIから勤怠データ取得
        2. HTMLテンプレートにデータ挿入  
        3. WeasyPrintでPDF変換
        4. 一時ファイル保存（24時間後削除）
        """
        try:
            # 1. Spring Boot APIから勤怠データ取得
            attendance_data = await self._fetch_attendance_data(employee_id, year_month)
            
            # 2. HTMLテンプレートレンダリング
            html_content = self._render_template(attendance_data)
            
            # 3. PDF生成
            pdf_filename = f"report_{datetime.now().strftime('%Y%m%d_%H%M%S')}_{uuid.uuid4().hex[:8]}.pdf"
            pdf_path = os.path.join(self.pdf_storage_path, pdf_filename)
            
            HTML(string=html_content).write_pdf(
                pdf_path,
                stylesheets=[CSS(string=self._get_pdf_css())]
            )
            
            # 4. 24時間後削除スケジュール設定
            asyncio.create_task(self._schedule_file_deletion(pdf_path, hours=24))
            
            # 5. レスポンス生成
            expires = datetime.now() + timedelta(hours=24)
            
            return {
                "success": True,
                "report_url": f"http://localhost:8081/reports/download/{pdf_filename}",
                "expires": expires.isoformat()
            }
            
        except Exception as e:
            logger.error(f"PDF生成エラー: {str(e)}")
            raise Exception(f"PDF生成に失敗しました: {str(e)}")
    
    async def _fetch_attendance_data(self, employee_id: int, year_month: str) -> dict:
        """Spring Boot APIから勤怠データ取得"""
        url = f"{self.spring_boot_base_url}/api/attendance/history"
        params = {
            "employeeId": employee_id,
            "yearMonth": year_month
        }
        
        try:
            response = requests.get(url, params=params, timeout=30)
            response.raise_for_status()
            
            api_response = response.json()
            if not api_response.get("success", False):
                raise Exception(f"Spring Boot APIエラー: {api_response.get('message', 'Unknown error')}")
            
            return api_response["data"]
            
        except requests.RequestException as e:
            logger.error(f"Spring Boot API呼び出しエラー: {str(e)}")
            raise Exception(f"勤怠データの取得に失敗しました: {str(e)}")
    
    def _render_template(self, data: dict) -> str:
        """HTMLテンプレートレンダリング"""
        template = self.template_env.get_template("report_template.html")
        
        # 時間表示フォーマット変換（分 → HH:MM）
        formatted_data = self._format_attendance_data(data)
        
        return template.render(
            employee=formatted_data["employee"],
            period=formatted_data["period"],
            attendance_list=formatted_data["attendanceList"],
            summary=formatted_data["summary"],
            generated_at=datetime.now().strftime("%Y年%m月%d日 %H:%M")
        )
    
    def _format_attendance_data(self, data: dict) -> dict:
        """勤怠データフォーマット（分→時分変換）"""
        formatted_list = []
        
        for record in data["attendanceList"]:
            formatted_record = record.copy()
            
            # 分を HH:MM 形式に変換
            formatted_record["late_time_formatted"] = self._minutes_to_hhmm(record["lateMinutes"])
            formatted_record["early_leave_time_formatted"] = self._minutes_to_hhmm(record["earlyLeaveMinutes"])
            formatted_record["overtime_time_formatted"] = self._minutes_to_hhmm(record["overtimeMinutes"])
            formatted_record["night_shift_time_formatted"] = self._minutes_to_hhmm(record["nightShiftMinutes"])
            
            # ステータス日本語化
            status_map = {
                "normal": "出勤",
                "paid_leave": "有給",
                "absent": "欠勤"
            }
            formatted_record["attendance_status_jp"] = status_map.get(record["attendanceStatus"], record["attendanceStatus"])
            
            submission_map = {
                "未提出": "未提出",
                "申請済": "申請済",
                "承認": "確定済",
                "却下": "却下"
            }
            formatted_record["submission_status_jp"] = submission_map.get(record["submissionStatus"], record["submissionStatus"])
            
            formatted_list.append(formatted_record)
        
        # 集計データフォーマット
        summary = data["summary"].copy()
        summary["total_working_formatted"] = self._minutes_to_hhmm(summary["totalWorkingMinutes"])
        summary["total_overtime_formatted"] = self._minutes_to_hhmm(summary["totalOvertimeMinutes"])
        summary["total_night_shift_formatted"] = self._minutes_to_hhmm(summary["totalNightShiftMinutes"])
        summary["total_late_formatted"] = self._minutes_to_hhmm(summary["totalLateMinutes"])
        summary["total_early_leave_formatted"] = self._minutes_to_hhmm(summary["totalEarlyLeaveMinutes"])
        
        # 期間データのフォーマット（from/to形式）
        period_data = {
            "from": data["period"]["from"],
            "to": data["period"]["to"]
        }
        
        return {
            "employee": data["employee"],
            "period": period_data,
            "attendanceList": formatted_list,
            "summary": summary
        }
    
    def _minutes_to_hhmm(self, minutes: int) -> str:
        """分をHH:MM形式に変換"""
        if minutes == 0:
            return "00:00"
        hours = minutes // 60
        mins = minutes % 60
        return f"{hours:02d}:{mins:02d}"
    
    def _get_pdf_css(self) -> str:
        """PDF用CSSスタイル"""
        return """
        @page {
            margin: 20mm;
            size: A4;
        }
        
        body {
            font-family: 'DejaVu Sans', sans-serif;
            font-size: 12px;
            line-height: 1.4;
            color: #333;
        }
        
        .header {
            text-align: center;
            margin-bottom: 20px;
            border-bottom: 2px solid #333;
            padding-bottom: 10px;
        }
        
        .header h1 {
            margin: 0;
            font-size: 18px;
            font-weight: bold;
        }
        
        .info-section {
            margin-bottom: 15px;
        }
        
        .info-section .label {
            font-weight: bold;
            display: inline-block;
            width: 80px;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
            font-size: 10px;
        }
        
        th, td {
            border: 1px solid #ddd;
            padding: 5px;
            text-align: center;
        }
        
        th {
            background-color: #f5f5f5;
            font-weight: bold;
        }
        
        .summary-table {
            margin-top: 20px;
        }
        
        .summary-table th {
            background-color: #e8f4f8;
        }
        
        .footer {
            margin-top: 30px;
            text-align: right;
            font-size: 10px;
            color: #666;
        }
        """
    
    async def _schedule_file_deletion(self, file_path: str, hours: int):
        """指定時間後にファイル削除"""
        await asyncio.sleep(hours * 3600)  # hours時間待機
        try:
            if os.path.exists(file_path):
                os.remove(file_path)
                logger.info(f"期限切れPDFファイルを削除しました: {file_path}")
        except Exception as e:
            logger.error(f"PDFファイル削除エラー: {str(e)}")