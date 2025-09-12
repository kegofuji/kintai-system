"""
PDF生成サービス
勤怠レポートPDF生成専用サービス
"""

import os
import logging
from datetime import datetime
from typing import Optional, Dict, Any
from jinja2 import Template
import requests
import asyncio
import aiofiles
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
import json

logger = logging.getLogger(__name__)

class PDFReportService:
    """PDF生成サービス"""
    
    def __init__(self):
        self.spring_boot_url = os.getenv("SPRING_BOOT_URL", "http://localhost:8080")
        self.output_dir = "generated_pdfs"
        self._setup_fonts()
    
    def _setup_fonts(self):
        """日本語フォントセットアップ"""
        try:
            # 日本語フォント登録（システムにある場合）
            font_paths = [
                "/System/Library/Fonts/Hiragino Sans GB.ttc",  # macOS
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",  # Linux
                "C:\\Windows\\Fonts\\msgothic.ttc",  # Windows
            ]
            
            for font_path in font_paths:
                if os.path.exists(font_path):
                    try:
                        pdfmetrics.registerFont(TTFont('Japanese', font_path))
                        logger.info(f"日本語フォント登録成功: {font_path}")
                        return
                    except Exception:
                        continue
            
            logger.warning("日本語フォントが見つかりません。デフォルトフォントを使用します。")
            
        except Exception as e:
            logger.error(f"フォントセットアップエラー: {str(e)}")
    
    async def generate_monthly_report(self, employee_id: int, year_month: str) -> Optional[str]:
        """
        月次勤怠レポート生成
        """
        try:
            # Spring Bootから勤怠データ取得
            attendance_data = await self._fetch_attendance_data(employee_id, year_month)
            
            if not attendance_data:
                logger.error("勤怠データの取得に失敗しました")
                return None
            
            # PDFファイル名生成
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"attendance_report_{employee_id}_{year_month.replace('-', '')}_{timestamp}.pdf"
            file_path = os.path.join(self.output_dir, filename)
            
            # PDF生成
            success = await self._create_pdf_report(file_path, attendance_data, year_month)
            
            if success:
                logger.info(f"PDFレポート生成完了: {filename}")
                return filename
            else:
                logger.error("PDFレポート生成に失敗しました")
                return None
                
        except Exception as e:
            logger.error(f"月次レポート生成エラー: {str(e)}")
            return None
    
    async def _fetch_attendance_data(self, employee_id: int, year_month: str) -> Optional[Dict[Any, Any]]:
        """
        Spring Bootから勤怠データ取得
        """
        try:
            url = f"{self.spring_boot_url}/api/attendance/history"
            params = {
                "employeeId": employee_id,
                "yearMonth": year_month
            }
            
            # 非同期HTTP要求
            response = requests.get(url, params=params, timeout=30)
            
            if response.status_code == 200:
                data = response.json()
                if data.get("success"):
                    return data.get("data")
                else:
                    logger.error(f"勤怠データ取得失敗: {data.get('message')}")
                    return None
            else:
                logger.error(f"HTTP エラー: {response.status_code}")
                return None
                
        except Exception as e:
            logger.error(f"勤怠データ取得エラー: {str(e)}")
            # 開発・テスト用のモックデータ
            return self._get_mock_data(employee_id, year_month)
    
    def _get_mock_data(self, employee_id: int, year_month: str) -> Dict[Any, Any]:
        """
        開発・テスト用モックデータ（設計書通りの構造）
        """
        return {
            "employee": {
                "employeeId": employee_id,
                "employeeName": "山田太郎",
                "employeeCode": f"E{employee_id:03d}"
            },
            "period": {
                "from": f"{year_month}-01",
                "to": f"{year_month}-31"
            },
            "attendanceList": [
                {
                    "attendanceDate": f"{year_month}-01",
                    "clockInTime": "09:05:00",
                    "clockOutTime": "18:10:00",
                    "lateMinutes": 5,
                    "earlyLeaveMinutes": 0,
                    "overtimeMinutes": 10,
                    "nightShiftMinutes": 0,
                    "attendanceStatus": "normal",
                    "submissionStatus": "承認済",
                    "attendanceFixedFlag": True
                },
                {
                    "attendanceDate": f"{year_month}-02",
                    "clockInTime": "09:00:00",
                    "clockOutTime": "18:00:00",
                    "lateMinutes": 0,
                    "earlyLeaveMinutes": 0,
                    "overtimeMinutes": 0,
                    "nightShiftMinutes": 0,
                    "attendanceStatus": "normal",
                    "submissionStatus": "承認済",
                    "attendanceFixedFlag": True
                }
            ],
            "summary": {
                "totalWorkingMinutes": 960,
                "totalOvertimeMinutes": 10,
                "totalNightShiftMinutes": 0,
                "totalLateMinutes": 5,
                "totalEarlyLeaveMinutes": 0,
                "paidLeaveDays": 1,
                "absentDays": 0
            }
        }
    
    async def _create_pdf_report(self, file_path: str, data: Dict[Any, Any], year_month: str) -> bool:
        """
        PDF勤怠レポート作成（出力項目）
        """
        try:
            # ドキュメント作成
            doc = SimpleDocTemplate(
                file_path,
                pagesize=A4,
                rightMargin=20*mm,
                leftMargin=20*mm,
                topMargin=20*mm,
                bottomMargin=20*mm
            )
            
            # スタイル設定
            styles = getSampleStyleSheet()
            
            # 日本語対応スタイル
            japanese_style = ParagraphStyle(
                'Japanese',
                parent=styles['Normal'],
                fontName='Japanese' if 'Japanese' in pdfmetrics.getRegisteredFontNames() else 'Helvetica',
                fontSize=10,
                leading=12
            )
            
            title_style = ParagraphStyle(
                'JapaneseTitle',
                parent=japanese_style,
                fontSize=16,
                leading=20,
                alignment=1  # センタリング
            )
            
            # レポート要素リスト
            elements = []
            
            # タイトル
            elements.append(Paragraph("勤怠管理システム 月次レポート", title_style))
            elements.append(Spacer(1, 20))
            
            # 基本情報
            employee = data.get("employee", {})
            period = data.get("period", {})
            
            info_data = [
                ["対象年月", year_month],
                ["社員名", employee.get("employeeName", "")],
                ["社員コード", employee.get("employeeCode", "")],
                ["期間", f"{period.get('from', '')} ～ {period.get('to', '')}"],
                ["出力日時", datetime.now().strftime("%Y-%m-%d %H:%M:%S")]
            ]
            
            info_table = Table(info_data, colWidths=[40*mm, 80*mm])
            info_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (0, -1), colors.lightgrey),
                ('TEXTCOLOR', (0, 0), (-1, -1), colors.black),
                ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
                ('FONTNAME', (0, 0), (-1, -1), 'Helvetica'),
                ('FONTSIZE', (0, 0), (-1, -1), 10),
                ('BOTTOMPADDING', (0, 0), (-1, -1), 12),
                ('BACKGROUND', (1, 0), (1, -1), colors.white),
                ('GRID', (0, 0), (-1, -1), 1, colors.black)
            ]))
            
            elements.append(info_table)
            elements.append(Spacer(1, 20))
            
            # 日次明細テーブル
            elements.append(Paragraph("日次勤怠明細", japanese_style))
            elements.append(Spacer(1, 10))
            
            # テーブルヘッダー（項目）
            attendance_data = [
                ["日付", "出勤", "退勤", "遅刻", "早退", "残業", "深夜", "ステータス"]
            ]
            
            # 勤怠データ追加
            attendance_list = data.get("attendanceList", [])
            for record in attendance_list:
                row = [
                    record.get("attendanceDate", ""),
                    record.get("clockInTime", ""),
                    record.get("clockOutTime", ""),
                    self._format_minutes_to_hhmm(record.get("lateMinutes", 0)),
                    self._format_minutes_to_hhmm(record.get("earlyLeaveMinutes", 0)),
                    self._format_minutes_to_hhmm(record.get("overtimeMinutes", 0)),
                    self._format_minutes_to_hhmm(record.get("nightShiftMinutes", 0)),
                    "確定" if record.get("attendanceFixedFlag") else record.get("submissionStatus", "")
                ]
                attendance_data.append(row)
            
            attendance_table = Table(attendance_data, colWidths=[
                25*mm, 20*mm, 20*mm, 15*mm, 15*mm, 15*mm, 15*mm, 25*mm
            ])
            
            attendance_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.grey),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                ('FONTNAME', (0, 0), (-1, -1), 'Helvetica'),
                ('FONTSIZE', (0, 0), (-1, -1), 8),
                ('BOTTOMPADDING', (0, 0), (-1, -1), 8),
                ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
                ('GRID', (0, 0), (-1, -1), 1, colors.black)
            ]))
            
            elements.append(attendance_table)
            elements.append(Spacer(1, 20))
            
            # 集計情報（集計項目）
            elements.append(Paragraph("月次集計", japanese_style))
            elements.append(Spacer(1, 10))
            
            summary = data.get("summary", {})
            summary_data = [
                ["実働合計", self._format_minutes_to_hhmm(summary.get("totalWorkingMinutes", 0))],
                ["残業合計", self._format_minutes_to_hhmm(summary.get("totalOvertimeMinutes", 0))],
                ["深夜合計", self._format_minutes_to_hhmm(summary.get("totalNightShiftMinutes", 0))],
                ["遅刻回数/時間", f"{self._count_late_days(attendance_list)}回 / {self._format_minutes_to_hhmm(summary.get('totalLateMinutes', 0))}"],
                ["早退回数/時間", f"{self._count_early_leave_days(attendance_list)}回 / {self._format_minutes_to_hhmm(summary.get('totalEarlyLeaveMinutes', 0))}"],
                ["有給取得日数", f"{summary.get('paidLeaveDays', 0)}日"],
                ["欠勤日数", f"{summary.get('absentDays', 0)}日"]
            ]
            
            summary_table = Table(summary_data, colWidths=[50*mm, 50*mm])
            summary_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (0, -1), colors.lightgrey),
                ('TEXTCOLOR', (0, 0), (-1, -1), colors.black),
                ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
                ('FONTNAME', (0, 0), (-1, -1), 'Helvetica'),
                ('FONTSIZE', (0, 0), (-1, -1), 10),
                ('BOTTOMPADDING', (0, 0), (-1, -1), 12),
                ('BACKGROUND', (1, 0), (1, -1), colors.white),
                ('GRID', (0, 0), (-1, -1), 1, colors.black)
            ]))
            
            elements.append(summary_table)
            
            # PDF生成
            doc.build(elements)
            return True
            
        except Exception as e:
            logger.error(f"PDF作成エラー: {str(e)}")
            return False
    
    def _format_minutes_to_hhmm(self, minutes: int) -> str:
        """分を時間:分の文字列に変換（設計書通りのhh:mm表示）"""
        if not minutes:
            return "00:00"
        hours = minutes // 60
        mins = minutes % 60
        return f"{hours:02d}:{mins:02d}"
    
    def _count_late_days(self, attendance_list: list) -> int:
        """遅刻日数カウント"""
        return len([r for r in attendance_list if r.get("lateMinutes", 0) > 0])
    
    def _count_early_leave_days(self, attendance_list: list) -> int:
        """早退日数カウント"""
        return len([r for r in attendance_list if r.get("earlyLeaveMinutes", 0) > 0])