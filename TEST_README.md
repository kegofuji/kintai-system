# 勤怠管理システム テスト仕様書

## 概要

設計書仕様に基づいて作成された包括的なテストスイートです。Spring Boot（Java）とFastAPI（Python）の両方のバックエンドAPIをテストします。

## テスト構成

### Spring Boot テスト

#### 1. 単体テスト（Unit Tests）

**ファイル**: `backend/java-springboot/src/test/java/com/kintai/service/TimeCalculatorTest.java`

**テスト対象**: `TimeCalculator` クラス
- 遅刻時間計算
- 早退時間計算
- 実働時間計算（昼休憩控除）
- 残業時間計算
- 深夜勤務時間計算
- 勤怠時間統合計算

**テストケース数**: 20ケース

#### 2. 統合テスト（Integration Tests）

**ファイル**: `backend/java-springboot/src/test/java/com/kintai/controller/AttendanceControllerIntegrationTest.java`

**テスト対象**: `AttendanceController` API
- 出勤打刻（正常・重複エラー・存在しない社員）
- 退勤打刻（正常・出勤記録なし・既に退勤済み）
- 勤怠履歴取得（正常・存在しない社員）
- 月末勤怠申請（正常・存在しない社員）

**テストケース数**: 10ケース

#### 3. テスト設定

**データベース設定**: `backend/java-springboot/src/test/resources/application-test.yml`
- H2インメモリデータベース
- テスト用JWT設定
- ログレベル設定

**テストデータ**: `backend/java-springboot/src/test/resources/test-data.sql`
- 社員データ（3件）
- 勤怠記録データ（5件）
- 休暇申請データ（3件）
- 勤怠修正申請データ（3件）

### FastAPI テスト

#### 1. PDFサービステスト

**ファイル**: `backend/python-fastapi/tests/test_pdf_service.py`

**テスト対象**: `PDFReportService` クラス
- 月次レポート生成（正常・APIエラー・PDF生成エラー）
- 分→時分変換
- 勤怠データフォーマット
- 複雑な勤怠データでのレポート生成

**テストケース数**: 8ケース

#### 2. テスト設定

**pytest設定**: `backend/python-fastapi/pytest.ini`
- 非同期テスト対応
- マーカー設定
- 出力設定

**テスト共通設定**: `backend/python-fastapi/tests/conftest.py`
- 環境変数設定
- イベントループ設定
- モック設定

## テスト実行方法

### Spring Boot テスト実行

```bash
cd backend/java-springboot
./run-tests.sh
```

または

```bash
cd backend/java-springboot
mvn test -Dspring.profiles.active=test
```

### FastAPI テスト実行

```bash
cd backend/python-fastapi
./run-tests.sh
```

または

```bash
cd backend/python-fastapi
source venv/bin/activate
pip install -r requirements-test.txt
pytest tests/ -v
```

## テストカバレッジ

### Spring Boot
- **単体テスト**: TimeCalculatorクラスの全メソッド
- **統合テスト**: AttendanceControllerの全エンドポイント
- **データベース**: H2インメモリDBでのCRUD操作

### FastAPI
- **PDFサービス**: 全メソッドの正常・異常ケース
- **データフォーマット**: 時間計算・表示変換
- **外部API連携**: Spring Boot API呼び出し

## 設計書準拠事項

### 時間計算ロジック
- 定時: 09:00-18:00（8時間勤務）
- 昼休憩: 12:00-13:00（60分自動控除）
- 深夜勤務: 22:00-翌05:00
- 遅刻・早退・残業時間の正確な計算

### API仕様
- RESTful API設計
- 統一エラーレスポンス形式
- JWT認証対応
- バリデーション機能

### データベース設計
- 正規化されたテーブル設計
- 外部キー制約
- インデックス最適化
- 列挙型の適切な使用

## 品質保証

### テスト品質
- **網羅性**: 正常・異常・境界値テスト
- **独立性**: 各テストケースの独立性確保
- **再現性**: テストデータの一貫性
- **保守性**: 明確なテスト名とコメント

### パフォーマンス
- **実行速度**: 単体テスト < 1秒、統合テスト < 10秒
- **メモリ使用量**: テスト用H2DBの効率的な使用
- **並列実行**: テストケースの並列実行対応

## 継続的インテグレーション

### 自動テスト実行
- コードコミット時の自動実行
- プルリクエスト時の検証
- デプロイ前の最終確認

### テストレポート
- テスト結果の詳細レポート
- カバレッジレポート生成
- 失敗テストの詳細分析

## 今後の拡張

### 追加予定テスト
- パフォーマンステスト
- セキュリティテスト
- エンドツーエンドテスト
- 負荷テスト

### テスト改善
- テストデータの動的生成
- モックの高度化
- テスト実行時間の最適化
- カバレッジの向上

---

**作成日**: 2025年1月
**バージョン**: 1.0.0
**対象システム**: 勤怠管理システム v1.0
