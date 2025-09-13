FROM nginx:alpine

# Nginxの設定ファイルをコピー
COPY nginx.conf /etc/nginx/nginx.conf

# 静的ファイルをコピー
COPY . /usr/share/nginx/html

# SPA用の設定（全てのルートをindex.htmlにリダイレクト）
RUN echo 'server { \
    listen 80; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    \
    # SPAルーティング対応 \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
    \
    # 静的ファイルキャッシュ設定（開発用：キャッシュ無効） \
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ { \
        expires -1; \
        add_header Cache-Control "no-cache, no-store, must-revalidate"; \
        add_header Pragma "no-cache"; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
