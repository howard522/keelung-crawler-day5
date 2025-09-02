# Day 5 — 打包與部署（Docker + Railway）

目標：把後端（含前端靜態檔）打包成 Docker 映像，先在本機驗證，再部署到 Railway。
結果：一個網址即可開啟前端首頁 `/`，並能呼叫 `/SightAPI?zone=七堵`。

---

## 0) 先把前端靜態檔放到 Spring Boot
- 在前端專案執行：
  ```bash
  npm ci
  npm run build   # CRA -> build/；Vite -> dist/
  ```
- 複製到後端：
  ```bash
  rm -rf src/main/resources/static/*
  cp -R build/* src/main/resources/static/      # 若是 Vite 改成 dist/*
  ```
- （若有 React Router）可加一個 SPA 轉發 Controller，把非 /SightAPI 的路徑 forward 到 /index.html。

## 1) 調整 application.properties
```properties
server.port=${PORT:8080}
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/keelung}
spring.data.mongodb.auto-index-creation=true
```

## 2) 放入這些檔案（與本 README 同層）
- `Dockerfile`（多階段建置，已附）
- `.dockerignore`（避免把 target/node_modules 打進映像）
- （可選）`docker-compose.yml`（本機一次帶起 mongo+api）

## 3) 本機驗證（兩種方式）

### 方式 A：只跑 API（連本機 MongoDB）
```bash
docker build -t keelung-api:dev .
docker run --rm -p 8080:8080 -e MONGODB_URI="mongodb://host.docker.internal:27017/keelung" keelung-api:dev
```

### 方式 B：用 docker-compose（連容器內 Mongo）
```bash
docker compose up --build
```

驗收：
- 瀏覽 `http://localhost:8080/` 可看到前端頁。
- `curl "http://localhost:8080/SightAPI?zone=七堵"` 會回 JSON。

## 4) 部署到 Railway（重點）
1. 將專案推到 GitHub（根目錄含 `Dockerfile`）。
2. Railway → New Project → Deploy from GitHub → 選此 repo。
3. 在 Project 的 **Variables** 新增：
   - `MONGODB_URI`：你的 Atlas 或 Railway Mongo 連線字串（完整 URI）。
   - `PORT`：`8080`（有些平台會自動提供，但設上可避免衝突）。
4. 按 Deploy / 觀察 Logs，完成後打開 Public URL：
   - `/` 有前端頁
   - `/SightAPI?zone=七堵` 回 JSON

## 5) Debug 小抄
- **前端 404**：確認 `static/` 下有 `index.html` 與資源檔。
- **CORS**：同站部署通常無此問題；若分離部署，Controller 需 `@CrossOrigin("*")`。
- **DB 連線錯**：檢查 `MONGODB_URI` 是否完整（使用者/密碼/資料庫名）。
- **確認走 DB**：在 `application.properties` 打開
  `logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG`，再呼叫 API 看 log。

## 6) （可選）健康檢查
- 新增 `/health` Controller（已附 `HealthController.java` 範例），方便平台探針與你自測：
  ```bash
  curl "http://localhost:8080/health"
  ```

---
祝上雲順利！若要我幫你做 Render / Fly.io 的部署腳本，也可以加上。
