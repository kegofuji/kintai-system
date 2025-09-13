import pytest
import asyncio
from unittest.mock import Mock, patch, AsyncMock
from datetime import datetime, timedelta
import os
import tempfile
import shutil

from services.pdf_service import PDFReportService
from models.report_models import ReportRequest

class TestPDFReportService:
    
    @pytest.fixture
    def pdf_service(self):
        """PDFReportServiceのインスタンスを作成"""
        with tempfile.TemporaryDirectory() as temp_dir:
            # テスト用の一時ディレクトリを使用
            service = PDFReportService()
            service.pdf_storage_path = os.path.join(temp_dir, "reports")
            service.template_env = Mock()
            os.makedirs(service.pdf_storage_path, exist_ok=True)
            yield service
    
    @pytest.fixture
    def sample_attendance_data(self):
        """テスト用の勤怠データ"""
        return {
            "success": True,
            "data": {
                "employee": {
                    "employeeId": 1,
                    "employeeName": "山田太郎",
                    "employeeCode": "E001"
                },
                "period": {
                    "from": "2025-08-01",
                    "to": "2025-08-31"
                },
                "attendanceList": [
                    {
                        "attendanceDate": "2025-08-01",
                        "clockInTime": "09:05:00",
                        "clockOutTime": "18:10:00",
                        "lateMinutes": 5,
                        "earlyLeaveMinutes": 0,
                        "overtimeMinutes": 10,
                        "nightShiftMinutes": 0,
                        "attendanceStatus": "normal",
                        "submissionStatus": "承認済"
                    },
                    {
                        "attendanceDate": "2025-08-02",
                        "clockInTime": "09:00:00",
                        "clockOutTime": "19:00:00",
                        "lateMinutes": 0,
                        "earlyLeaveMinutes": 0,
                        "overtimeMinutes": 60,
                        "nightShiftMinutes": 0,
                        "attendanceStatus": "normal",
                        "submissionStatus": "未提出"
                    }
                ],
                "summary": {
                    "totalWorkingMinutes": 485,
                    "totalOvertimeMinutes": 70,
                    "totalNightShiftMinutes": 0,
                    "totalLateMinutes": 5,
                    "totalEarlyLeaveMinutes": 0,
                    "paidLeaveDays": 0,
                    "absentDays": 0
                }
            }
        }
    
    @pytest.fixture
    def mock_template(self):
        """モックテンプレート"""
        template = Mock()
        template.render.return_value = "<html><body>Test PDF Content</body></html>"
        return template
    
    @pytest.mark.asyncio
    @patch('services.pdf_service.requests.get')
    @patch('services.pdf_service.HTML')
    async def test_generate_monthly_report_success(self, mock_html, mock_requests, 
                                                 pdf_service, sample_attendance_data, mock_template):
        """月次レポート生成 - 正常ケース"""
        # モック設定
        mock_response = Mock()
        mock_response.json.return_value = sample_attendance_data
        mock_response.raise_for_status.return_value = None
        mock_requests.return_value = mock_response
        
        mock_html_instance = Mock()
        mock_html.return_value = mock_html_instance
        mock_html_instance.write_pdf.return_value = None
        
        # テンプレートモック設定
        pdf_service.template_env.get_template.return_value = mock_template
        
        # テスト実行
        result = await pdf_service.generate_monthly_report(1, "2025-08")
        
        # 検証
        assert result["success"] is True
        assert "report_url" in result
        assert "expires" in result
        assert result["report_url"].startswith("http://localhost:8081/reports/download/")
        
        # モック呼び出し確認
        mock_requests.assert_called_once()
        mock_html.assert_called_once()
        mock_template.render.assert_called_once()
        mock_html_instance.write_pdf.assert_called_once()
    
    @pytest.mark.asyncio
    @patch('services.pdf_service.requests.get')
    async def test_generate_monthly_report_api_error(self, mock_requests, pdf_service):
        """月次レポート生成 - API エラー"""
        # API エラーのモック
        mock_requests.side_effect = Exception("API Error")
        
        # テスト実行・検証
        with pytest.raises(Exception) as exc_info:
            await pdf_service.generate_monthly_report(1, "2025-08")
        
        assert "PDF生成に失敗しました" in str(exc_info.value)
    
    @pytest.mark.asyncio
    @patch('services.pdf_service.requests.get')
    async def test_generate_monthly_report_api_response_error(self, mock_requests, pdf_service):
        """月次レポート生成 - API レスポンスエラー"""
        # API エラーレスポンスのモック
        mock_response = Mock()
        mock_response.json.return_value = {
            "success": False,
            "message": "Employee not found"
        }
        mock_response.raise_for_status.return_value = None
        mock_requests.return_value = mock_response
        
        # テスト実行・検証
        with pytest.raises(Exception) as exc_info:
            await pdf_service.generate_monthly_report(1, "2025-08")
        
        assert "Spring Boot APIエラー" in str(exc_info.value)
    
    @pytest.mark.asyncio
    @patch('services.pdf_service.requests.get')
    @patch('services.pdf_service.HTML')
    async def test_generate_monthly_report_pdf_generation_error(self, mock_html, mock_requests, 
                                                               pdf_service, sample_attendance_data, mock_template):
        """月次レポート生成 - PDF生成エラー"""
        # モック設定
        mock_response = Mock()
        mock_response.json.return_value = sample_attendance_data
        mock_response.raise_for_status.return_value = None
        mock_requests.return_value = mock_response
        
        # PDF生成エラーのモック
        mock_html.side_effect = Exception("PDF generation failed")
        
        # テンプレートモック設定
        pdf_service.template_env.get_template.return_value = mock_template
        
        # テスト実行・検証
        with pytest.raises(Exception) as exc_info:
            await pdf_service.generate_monthly_report(1, "2025-08")
        
        assert "PDF生成に失敗しました" in str(exc_info.value)
    
    def test_minutes_to_hhmm_conversion(self, pdf_service):
        """分→時分変換テスト"""
        # 正常ケース
        assert pdf_service._minutes_to_hhmm(0) == "00:00"
        assert pdf_service._minutes_to_hhmm(60) == "01:00"
        assert pdf_service._minutes_to_hhmm(65) == "01:05"
        assert pdf_service._minutes_to_hhmm(480) == "08:00"
        assert pdf_service._minutes_to_hhmm(1440) == "24:00"
        
        # 境界値テスト
        assert pdf_service._minutes_to_hhmm(1) == "00:01"
        assert pdf_service._minutes_to_hhmm(59) == "00:59"
        assert pdf_service._minutes_to_hhmm(61) == "01:01"
    
    def test_format_attendance_data(self, pdf_service, sample_attendance_data):
        """勤怠データフォーマットテスト"""
        formatted_data = pdf_service._format_attendance_data(sample_attendance_data["data"])
        
        # 社員情報確認
        assert formatted_data["employee"]["employeeId"] == 1
        assert formatted_data["employee"]["employeeName"] == "山田太郎"
        assert formatted_data["employee"]["employeeCode"] == "E001"
        
        # 期間情報確認
        assert formatted_data["period"]["from"] == "2025-08-01"
        assert formatted_data["period"]["to"] == "2025-08-31"
        
        # 勤怠記録フォーマット確認
        attendance_list = formatted_data["attendanceList"]
        assert len(attendance_list) == 2
        
        # 1件目の記録確認
        record1 = attendance_list[0]
        assert record1["late_time_formatted"] == "00:05"
        assert record1["early_leave_time_formatted"] == "00:00"
        assert record1["overtime_time_formatted"] == "00:10"
        assert record1["night_shift_time_formatted"] == "00:00"
        assert record1["attendance_status_jp"] == "出勤"
        assert record1["submission_status_jp"] == "承認済"
        
        # 2件目の記録確認
        record2 = attendance_list[1]
        assert record2["late_time_formatted"] == "00:00"
        assert record2["early_leave_time_formatted"] == "00:00"
        assert record2["overtime_time_formatted"] == "01:00"
        assert record2["night_shift_time_formatted"] == "00:00"
        assert record2["attendance_status_jp"] == "出勤"
        assert record2["submission_status_jp"] == "未提出"
        
        # 集計データフォーマット確認
        summary = formatted_data["summary"]
        assert summary["total_working_formatted"] == "08:05"
        assert summary["total_overtime_formatted"] == "01:10"
        assert summary["total_night_shift_formatted"] == "00:00"
        assert summary["total_late_formatted"] == "00:05"
        assert summary["total_early_leave_formatted"] == "00:00"
    
    def test_get_pdf_css(self, pdf_service):
        """PDF用CSSスタイルテスト"""
        css = pdf_service._get_pdf_css()
        
        # CSSの主要要素が含まれているか確認
        assert "@page" in css
        assert "body" in css
        assert ".header" in css
        assert "table" in css
        assert "th, td" in css
        assert ".summary-table" in css
        assert ".footer" in css
    
    @pytest.mark.asyncio
    async def test_schedule_file_deletion(self, pdf_service):
        """ファイル削除スケジュールテスト"""
        # テスト用ファイル作成
        test_file_path = os.path.join(pdf_service.pdf_storage_path, "test_file.txt")
        with open(test_file_path, "w") as f:
            f.write("test content")
        
        # ファイルが存在することを確認
        assert os.path.exists(test_file_path)
        
        # 即座に削除スケジュール（0時間後）
        await pdf_service._schedule_file_deletion(test_file_path, 0)
        
        # ファイルが削除されたことを確認
        assert not os.path.exists(test_file_path)
    
    @pytest.mark.asyncio
    @patch('services.pdf_service.requests.get')
    @patch('services.pdf_service.HTML')
    async def test_generate_monthly_report_with_complex_data(self, mock_html, mock_requests, 
                                                           pdf_service, mock_template):
        """複雑な勤怠データでの月次レポート生成テスト"""
        # 複雑な勤怠データ
        complex_data = {
            "success": True,
            "data": {
                "employee": {
                    "employeeId": 2,
                    "employeeName": "佐藤花子",
                    "employeeCode": "E002"
                },
                "period": {
                    "from": "2025-08-01",
                    "to": "2025-08-31"
                },
                "attendanceList": [
                    {
                        "attendanceDate": "2025-08-01",
                        "clockInTime": "08:30:00",
                        "clockOutTime": "22:30:00",
                        "lateMinutes": 0,
                        "earlyLeaveMinutes": 0,
                        "overtimeMinutes": 300,  # 5時間残業
                        "nightShiftMinutes": 30,  # 30分深夜勤務
                        "attendanceStatus": "normal",
                        "submissionStatus": "申請済"
                    },
                    {
                        "attendanceDate": "2025-08-02",
                        "clockInTime": None,
                        "clockOutTime": None,
                        "lateMinutes": 0,
                        "earlyLeaveMinutes": 0,
                        "overtimeMinutes": 0,
                        "nightShiftMinutes": 0,
                        "attendanceStatus": "paid_leave",
                        "submissionStatus": "承認"
                    }
                ],
                "summary": {
                    "totalWorkingMinutes": 840,  # 14時間
                    "totalOvertimeMinutes": 300,  # 5時間
                    "totalNightShiftMinutes": 30,  # 30分
                    "totalLateMinutes": 0,
                    "totalEarlyLeaveMinutes": 0,
                    "paidLeaveDays": 1,
                    "absentDays": 0
                }
            }
        }
        
        # モック設定
        mock_response = Mock()
        mock_response.json.return_value = complex_data
        mock_response.raise_for_status.return_value = None
        mock_requests.return_value = mock_response
        
        mock_html_instance = Mock()
        mock_html.return_value = mock_html_instance
        mock_html_instance.write_pdf.return_value = None
        
        # テンプレートモック設定
        pdf_service.template_env.get_template.return_value = mock_template
        
        # テスト実行
        result = await pdf_service.generate_monthly_report(2, "2025-08")
        
        # 検証
        assert result["success"] is True
        assert "report_url" in result
        assert "expires" in result
        
        # フォーマットされたデータの確認
        formatted_data = pdf_service._format_attendance_data(complex_data["data"])
        
        # 有給のステータス確認
        paid_leave_record = formatted_data["attendanceList"][1]
        assert paid_leave_record["attendance_status_jp"] == "有給"
        assert paid_leave_record["submission_status_jp"] == "確定済"
        
        # 残業・深夜勤務の時間確認
        overtime_record = formatted_data["attendanceList"][0]
        assert overtime_record["overtime_time_formatted"] == "05:00"
        assert overtime_record["night_shift_time_formatted"] == "00:30"
        
        # 集計データ確認
        summary = formatted_data["summary"]
        assert summary["total_working_formatted"] == "14:00"
        assert summary["total_overtime_formatted"] == "05:00"
        assert summary["total_night_shift_formatted"] == "00:30"
