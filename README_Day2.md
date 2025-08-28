# Keelung Sights API — Day 2

以 **Spring Boot** 建立的 REST API，包裝 Day 1 的 **Jsoup 爬蟲**，
提供 `/SightAPI?zone=...` 端點回傳指定行政區的基隆景點清單。

> 課程對應：《新生學習菜單》**作業 Day 2 — Web API**。

---

## 專案說明

- 套件結構：
  - `com.example.crawler`：爬蟲與資料模型
    - `KeelungSightsCrawler`：抓取 TravelKing 基隆市頁面、進入景點詳情頁解析欄位
    - `Sight`：資料模型（`sightName/zone/category/photoURL/address/description`）
  - `com.example.crawlerapi`：Spring Boot 啟動與 API
    - `CrawlerApiApplication`：應用程式進入點
    - `controller.SightApiController`：`/SightAPI` 控制器（含 `@CrossOrigin("*")`）
    - `service.CrawlerService`：呼叫 `KeelungSightsCrawler` 並回傳 `Sight[]`

- 依賴：
  - Spring Boot Web (`spring-boot-starter-web`)
  - Jsoup (`org.jsoup:jsoup:1.15.3`)

---

## 執行環境

- **JDK 8+**
- **Maven 3.x**
- 可連外的網路（執行時即時抓取頁面資料）

---

## 建置與執行

### 1) 使用 Maven 直接啟動（開發模式）
```bash
mvn spring-boot:run
```
預設啟動在 `http://localhost:8080`。

### 2) 打包為可執行 Jar
```bash
mvn -DskipTests clean package
# 若需要可顯式重打包
mvn spring-boot:repackage
```
執行：
```bash
java -jar target/keelung-crawler-api-1.0.0.jar
```

### 3) 變更埠號（可選）
`src/main/resources/application.properties`
```
server.port=8080
```

---

## API 規格

- **Endpoint**：`GET /SightAPI`
- **Query Param**：`zone`（必填），例如：`七堵`、`中正`、`仁愛`…
- **Response**：`Sight[]`（JSON 陣列）

### 範例請求
```bash
# macOS/Linux
curl "http://localhost:8080/SightAPI?zone=七堵"

# Windows (PowerShell)
curl "http://localhost:8080/SightAPI?zone=%E4%B8%83%E5%A0%B5"
```

### 範例回應（節錄）
```json
[
  {
    "sightName": "某某景點",
    "zone": "七堵",
    "category": "步道 / 風景點",
    "photoURL": "https://.../image.jpg",
    "address": "基隆市七堵區...",
    "description": "……"
  }
]
```

### 例外/狀態碼
- `400 Bad Request`：未帶 `zone` 參數（Spring 會自動判定）
- `500 Internal Server Error`：爬取或解析發生未預期錯誤（控制器以 `ResponseEntity.status(500)` 回覆）

---

## 注意事項

- **請尊重目標網站條款與 robots.txt**；避免高頻率請求，必要時增加 `timeout` 或加入簡單節流/重試機制。
- 若目標頁面 DOM 結構變動，請微調 `KeelungSightsCrawler` 中的 CSS 選擇器。
- 編碼建議使用 **UTF-8**（IDE/Console）。

---

## 後續里程碑（預告）

- **Day 3**：RWD 前端介面（七區按鈕、卡片＋折疊）
- **Day 4**：接上 MongoDB（啟動匯入、API 從 DB 查詢）
- **Day 5**：Docker 化與雲端部署（Railway/Render）
