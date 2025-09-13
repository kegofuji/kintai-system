# 勤怠管理システム (Kintai Management System)

## システム概要

SES営業向けのポートフォリオとして構築する勤怠管理システムです。従業員の出退勤管理、有給申請、承認ワークフロー、レポート生成機能を提供します。

## 技術スタック

### バックエンド
- **Java Spring Boot**: 認証・承認ワークフロー、メインAPI
- **Python FastAPI**: PDFレポート生成、データ分析
- **MySQL**: データベース

### フロントエンド
- **Vanilla JavaScript SPA**: シングルページアプリケーション
- **HTML5/CSS3**: レスポンシブデザイン

### インフラ・デプロイ
- **Railway**: クラウドデプロイメント
- **Docker**: コンテナ化

## 想定ユーザー

| ユーザータイプ | 権限 | 説明 |
|---|---|---|
| **一般社員** | 基本操作 | 出退勤打刻、勤怠履歴確認、有給申請 |
| **管理者** | 全操作 | 全社員の勤怠管理、承認処理、レポート確認 |
| **退職者** | ログイン不可 | システムへのアクセス権限なし |

## 主要機能

### 1. 出退勤管理
- 出勤・退勤の打刻機能
- リアルタイム勤怠状況表示
- 打刻履歴の確認

### 2. 勤怠履歴
- 日別・月別勤怠履歴の確認
- 勤務時間の自動計算
- 残業時間の集計

### 3. 有給申請
- 有給休暇の申請機能
- 申請状況の確認
- 残日数の表示

### 4. 月末申請
- 月次勤怠の申請・承認
- 申請書の自動生成
- 承認フローの管理

### 5. 承認ワークフロー
- 多段階承認システム
- 承認通知機能
- 承認履歴の管理

### 6. PDFレポート出力
- 勤怠レポートの自動生成
- カスタマイズ可能なレポート形式
- 一括ダウンロード機能

## 勤務時間設定

### 所定勤務時間
- **勤務時間**: 9:00 ～ 18:00
- **休憩時間**: 12:00 ～ 13:00（自動休憩）
- **実働時間**: 8時間

### 深夜勤務
- **深夜時間帯**: 22:00 ～ 翌5:00
- **深夜手当**: 自動計算・適用

## プロジェクト構成

```
kintai-system/
├─ backend/                    # バックエンド関連
│  ├─ java-springboot/            # Java Spring Boot アプリケーション
│  │  ├─ src/                     # ソースコード
│  │  ├─ pom.xml                  # Maven設定
│  │  └─ README.md                # セットアップ手順
│  └─ python-fastapi/             # Python FastAPI アプリケーション
│     ├─ app/                     # アプリケーションコード
│     ├─ requirements.txt         # Python依存関係
│     └─ README.md                # セットアップ手順
├─ frontend/                   # フロントエンド関連
│  └─ public_ui/                   # パブリックUI
│     ├─ index.html                # メインページ
│     ├─ css/                      # スタイルシート
│     ├─ js/                       # JavaScript
│     └─ README.md                 # セットアップ手順
├─ infra/                      # インフラ関連
│  ├─ Dockerfile                  # Docker設定
│  ├─ railway.json                # Railway設定
│  └─ README.md                   # インフラドキュメント
├─ docs/                       # ドキュメント
│  └─ requirements/                # 要件定義書
│     ├─ functional/               # 機能要件
│     ├─ non-functional/           # 非機能要件
│     └─ user-stories/             # ユーザーストーリー
├─ .gitignore                  # Git除外設定
└─ README.md                   # このファイル
```

## 開発環境のセットアップ

### 前提条件
- Java 17以上
- Python 3.9以上
- Node.js 16以上
- MySQL 8.0以上
- Docker（オプション）

### セットアップ手順

1. **リポジトリのクローン**
   ```bash
   git clone <repository-url>
   cd kintai-system
   ```

2. **バックエンド（Java Spring Boot）のセットアップ**
   ```bash
   cd backend/java-springboot
   ./mvnw spring-boot:run
   ```

3. **バックエンド（Python FastAPI）のセットアップ**
   ```bash
   cd backend/python-fastapi
   pip install -r requirements.txt
   uvicorn app.main:app --reload
   ```

4. **フロントエンドのセットアップ**
   ```bash
   cd frontend/public_ui
   # ブラウザでindex.htmlを開く
   ```

詳細なセットアップ手順は、各ディレクトリのREADMEファイルを参照してください。

## デプロイ

Railwayを使用してクラウドデプロイを行います。詳細は`infra/README.md`を参照してください。

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。

## 貢献

プルリクエストやイシューの報告を歓迎します。詳細は各コンポーネントのドキュメントを参照してください。
