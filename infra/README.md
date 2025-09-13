# 勤怠管理システム デプロイメント

## Railway デプロイ手順

### 1. 前提条件
- Railway アカウント作成済み
- GitHub リポジトリ作成済み
- Railway CLI インストール済み

### 2. データベースセットアップ
```bash
# Railway プロジェクト作成
railway new kintai-system

# MySQL サービス追加
railway add mysql

# 環境変数確認
railway variables
```

### 3. バックエンド（Spring Boot）デプロイ
```bash
# Java バックエンドサービス作成
railway service create backend-java

# 環境変数設定
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set JWT_SECRET="your-very-secure-jwt-secret-key"

# デプロイ
railway up --service backend-java
```

### 4. バックエンド（FastAPI）デプロイ
```bash
# Python バックエンドサービス作成
railway service create backend-python

# 環境変数設定
railway variables set SPRING_BOOT_URL="https://backend-java.railway.app"

# デプロイ
railway up --service backend-python
```

### 5. フロントエンドデプロイ
```bash
# フロントエンドサービス作成
railway service create frontend

# デプロイ
railway up --service frontend
```

### 6. データベース初期化
```bash
# Railway MySQL に接続
railway connect mysql

# 初期化スクリプト実行
mysql> source /path/to/init.sql;
```

### 7. 環境変数設定完了例
```
# Java Backend Service
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=mysql://user:pass@host:port/db
JWT_SECRET=your-secret-key
PORT=8080

# Python Backend Service  
SPRING_BOOT_URL=https://backend-java-xxx.railway.app
PORT=8081

# Frontend Service
# 環境変数なし（静的ファイル配信のみ）
```

### 8. URL確認
- フロントエンド: https://frontend-xxx.railway.app
- Java API: https://backend-java-xxx.railway.app/api
- Python API: https://backend-python-xxx.railway.app/reports
- ヘルスチェック: https://backend-java-xxx.railway.app/actuator/health

### 9. ログ確認
```bash
# 各サービスのログ確認
railway logs --service backend-java
railway logs --service backend-python
railway logs --service frontend
```

## ローカル開発環境セットアップ

### 1. 依存関係インストール
```bash
# Java環境
cd backend/java-springboot
./mvnw clean install

# Python環境
cd backend/python-fastapi
pip install -r requirements.txt

# フロントエンド（開発サーバー不要）
# ブラウザで直接 frontend/public_ui/index.html を開く
```

### 2. Docker Compose での起動
```bash
# 全サービス起動
docker-compose up -d

# 特定サービスのみ起動
docker-compose up mysql backend-java

# ログ確認
docker-compose logs -f
```

### 3. データベース初期化（ローカル）
```bash
# MySQL コンテナに接続
docker-compose exec mysql mysql -u root -p

# 初期データ投入
mysql> source /docker-entrypoint-initdb.d/init.sql;
```

## トラブルシューティング

### よくあるエラーと対処法
1. **データベース接続エラー**
   - 環境変数 DATABASE_URL の確認
   - MySQL サービスの起動確認

2. **JWT エラー**
   - JWT_SECRET 環境変数の設定確認
   - 秘密鍵の長さ（32文字以上推奨）

3. **CORS エラー**  
   - フロントエンドとバックエンドのURL設定確認
   - Spring Boot の CORS 設定確認

4. **PDF生成エラー**
   - WeasyPrint 依存関係の確認
   - FastAPI サービスの起動確認
