# Keelung Sights — Day 4（MongoDB 版）

把 Day 2 的 API **接上 MongoDB**：
- 第一次啟動時，利用 Day 1 的爬蟲把七個行政區的資料「一次抓好」存入資料庫。
- 之後 `/SightAPI` **直接從 MongoDB 查**，不用每次上網，速度更快、也更穩。

---

## 目標與流程（超白話）

- **目的**：把資料放進「倉庫」（MongoDB），之後回應 API 時從倉庫拿，而不是每次都到外面抓。
- **流程**：
  1) 啟動應用 → `DataLoader` 檢查 DB 是否已有資料。
  2) **若沒有**：用爬蟲抓「七堵/中山/中正/仁愛/安樂/信義/暖暖」，寫進 `sights` 集合。
  3) `/SightAPI?zone=...` 讀 DB 回傳結果（`zone` 省略可回全部）。

---

## 專案結構（重點檔案）

```
src/main/java
├─ com/example/crawler/                  # Day 1 的爬蟲與資料模型（保留）
│  ├─ KeelungSightsCrawler.java
│  └─ Sight.java
└─ com/example/crawlerapi
   ├─ CrawlerApiApplication.java
   ├─ controller/SightApiController.java   # 用 DB 版本的 Controller
   ├─ entity/SightEntity.java
   ├─ repo/SightRepository.java
   ├─ service/SightService.java
   └─ startup/DataLoader.java              # 啟動時初次匯入
```

> 以上程式碼樣板已於說明訊息提供；若尚未建立，請依檔名與 package 建立檔案。

---

## 需求環境

- **JDK 8+**
- **Maven 3.x**
- **MongoDB 7+**（本機或雲端 Atlas 皆可）
- 可連外網路（**只在第一次啟動匯入時需要**）

---

## 安裝與啟動

### 1) 啟動本機 MongoDB（建議 Docker）
```bash
docker run -d --name mongo -p 27017:27017 mongo:7
```

### 2) 設定連線字串（`src/main/resources/application.properties`）
```properties
server.port=8080
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/keelung}
spring.data.mongodb.auto-index-creation=true
```
> 上線時改用環境變數 `MONGODB_URI` 指向 Atlas。

### 3) 建置與執行
```bash
mvn -DskipTests clean package
mvn spring-boot:run
```
觀察 Console：第一次會看到類似：
```
[DataLoader] Initial import started...
[DataLoader] Crawling zone: 七堵
...
[DataLoader] Initial import done. Inserted ~ N docs.
```

---

## API 使用說明

- **Endpoint**：`GET /SightAPI`
- **Query**：`zone`（可選；例如「七堵」「中正」；不帶則回全部）
- **回傳**：JSON 陣列（欄位：`sightName/zone/category/photoURL/address/description`）

### 範例
```bash
curl "http://localhost:8080/SightAPI?zone=七堵"
curl "http://localhost:8080/SightAPI"         # 回全部
```

---

## 我怎麼確定資料「真的」是從 MongoDB 來的？（Proof-of-DB）

### 方法 A：看查詢 Log（最直觀）
在 `application.properties` 打開 Mongo 查詢 log：
```properties
logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG
```
重啟後，呼叫 API：
```bash
curl "http://localhost:8080/SightAPI?zone=七堵"
```
Console 會打印類似：
```
DEBUG ... MongoTemplate - find using query { "zone" : { "$regex" : "七堵", "$options" : ""}} in collection sights
```
> 有看到 `MongoTemplate` 對 `sights` 查詢的 log → **就是從 DB 取的**。

---

### 方法 B：用資料庫工具直接查（Compass / mongosh）
1) 安裝 **MongoDB Compass** 或使用 `mongosh`：
   ```bash
   mongosh "mongodb://localhost:27017/keelung"
   ```
2) 查集合與筆數：
   ```js
   db.sights.countDocuments()
   db.sights.find({ zone: /七堵/ }).limit(3)
   db.sights.getIndexes()
   ```
   - 看得到資料與索引（`uniq_name_zone`） → **證明資料在 DB**。
   - `countDocuments()` 的值要與你 API 回傳筆數「大致相符」（不同 `zone` 會不同）。

> 若你用 Compass，直接連 `mongodb://localhost:27017/keelung`，打開 `sights` 集合就能看見資料。

---

### 方法 C：把網路關掉再測（只要 DB 已有資料）
1) 第一次已完成匯入之後（DB **已有資料**）。
2) 暫時讓伺服器機器斷網（或阻擋外連）。
3) 再呼叫：`/SightAPI?zone=七堵`。  
   - 還是有資料 → 表示「**根本沒上網抓**」，就是 **DB 在供應**。

> 反之，若你「把 Mongo 關掉」，API 會回 500（因為連不到 DB），這也證明 API 依賴 DB。

---

## 進階：重新匯入（清空再抓）

若你想 **重新爬取** 最新資料：
1) 刪除集合或整個 DB：
   ```bash
   mongosh "mongodb://localhost:27017/keelung" --eval "db.sights.drop()"
   ```
2) 重新啟動 Spring Boot：`DataLoader` 會再次爬取並匯入。

> 更安全的做法是另做一個管理端點（如 `POST /admin/refresh`）搭配 token 驗證；需要可再告訴我。

---

## 常見問題（Troubleshooting）

- **連不到 Mongo**：檢查 Docker 容器是否有 `-p 27017:27017`、URI 是否正確。  
- **第一次匯入 0 筆**：目標網站 DOM 可能變動；調整 `KeelungSightsCrawler` 的 CSS 選擇器。  
- **重複資料**：已設 `sightName+zone` **唯一索引**，且儲存前會查重；若你手動多次匯入不會無限重複。  
- **前端 CORS**：Controller 已有 `@CrossOrigin("*")`；若前端是 HTTPS，建議 API 也跟著 HTTPS。

---

## 環境變數（上雲必備）

- `MONGODB_URI`：Mongo 連線字串（例如 Atlas 提供）。
- `PORT`：有些平台會注入（Spring Boot 仍用 `server.port`，可相容平台設定）。

---

## 下一步（Day 5 預告）
- 建立 **Dockerfile / docker-compose**，API 與 DB 一起帶起來。
- 連接 Atlas（把 `MONGODB_URI` 設為雲端連線字串），把前端靜態檔放進 `src/main/resources/static/`，單一服務對外。

---

## 授權
此範例僅供教學/練習用途。
